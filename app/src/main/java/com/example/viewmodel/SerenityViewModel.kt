package com.example.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SerenityRepository
import com.example.model.ActionableNudge
import com.example.model.CareActionLog
import com.example.model.ChatMessage
import com.example.model.DailyRhythmBlock
import com.example.model.DemoRecoveryStage
import com.example.model.LifeQuest
import com.example.model.MemoryItem
import com.example.model.MuseDemoScenario
import com.example.model.PresenceMode
import com.example.model.RealConnectorState
import com.example.model.ScreenNav
import com.example.model.SyntheticTelemetrySnapshot
import com.example.model.TamagotchiState
import com.example.model.ThoughtStream
import com.example.model.VitalityCheckin
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class SerenityViewModel(
    private val repository: SerenityRepository = SerenityRepository()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(ScreenNav.ATTUNE)
    val currentScreen: StateFlow<ScreenNav> = _currentScreen.asStateFlow()

    private val _showFullHome = MutableStateFlow(false)
    val showFullHome: StateFlow<Boolean> = _showFullHome.asStateFlow()

    fun setShowFullHome(show: Boolean) {
        _showFullHome.value = show
    }

    private val _showFullData = MutableStateFlow(false)
    val showFullData: StateFlow<Boolean> = _showFullData.asStateFlow()

    fun setShowFullData(show: Boolean) {
        _showFullData.value = show
    }

    private val _showFullConnections = MutableStateFlow(false)
    val showFullConnections: StateFlow<Boolean> = _showFullConnections.asStateFlow()

    fun setShowFullConnections(show: Boolean) {
        _showFullConnections.value = show
    }

    val tamagotchiState: StateFlow<TamagotchiState> = repository.tamagotchiState
    val activeDemoScenario: StateFlow<MuseDemoScenario> = repository.activeDemoScenario
    val demoRecoveryStage: StateFlow<DemoRecoveryStage> = repository.demoRecoveryStage
    val telemetrySnapshot: StateFlow<SyntheticTelemetrySnapshot> = repository.telemetrySnapshot
    val realConnectorState: StateFlow<RealConnectorState> = repository.realConnectorState

    val healthConnectPermissions: Set<String> = repository.healthConnectManager.requiredPermissions
    val healthConnectReadWritePermissions: Set<String> = repository.healthConnectManager.readWritePermissions

    fun createHealthPermissionContract() =
        repository.healthConnectManager.createPermissionRequestContract()

    val careLogs: StateFlow<List<CareActionLog>> = repository.careLogs
    val lifeQuests: StateFlow<List<LifeQuest>> = repository.lifeQuests

    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val chatPendingCount: StateFlow<Int> = repository.chatPendingCount
    val chatReplyStatus: StateFlow<String?> = repository.chatReplyStatus
    val memories: StateFlow<List<MemoryItem>> = repository.memories
    val selectedMode: StateFlow<PresenceMode> = repository.selectedMode
    val auraIntensity: StateFlow<Float> = repository.auraIntensity
    val vitalityCheckin: StateFlow<VitalityCheckin> = repository.vitalityCheckin

    val userName: StateFlow<String> = repository.userName
    val companionName: StateFlow<String> = repository.companionName
    fun getDisplayName(): String = repository.getDisplayName()
    fun setUserName(name: String) = repository.setUserName(name)
    fun setCompanionName(name: String) = repository.setCompanionName(name)

    val thoughtStreams: List<ThoughtStream> = repository.getThoughtStreams()
    val dailyRhythm: List<DailyRhythmBlock> = repository.getDailyRhythm()

    val metaAiConfig = repository.metaAiConfig
    val latestMuseAdvice = repository.latestMuseAdvice
    val integrations = repository.integrations
    val appThemeMode = repository.appThemeMode

    fun setAppThemeMode(mode: AppThemeMode) {
        repository.setAppThemeMode(mode)
    }

    val ttsPitch = repository.ttsPitch
    val ttsSpeechRate = repository.ttsSpeechRate

    fun setTtsPitch(value: Float) = repository.setTtsPitch(value)
    fun setTtsSpeechRate(value: Float) = repository.setTtsSpeechRate(value)

    init {
        syncAllRealChannels()
    }

    fun syncAllRealChannels() {
        viewModelScope.launch {
            repository.syncAllRealChannels()
        }
    }

    fun syncRealHealth() {
        viewModelScope.launch {
            repository.syncRealHealth()
        }
    }

    fun generateSampleHealthData() {
        viewModelScope.launch {
            repository.generateSampleHealthData()
        }
    }

    fun syncRealCalendarAndTasks(googleTasksToken: String? = null) {
        viewModelScope.launch {
            repository.syncRealCalendarAndTasks(googleTasksToken)
        }
    }

    fun syncRealFacebookAndContacts(
        facebookAccessToken: String? = null,
        facebookAppId: String? = null
    ) {
        viewModelScope.launch {
            repository.syncRealFacebookAndContacts(facebookAccessToken, facebookAppId)
        }
    }

    fun launchFacebookOAuth(appId: String) {
        repository.launchFacebookOAuth(appId)
    }

    fun handleOAuthRedirect(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val handled = repository.handleOAuthRedirect(uri)
            if (handled) {
                _currentScreen.value = ScreenNav.CONNECTIONS
                _showFullConnections.value = true
            }
        }
    }

    fun connectPlaidBank(
        clientId: String,
        secret: String,
        environment: String,
        accessToken: String = ""
    ) {
        viewModelScope.launch {
            repository.connectPlaidBank(clientId, secret, environment, accessToken)
        }
    }

    fun connectSimpleFinBank(tokenOrUrl: String) {
        viewModelScope.launch {
            repository.connectSimpleFinBank(tokenOrUrl)
        }
    }

    fun importRealBankCsv(uri: Uri) {
        viewModelScope.launch {
            repository.importRealBankCsv(uri)
        }
    }

    fun openHealthConnectSettings() {
        repository.openHealthConnectSettings()
    }

    fun insertRecoveryCalendarEvent(title: String, description: String) {
        repository.insertRecoveryCalendarEvent(title, description)
    }

    fun activateDemoScenario(scenario: MuseDemoScenario) {
        repository.activateDemoScenario(scenario)
        if (scenario == MuseDemoScenario.LIVE_REAL_DATA) {
            syncAllRealChannels()
        }
    }

    fun completeDemoFollowThrough() {
        if (activeDemoScenario.value == MuseDemoScenario.LIVE_REAL_DATA) {
            syncAllRealChannels()
        }
        repository.completeDemoFollowThrough()
    }

    fun toggleGoogleHealth() {
        repository.toggleGoogleHealth()
    }

    fun toggleFacebook() {
        repository.toggleFacebook()
    }

    fun toggleFinanceApp() {
        repository.toggleFinanceApp()
    }

    fun toggleTasksApp() {
        repository.toggleTasksApp()
    }

    private val _isSyncingWithMuse = MutableStateFlow(false)
    val isSyncingWithMuse: StateFlow<Boolean> = _isSyncingWithMuse.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    fun initTts(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    applyTtsVoiceSettings()
                    // Callbacks arrive on a binder thread: hop to Main for state.
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) {
                            viewModelScope.launch { _isSpeaking.value = false }
                        }
                        override fun onError(utteranceId: String?) {
                            viewModelScope.launch { _isSpeaking.value = false }
                        }
                    })
                }
            }
        }
    }

    private fun applyTtsVoiceSettings() {
        textToSpeech?.setPitch(ttsPitch.value)
        textToSpeech?.setSpeechRate(ttsSpeechRate.value)
    }

    fun speakPrescription(text: String, context: Context) {
        initTts(context)
        if (_isSpeaking.value) {
            textToSpeech?.stop()
            _isSpeaking.value = false
            return
        }
        val cleanText = text.replace("**", "").replace("💡", "").replace("✨", "").replace("🛡️", "").trim()
        if (cleanText.isNotBlank()) {
            _isSpeaking.value = true
            applyTtsVoiceSettings()
            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "lifesos_speech_id")
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun executeActionableNudge(context: Context, nudge: ActionableNudge) {
        when (nudge.type.uppercase(Locale.US)) {
            "WHATSAPP" -> {
                val encoded = Uri.encode(nudge.targetPayload)
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?text=$encoded")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(waIntent)
                } catch (_: Exception) {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, nudge.targetPayload)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(share, "Reach out via").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
                completeDemoFollowThrough()
                repository.nurtureConnect()
            }
            "CALENDAR" -> {
                val calIntent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, nudge.targetPayload.ifBlank { "Wind-Down & Rest" })
                    putExtra(CalendarContract.Events.DESCRIPTION, "Scheduled with LifeSOS Life Equilibrium Companion")
                    val start = System.currentTimeMillis() + 3600000L
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 1800000L)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(calIntent)
                } catch (_: Exception) {
                    insertRecoveryCalendarEvent(nudge.targetPayload, "Scheduled with LifeSOS")
                }
                completeDemoFollowThrough()
            }
            "WATER" -> {
                feedWater()
                completeDemoFollowThrough()
            }
            "WALK" -> {
                logMovementWalk()
                completeDemoFollowThrough()
            }
            else -> {
                completeDemoFollowThrough()
            }
        }
    }

    fun updateMetaAiToken(token: String) {
        repository.updateMetaAiToken(token)
    }

    fun updateChatHistoryWindow(size: Int) {
        repository.updateChatHistoryWindow(size)
    }

    fun selectModelEngine(engine: com.example.model.MetaModelEngine) {
        repository.selectModelEngine(engine)
    }

    fun updateMetaAiConfig(
        token: String,
        endpoint: String,
        model: String,
        isLocalPrivacyMode: Boolean
    ) {
        repository.updateMetaAiConfig(token, endpoint, model, isLocalPrivacyMode)
    }

    fun removeMetaAiToken() {
        repository.removeMetaAiToken()
    }

    fun sendAllInfoToMuse(onSuccess: (com.example.model.MuseLifeAdvice) -> Unit = {}) {
        Log.d(TAG, "consult: button tapped, launching")
        viewModelScope.launch {
            _isSyncingWithMuse.value = true
            try {
                val advice = repository.sendAllInfoToMuse()
                Log.d(TAG, "consult: done modelUsed='${advice.modelUsed}'")
                onSuccess(advice)
            } catch (e: Exception) {
                Log.e(TAG, "consult: FAILED", e)
                throw e
            } finally {
                _isSyncingWithMuse.value = false
            }
        }
    }

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedMemoryCategory = MutableStateFlow("All")
    val selectedMemoryCategory: StateFlow<String> = _selectedMemoryCategory.asStateFlow()

    private val _isBreathingActive = MutableStateFlow(false)
    val isBreathingActive: StateFlow<Boolean> = _isBreathingActive.asStateFlow()

    private val _breathPhase = MutableStateFlow("Inhale...")
    val breathPhase: StateFlow<String> = _breathPhase.asStateFlow()

    // Tamagotchi caretaker actions
    fun feedWater() {
        repository.nurtureWater()
    }

    fun doBreathReset() {
        repository.nurtureBreathe()
        toggleBreathingExercise()
    }

    fun logMovementWalk() {
        repository.nurtureWalk()
    }

    fun logFocusSprint() {
        repository.nurtureFocus()
    }

    fun logConnection() {
        repository.nurtureConnect()
    }

    fun petTamagotchi() {
        repository.petTamagotchi()
    }

    fun toggleSleepMode() {
        repository.toggleSleepMode()
    }

    fun equipAccessory(accessory: String) {
        repository.equipAccessory(accessory)
    }

    fun toggleQuest(questId: String) {
        repository.toggleQuest(questId)
    }

    fun navigateTo(screen: ScreenNav) {
        when (screen) {
            ScreenNav.HOME, ScreenNav.ATTUNE, ScreenNav.DIALOGUE -> _showFullHome.value = false
            ScreenNav.DATA, ScreenNav.VITALITY, ScreenNav.VAULT -> _showFullData.value = false
            ScreenNav.CONNECTIONS, ScreenNav.PRESENCE -> _showFullConnections.value = false
        }
        _currentScreen.value = screen
    }

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun sendCurrentMessage() {
        val text = _inputText.value.trim()
        if (text.isNotEmpty()) {
            Log.d(TAG, "chat: send tapped len=${text.length}")
            _inputText.value = ""
            repository.sendMessage(text)
        } else {
            Log.d(TAG, "chat: send tapped with blank input, ignored")
        }
    }

    fun sendQuickPrompt(promptText: String) {
        repository.sendMessage(promptText)
    }

    fun setPresenceMode(mode: PresenceMode) {
        repository.setPresenceMode(mode)
    }

    fun setAuraIntensity(intensity: Float) {
        repository.setAuraIntensity(intensity)
    }

    fun setMemoryCategory(category: String) {
        _selectedMemoryCategory.value = category
    }

    fun addMemory(title: String, content: String, category: String, isPinned: Boolean = false) {
        repository.addMemory(title, content, category, isPinned)
    }

    fun toggleMemoryPin(id: String) {
        repository.toggleMemoryPin(id)
    }

    fun updateVitality(clarity: Int, equilibrium: Int, peace: Int, mood: String) {
        repository.logVitalityCheckin(clarity, equilibrium, peace, mood)
    }

    fun toggleBreathingExercise() {
        if (_isBreathingActive.value) {
            _isBreathingActive.value = false
        } else {
            _isBreathingActive.value = true
            startBreathingCycle()
        }
    }

    private fun startBreathingCycle() {
        viewModelScope.launch {
            while (_isBreathingActive.value) {
                _breathPhase.value = "Inhale gently (4s)..."
                delay(4000)
                if (!_isBreathingActive.value) break
                _breathPhase.value = "Hold in serene calm (2s)..."
                delay(2000)
                if (!_isBreathingActive.value) break
                _breathPhase.value = "Exhale softly (4s)..."
                delay(4000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    companion object {
        private const val TAG = "LifeSOS"

        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appCtx = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SerenityViewModel(SerenityRepository(appCtx)) as T
                }
            }
        }
    }
}
