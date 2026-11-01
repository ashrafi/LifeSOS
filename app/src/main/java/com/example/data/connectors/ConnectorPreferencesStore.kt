package com.example.data.connectors

import android.content.Context
import android.content.SharedPreferences
import com.example.data.security.SecureByokStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class ConnectorPreferencesStore(context: Context) {

    private val secureByokStore = SecureByokStore.getInstance(context)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("metamuse_real_connectors_prefs", Context.MODE_PRIVATE)

    var metaAiToken: String
        get() = secureByokStore.getMetaAiTokenSync()
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) {
                secureByokStore.removeMetaAiTokenSync()
            } else {
                runBlocking(Dispatchers.IO) {
                    secureByokStore.saveMetaAiToken(trimmed)
                }
            }
        }

    fun removeMetaAiToken() {
        secureByokStore.removeMetaAiTokenSync()
    }

    var selectedModelEngine: String
        get() = prefs.getString("selected_model_engine", "muse_spark_1_3") ?: "muse_spark_1_3"
        set(value) = prefs.edit().putString("selected_model_engine", value).apply()

    var metaAiEndpoint: String
        get() = prefs.getString("meta_ai_endpoint", "https://api.meta.ai/v1/chat/completions") ?: "https://api.meta.ai/v1/chat/completions"
        set(value) = prefs.edit().putString("meta_ai_endpoint", value).apply()

    var metaAiModel: String
        get() = prefs.getString("meta_ai_model", "muse-spark-1.3") ?: "muse-spark-1.3"
        set(value) = prefs.edit().putString("meta_ai_model", value).apply()

    var isLocalLlamaPrivacyMode: Boolean
        get() = prefs.getBoolean("is_local_llama_privacy_mode", false)
        set(value) = prefs.edit().putBoolean("is_local_llama_privacy_mode", value).apply()

    var googleTasksToken: String
        get() = prefs.getString("google_tasks_token", "") ?: ""
        set(value) = prefs.edit().putString("google_tasks_token", value).apply()

    var facebookAppId: String
        get() = prefs.getString("facebook_app_id", "") ?: ""
        set(value) = prefs.edit().putString("facebook_app_id", value).apply()

    var facebookAccessToken: String
        get() = prefs.getString("facebook_access_token", "") ?: ""
        set(value) = prefs.edit().putString("facebook_access_token", value).apply()

    var facebookUserName: String?
        get() = prefs.getString("facebook_user_name", null)
        set(value) = prefs.edit().putString("facebook_user_name", value).apply()

    var plaidClientId: String
        get() = prefs.getString("plaid_client_id", "") ?: ""
        set(value) = prefs.edit().putString("plaid_client_id", value).apply()

    var plaidSecret: String
        get() = secureByokStore.getPlaidSecretSync()
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) {
                runBlocking(Dispatchers.IO) {
                    secureByokStore.removePlaidSecret()
                }
            } else {
                runBlocking(Dispatchers.IO) {
                    secureByokStore.savePlaidSecret(trimmed)
                }
            }
        }

    fun removePlaidSecret() {
        runBlocking(Dispatchers.IO) {
            secureByokStore.removePlaidSecret()
        }
    }

    var plaidEnvironment: String
        get() = prefs.getString("plaid_environment", "sandbox") ?: "sandbox"
        set(value) = prefs.edit().putString("plaid_environment", value).apply()

    var plaidAccessToken: String
        get() = prefs.getString("plaid_access_token", "") ?: ""
        set(value) = prefs.edit().putString("plaid_access_token", value).apply()

    var simpleFinUrlOrToken: String
        get() = prefs.getString("simplefin_url_or_token", "") ?: ""
        set(value) = prefs.edit().putString("simplefin_url_or_token", value).apply()

    var cachedCsvTransactionsJson: String
        get() = prefs.getString("cached_csv_transactions_json", "") ?: ""
        set(value) = prefs.edit().putString("cached_csv_transactions_json", value).apply()

    var cachedCsvLabel: String?
        get() = prefs.getString("cached_csv_label", null)
        set(value) = prefs.edit().putString("cached_csv_label", value).apply()

    var appThemeMode: String
        get() = prefs.getString("app_theme_mode", "META_HORIZON") ?: "META_HORIZON"
        set(value) = prefs.edit().putString("app_theme_mode", value).apply()

    var ttsPitch: Float
        get() = prefs.getFloat("tts_pitch", 1.04f)
        set(value) = prefs.edit().putFloat("tts_pitch", value.coerceIn(0.5f, 2.0f)).apply()

    var ttsSpeechRate: Float
        get() = prefs.getFloat("tts_speech_rate", 0.96f)
        set(value) = prefs.edit().putFloat("tts_speech_rate", value.coerceIn(0.5f, 2.0f)).apply()

    // Persisted live companion state: written by every live evaluation (foreground or
    // background Worker) so Lumi's body survives process death and app-closed periods.
    var vitalHydration: Float
        get() = prefs.getFloat("live_vital_hydration", 0.85f)
        set(value) = prefs.edit().putFloat("live_vital_hydration", value).apply()

    var vitalFocus: Float
        get() = prefs.getFloat("live_vital_focus", 0.90f)
        set(value) = prefs.edit().putFloat("live_vital_focus", value).apply()

    var vitalInnerPeace: Float
        get() = prefs.getFloat("live_vital_inner_peace", 0.82f)
        set(value) = prefs.edit().putFloat("live_vital_inner_peace", value).apply()

    var vitalPhysicalEnergy: Float
        get() = prefs.getFloat("live_vital_physical_energy", 0.78f)
        set(value) = prefs.edit().putFloat("live_vital_physical_energy", value).apply()

    var vitalConnection: Float
        get() = prefs.getFloat("live_vital_connection", 0.80f)
        set(value) = prefs.edit().putFloat("live_vital_connection", value).apply()

    var liveMoodName: String
        get() = prefs.getString("live_mood", "RADIANT") ?: "RADIANT"
        set(value) = prefs.edit().putString("live_mood", value).apply()

    var liveSpeech: String
        get() = prefs.getString("live_speech", "") ?: ""
        set(value) = prefs.edit().putString("live_speech", value).apply()

    var lastLiveSyncEpochMillis: Long
        get() = prefs.getLong("live_last_sync_epoch", 0L)
        set(value) = prefs.edit().putLong("live_last_sync_epoch", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "") ?: ""
        set(value) = prefs.edit().putString("user_name", value.trim()).apply()

    fun getDisplayName(): String {
        val custom = userName.trim()
        if (custom.isNotEmpty()) return custom
        val fb = facebookUserName?.trim()
        if (!fb.isNullOrEmpty()) {
            return fb.split(" ").firstOrNull() ?: fb
        }
        return "Friend"
    }

    var companionName: String
        get() = prefs.getString("companion_name", "Lumi") ?: "Lumi"
        set(value) = prefs.edit().putString("companion_name", value.trim().ifEmpty { "Lumi" }).apply()

    var totalPets: Int
        get() = prefs.getInt("total_pets", 0)
        set(value) = prefs.edit().putInt("total_pets", value).apply()

    var companionXp: Int
        get() = prefs.getInt("companion_xp", 0)
        set(value) = prefs.edit().putInt("companion_xp", value).apply()

    var companionLevel: Int
        get() = prefs.getInt("companion_level", 1)
        set(value) = prefs.edit().putInt("companion_level", value).apply()

    var clarityScore: Int
        get() = prefs.getInt("vitality_clarity_score", 85)
        set(value) = prefs.edit().putInt("vitality_clarity_score", value).apply()

    var emotionalEquilibrium: Int
        get() = prefs.getInt("vitality_emotional_equilibrium", 85)
        set(value) = prefs.edit().putInt("vitality_emotional_equilibrium", value).apply()

    var restorativePeace: Int
        get() = prefs.getInt("vitality_restorative_peace", 85)
        set(value) = prefs.edit().putInt("vitality_restorative_peace", value).apply()

    var moodWord: String
        get() = prefs.getString("vitality_mood_word", "Harmonious Flow") ?: "Harmonious Flow"
        set(value) = prefs.edit().putString("vitality_mood_word", value).apply()

    var savedMemoriesJson: String
        get() = prefs.getString("saved_memories_json", "") ?: ""
        set(value) = prefs.edit().putString("saved_memories_json", value).apply()

    var completedQuestIds: Set<String>
        get() = prefs.getStringSet("completed_quest_ids", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("completed_quest_ids", value).apply()
}
