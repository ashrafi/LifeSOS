package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.byokDataStore: DataStore<Preferences> by preferencesDataStore(name = "metamuse_secure_byok")

/**
 * Secure Bring-Your-Own-Key (BYOK) manager for MetaMuse.
 *
 * Safeguards implemented:
 * 1. Keystore Encryption: AES-256-GCM key generated inside AndroidKeyStore (hardware backing where the device provides it; StrongBox/hardware level is neither requested nor verified at runtime).
 * 2. Encrypted DataStore: Ciphertext + IV persisted in private Preferences DataStore.
 * 3. Zero Cloud Backups: Explicitly excluded from Google Drive and device transfers in data_extraction_rules.xml.
 * 4. Zero Logging: Plaintext keys are never output to Logcat, crash logs, or analytics.
 * 5. Complete Key Revocation: Users can remove and purge the key at any time.
 * 6. Attacker Threat Model: Transparently disclosed in-app for rooted/compromised device environments.
 */
class SecureByokStore(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "metamuse_byok_aes_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val PAYLOAD_PREFIX = "v1"

        private val PREF_KEY_ENC_META_AI_TOKEN = stringPreferencesKey("enc_meta_ai_token")
        private val PREF_KEY_ENC_PLAID_SECRET = stringPreferencesKey("enc_plaid_secret")
        private val PREF_KEY_HAS_ACTIVE_KEY = booleanPreferencesKey("has_active_byok_key")
        private val PREF_KEY_LAST_UPDATED = longPreferencesKey("byok_last_updated_epoch")

        @Volatile
        private var instance: SecureByokStore? = null

        fun getInstance(context: Context): SecureByokStore {
            return instance ?: synchronized(this) {
                instance ?: SecureByokStore(context.applicationContext).also { instance = it }
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var inMemoryCachedMetaToken: String? = null

    @Volatile
    private var inMemoryCachedPlaidSecret: String? = null

    init {
        // Attempt lazy migration from legacy plaintext SharedPreferences if present
        migrateLegacyTokensIfNeeded()
    }

    // =========================================================================
    // 1. ANDROID KEYSTORE HARDWARE ENCRYPTION / DECRYPTION (AES-256-GCM)
    // =========================================================================

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
            keyGenerator.init(spec)
            return keyGenerator.generateKey()
        }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey
            ?: throw IllegalStateException("Failed to retrieve secret key from AndroidKeystore")
    }

    /**
     * Encrypts plaintext using AES-256-GCM via the Android Keystore.
     * Produces a string in the format: "v1:<base64(iv)>:<base64(ciphertext)>"
     */
    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ciphertextBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
            "$PAYLOAD_PREFIX:$ivBase64:$ciphertextBase64"
        } catch (_: Exception) {
            // Never leak sensitive payload in error
            ""
        }
    }

    /**
     * Decrypts an AES-256-GCM payload using the Android Keystore.
     * Returns empty string if payload is tampered, corrupted, or key is invalidated.
     */
    fun decrypt(encryptedPayload: String): String {
        if (encryptedPayload.isEmpty() || !encryptedPayload.startsWith("$PAYLOAD_PREFIX:")) {
            return ""
        }
        return try {
            val parts = encryptedPayload.split(":", limit = 3)
            if (parts.size != 3) return ""
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[2], Base64.NO_WRAP)
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(ciphertext)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (_: Exception) {
            // Fail safely without logging
            ""
        }
    }

    // =========================================================================
    // 2. DATASTORE STORAGE & REVOCATION APIS
    // =========================================================================

    val hasActiveMetaAiTokenFlow: Flow<Boolean> = context.byokDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val enc = prefs[PREF_KEY_ENC_META_AI_TOKEN]
            prefs[PREF_KEY_HAS_ACTIVE_KEY] == true && !enc.isNullOrBlank()
        }

    /**
     * Saves and encrypts the BYOK token in DataStore.
     */
    suspend fun saveMetaAiToken(token: String) {
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
            removeMetaAiToken()
            return
        }
        val encrypted = encrypt(trimmed)
        if (encrypted.isNotEmpty()) {
            context.byokDataStore.edit { prefs ->
                prefs[PREF_KEY_ENC_META_AI_TOKEN] = encrypted
                prefs[PREF_KEY_HAS_ACTIVE_KEY] = true
                prefs[PREF_KEY_LAST_UPDATED] = System.currentTimeMillis()
            }
            inMemoryCachedMetaToken = trimmed
        }
    }

    /**
     * Retrieves and decrypts the BYOK token.
     */
    suspend fun getMetaAiToken(): String {
        inMemoryCachedMetaToken?.takeIf { it.isNotEmpty() }?.let { return it }
        val prefs = context.byokDataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val enc = prefs[PREF_KEY_ENC_META_AI_TOKEN].orEmpty()
        val decrypted = if (enc.isNotEmpty()) decrypt(enc) else ""
        inMemoryCachedMetaToken = decrypted
        return decrypted
    }

    /**
     * Synchronous accessor for fast repository/viewmodel bootstrap.
     */
    fun getMetaAiTokenSync(): String {
        inMemoryCachedMetaToken?.takeIf { it.isNotEmpty() }?.let { return it }
        return try {
            runBlocking(Dispatchers.IO) {
                getMetaAiToken()
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Revokes and removes the BYOK token permanently from DataStore and memory.
     */
    suspend fun removeMetaAiToken() {
        context.byokDataStore.edit { prefs ->
            prefs.remove(PREF_KEY_ENC_META_AI_TOKEN)
            prefs[PREF_KEY_HAS_ACTIVE_KEY] = false
            prefs[PREF_KEY_LAST_UPDATED] = System.currentTimeMillis()
        }
        inMemoryCachedMetaToken = ""
    }

    fun removeMetaAiTokenSync() {
        inMemoryCachedMetaToken = ""
        scope.launch {
            removeMetaAiToken()
        }
    }

    /**
     * Saves and encrypts Plaid Secret at rest.
     */
    suspend fun savePlaidSecret(secret: String) {
        val trimmed = secret.trim()
        if (trimmed.isEmpty()) {
            removePlaidSecret()
            return
        }
        val encrypted = encrypt(trimmed)
        if (encrypted.isNotEmpty()) {
            context.byokDataStore.edit { prefs ->
                prefs[PREF_KEY_ENC_PLAID_SECRET] = encrypted
            }
            inMemoryCachedPlaidSecret = trimmed
        }
    }

    suspend fun getPlaidSecret(): String {
        inMemoryCachedPlaidSecret?.takeIf { it.isNotEmpty() }?.let { return it }
        val prefs = context.byokDataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .first()
        val enc = prefs[PREF_KEY_ENC_PLAID_SECRET].orEmpty()
        val decrypted = if (enc.isNotEmpty()) decrypt(enc) else ""
        inMemoryCachedPlaidSecret = decrypted
        return decrypted
    }

    suspend fun removePlaidSecret() {
        context.byokDataStore.edit { prefs ->
            prefs.remove(PREF_KEY_ENC_PLAID_SECRET)
        }
        inMemoryCachedPlaidSecret = ""
    }

    fun getPlaidSecretSync(): String {
        inMemoryCachedPlaidSecret?.takeIf { it.isNotEmpty() }?.let { return it }
        return try {
            runBlocking(Dispatchers.IO) {
                getPlaidSecret()
            }
        } catch (_: Exception) {
            ""
        }
    }

    // =========================================================================
    // 3. MIGRATION UTILITY
    // =========================================================================

    private fun migrateLegacyTokensIfNeeded() {
        scope.launch {
            try {
                val legacyPrefs: SharedPreferences = context.getSharedPreferences(
                    "metamuse_real_connectors_prefs",
                    Context.MODE_PRIVATE
                )
                val legacyToken = legacyPrefs.getString("meta_ai_token", null)?.trim()
                if (!legacyToken.isNullOrEmpty()) {
                    saveMetaAiToken(legacyToken)
                    legacyPrefs.edit().remove("meta_ai_token").apply()
                }

                val legacyPlaid = legacyPrefs.getString("plaid_secret", null)?.trim()
                if (!legacyPlaid.isNullOrEmpty()) {
                    savePlaidSecret(legacyPlaid)
                    legacyPrefs.edit().remove("plaid_secret").apply()
                }
            } catch (_: Exception) {
                // Ignore migration errors
            }
        }
    }
}
