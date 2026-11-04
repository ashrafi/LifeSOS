package com.example.model

enum class ScreenNav(val label: String) {
    HOME("Home"),
    DATA("Show Data"),
    CONNECTIONS("Connections"),
    // Legacy aliases preserved for backward compatibility
    ATTUNE("Home"),
    DIALOGUE("Home"),
    VITALITY("Show Data"),
    VAULT("Show Data"),
    PRESENCE("Connections")
}

enum class PresenceMode(
    val title: String,
    val subtitle: String,
    val auraGlowColorHex: Long
) {
    MINDFUL_GUIDE(
        "Mindful Guide",
        "Serene, breath-centered, restorative presence",
        0xFF4ECDC4
    ),
    STRATEGIC_THINKER(
        "Strategic Thinker",
        "Analytical synthesis, crisp priority framing",
        0xFF79F3EA
    ),
    CREATIVE_FLOW(
        "Creative Flow",
        "Vibrant, associative, spark-igniting intuition",
        0xFFFBBF24
    ),
    GENTLE_LISTENER(
        "Gentle Confidant",
        "Empathetic warmth, unhurried space for reflection",
        0xFFFF6F61
    )
}

enum class TamagotchiMood(
    val title: String,
    val emoji: String,
    val description: String
) {
    RADIANT("Happy & Radiant", "✨", "In full bloom from your sustainable life rhythm!"),
    BALANCED("Harmonious & Serene", "🌿", "Calm and contented with your day."),
    HOPEFUL("Hopeful & Recovering", "🌱", "Brightening as you commit to a healthier routine—waiting on follow-through!"),
    SICK("Visibly Sick & Weak", "🤒", "LifeSOS detected a hidden conflict in your life patterns and needs to talk."),
    SAD("Sad & Drifting", "🥺", "Feeling the quiet distance between you and the people who matter most."),
    TIRED("Exhausted & Drained", "🥱", "Your late nights and low sleep are draining our vitality."),
    THIRSTY("Craving Hydration", "💧", "A cup of fresh water would revitalize us."),
    RESTLESS("Seeking Flow", "⚡", "Ready for a quiet deep focus block."),
    SLEEPY("Needs Restorative Pause", "🌙", "Time to step away and breathe."),
    DORMANT("Dormant & Waiting", "💤", "No live signals yet — connect a channel to wake me up!")
}

enum class MuseDemoScenario(
    val id: String,
    val title: String,
    val subtitle: String,
    val intelligenceKind: String,
    val detectedRuleSummary: String,
    val followThroughButtonLabel: String,
    val followThroughEventDetail: String
) {
    LIVE_REAL_DATA(
        id = "live_real_data",
        title = "Live Real Data",
        subtitle = "Real-time telemetry from Health Connect, Calendar/Tasks, Facebook Graph & Bank API",
        intelligenceKind = "Live Bio-Responsive Telemetry",
        detectedRuleSummary = "Evaluating live device & API channels from your real connected accounts.",
        followThroughButtonLabel = "Re-Sync All Real Channels Now",
        followThroughEventDetail = "Re-synced live Health Connect, Calendar/Tasks, Finance, and Social streams"
    ),
    NONE(
        id = "healthy_baseline",
        title = "Healthy Baseline",
        subtitle = "Sustainable equilibrium across Health, Tasks, Finance & Social",
        intelligenceKind = "Symbiotic Equilibrium",
        detectedRuleSummary = "All 4 telemetry channels are balanced and aligned with your core values.",
        followThroughButtonLabel = "Log Mindful Check-in",
        followThroughEventDetail = "Logged daily balance across sleep, tasks, budget, and relationships"
    ),
    SUCCESS_TRAP(
        id = "success_trap",
        title = "1. The Success Trap",
        subtitle = "High productivity & +20% savings, but 5h sleep & 3 canceled friend plans",
        intelligenceKind = "Multi-Channel Trade-off Intelligence",
        detectedRuleSummary = "Rule Triggered: Sleep dropped 7.5h → 5.0h (14d) + Late-night tasks (98%) + 3 canceled social plans despite +20% savings.",
        followThroughButtonLabel = "Log Follow-Through: 11 PM Cutoff & Weekend Plan",
        followThroughEventDetail = "Logged: Stopped working before midnight (7.5h sleep logged) & confirmed weekend brunch with close friends"
    ),
    INVISIBLE_MONEY_LEAK(
        id = "money_leak",
        title = "2. The Invisible Money Leak",
        subtitle = "Exhausted solitary evenings driving convenience delivery spend",
        intelligenceKind = "Cross-Channel Behavioral Pattern Intelligence",
        detectedRuleSummary = "Rule Triggered: Consecutive poor sleep + high evening task stress + elevated delivery-merchant spending (association, not established cause).",
        followThroughButtonLabel = "Log Follow-Through: 5-Min Easy Dinner & Evening Unwind",
        followThroughEventDetail = "Logged: Prepared 5-minute home meal, skipped delivery order (+$34 saved), and enjoyed a relaxing evening read"
    ),
    CONNECTION_DEBT(
        id = "connection_debt",
        title = "3. The Connection Debt",
        subtitle = "Healthy body, tasks & budget, but drifting from sibling & close friends",
        intelligenceKind = "Values-vs-Behavior Alignment Intelligence",
        detectedRuleSummary = "Rule Triggered: Stated value 'Family & Close Friends First' conflicts with 3 postponed sibling calls, missed birthday, & 2 rescheduled visits.",
        followThroughButtonLabel = "Log Follow-Through: Record 10-Min Call with Sibling",
        followThroughEventDetail = "Logged: Completed 14-minute catch-up call with sibling and sent birthday voice note to close friend"
    )
}

enum class DemoRecoveryStage(val label: String) {
    PROACTIVE_CONCERN("LifeSOS Initiated Concern (Step 1 of 2)"),
    AWAITING_COMMITMENT("Exploring Context (Step 2 of 2)"),
    HOPEFUL_COMMITTED("Committed — Awaiting Logged Follow-Through"),
    RECOVERED_HAPPY("Logged Recovery — Healthy & Happy")
}

data class ChannelMetricItem(
    val label: String,
    val value: String,
    val trend: String,
    val isWarning: Boolean = false
)

data class ChannelTelemetryReport(
    val channelName: String,
    val headlineStatus: String,
    val summary: String,
    val scorePercent: Int,
    val isAlert: Boolean,
    val metrics: List<ChannelMetricItem>,
    val historyLog: List<String>,
    val isAvailable: Boolean = true
)

data class SyntheticTelemetrySnapshot(
    val scenario: MuseDemoScenario,
    val deterministicTriggerActive: Boolean,
    val triggeredRuleExplanation: String,
    val healthReport: ChannelTelemetryReport,
    val tasksReport: ChannelTelemetryReport,
    val financeReport: ChannelTelemetryReport,
    val socialReport: ChannelTelemetryReport,
    val weeklySleepHours: List<Float>
)

data class TamagotchiState(
    val name: String = "Lumi",
    val evolution: String = "Bio-Responsive Companion Charm",
    val level: Int = 1,
    val xp: Int = 0,
    val xpForNextLevel: Int = 1000,
    val hydration: Float = 0f,
    val focus: Float = 0f,
    val innerPeace: Float = 0f,
    val physicalEnergy: Float = 0f,
    val connection: Float = 0f,
    val mood: TamagotchiMood = TamagotchiMood.DORMANT,
    val speech: String = "Still asleep — connect a channel in the Connections tab and I'll wake up. 💤",
    val equippedAccessory: String = "Celestial Halo",
    val isSleeping: Boolean = false,
    val totalPets: Int = 0,
    val activeScenario: MuseDemoScenario = MuseDemoScenario.NONE,
    val recoveryStage: DemoRecoveryStage = DemoRecoveryStage.RECOVERED_HAPPY
) {
    val overallLifeScore: Int
        get() = ((hydration + focus + innerPeace + physicalEnergy + connection) / 5f * 100).toInt()
}

data class LifeQuest(
    val id: String,
    val title: String,
    val impact: String,
    val category: String,
    val isCompleted: Boolean = false
)

data class CareActionLog(
    val id: String,
    val actionName: String,
    val detail: String,
    val timestamp: String,
    val xpGained: Int
)

data class ThoughtStep(
    val title: String,
    val detail: String
)

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val content: String,
    val timestamp: String,
    val thoughtSteps: List<ThoughtStep> = emptyList(),
    val suggestedPrompts: List<String> = emptyList(),
    val category: String? = null
)

data class ThoughtStream(
    val id: String,
    val title: String,
    val tag: String,
    val summary: String,
    val prompt: String,
    val accentType: AccentType
)

enum class AccentType {
    CORAL, MINT, AMBER
}

data class DailyRhythmBlock(
    val time: String,
    val title: String,
    val description: String,
    val isCurrent: Boolean
)

data class MemoryItem(
    val id: String,
    val title: String,
    val content: String,
    val category: String, // "Intentions", "Values", "Projects", "Reflections"
    val date: String,
    val isPinned: Boolean = false
)

data class VitalityCheckin(
    val clarityScore: Int,
    val emotionalEquilibrium: Int,
    val restorativePeace: Int,
    val moodWord: String
)

data class ActionableNudge(
    val type: String, // "WHATSAPP", "CALENDAR", "WATER", "WALK"
    val title: String,
    val description: String,
    val actionButtonText: String,
    val targetPayload: String = "",
    val isCompleted: Boolean = false
)

enum class MetaModelEngine(
    val id: String,
    val displayName: String,
    val defaultModelName: String,
    val defaultEndpoint: String,
    val description: String,
    val isPrimaryStar: Boolean = false
) {
    MUSE_SPARK_1_3(
        id = "muse_spark_1_3",
        displayName = "⭐ Meta Muse Spark 1.3",
        defaultModelName = "muse-spark-1.3",
        defaultEndpoint = "https://api.meta.ai/v1/chat/completions",
        description = "Meta Model API flagship intelligence. Empathetic cross-stream life reasoning & proactive coaching.",
        isPrimaryStar = true
    ),
    ON_DEVICE_LLAMA(
        id = "on_device_llama",
        displayName = "🔒 Offline Engine",
        defaultModelName = "offline-rules-v1",
        defaultEndpoint = "local://offline-engine",
        description = "Zero-Cloud privacy mode: rule-based synthesis, nothing leaves your device. Local Llama weights via ExecuTorch on the roadmap."
    );

    companion object {
        fun fromId(id: String?): MetaModelEngine {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MUSE_SPARK_1_3
        }

        fun fromModelName(modelName: String?): MetaModelEngine {
            return when {
                modelName?.contains("spark", ignoreCase = true) == true -> MUSE_SPARK_1_3
                modelName?.contains("1b", ignoreCase = true) == true || modelName?.contains("local", ignoreCase = true) == true || modelName?.contains("offline", ignoreCase = true) == true -> ON_DEVICE_LLAMA
                else -> MUSE_SPARK_1_3 // Spark-first default (retired Llama cloud option removed)
            }
        }
    }
}

data class MetaAiConfig(
    val token: String = "",
    val isConnected: Boolean = false,
    val lastSyncedAt: String? = null,
    val endpointUrl: String = "https://api.meta.ai/v1/chat/completions",
    val modelName: String = "muse-spark-1.3",
    val selectedEngine: MetaModelEngine = MetaModelEngine.MUSE_SPARK_1_3,
    val isLocalLlamaPrivacyMode: Boolean = false,
    val isKeyEncryptedInKeystore: Boolean = true,
    val hasConfiguredKey: Boolean = false,
    val chatHistoryWindow: Int = 7
)

data class MuseLifeAdvice(
    val id: String,
    val timestamp: String,
    val headline: String,
    val overallDiagnosis: String,
    val pillars: List<MusePillarAdvice>,
    val tamagotchiPrescription: String,
    val reasoningSteps: List<ThoughtStep> = emptyList(),
    val actionableNudge: ActionableNudge? = null,
    val llamaGuardStatus: String = "Prompt-Level Safety Guidance (No Separate Classifier Call)",
    val modelUsed: String = "Meta Muse Spark 1.3 (Meta Model API)",
    /**
     * Set only when a key was configured but the cloud call failed: tells the
     * user on-screen why they are seeing offline guidance instead of failing
     * silently. Null for intentional offline (privacy mode) and keyless runs,
     * where the modelUsed label already explains the path.
     */
    val cloudFailureNote: String? = null
)

data class MusePillarAdvice(
    val pillar: String,
    val score: Int,
    val recommendation: String,
    val suggestedAction: String
)

data class LifeSyncIntegrations(
    val googleHealthConnected: Boolean = false,
    val googleHealthStats: String = "Not connected • Grant Android Health Connect access",
    val facebookConnected: Boolean = false,
    val facebookStats: String = "Not connected • Connect Facebook Graph API or Contacts",
    val financeAppConnected: Boolean = false,
    val financeAppStats: String = "Not connected • Link Plaid API, SimpleFIN, or Bank CSV",
    val tasksAppConnected: Boolean = false,
    val tasksAppStats: String = "Not connected • Grant Android Calendar / Google Tasks access"
)

data class RealConnectorState(
    val isSyncing: Boolean = false,
    val lastSyncSummary: String = "Ready to connect live data streams",
    val lastError: String? = null,
    // 1. Google Health Connect
    val healthSdkAvailable: Boolean = true,
    val healthPermissionsGranted: Boolean = false,
    val liveStepsToday: Long = 0L,
    val liveAvgSleepHours: Float = 0f,
    val liveHeartRateBpm: Long = 0L,
    val liveHydrationLiters: Double = 0.0,
    // 2. Calendar & Google Tasks
    val calendarPermissionGranted: Boolean = false,
    val googleTasksToken: String = "",
    val liveEventsToday: Int = 0,
    val liveCompletedEventsToday: Int = 0,
    val liveLateNightEvents7d: Int = 0,
    val liveCanceledEvents14d: Int = 0,
    // 3. Facebook Graph API & Close Contacts
    val facebookAppId: String = "",
    val facebookAccessToken: String = "",
    val facebookUserName: String? = null,
    val contactsPermissionGranted: Boolean = false,
    val liveStarredContactsCount: Int = 0,
    val liveNeglectedContactsCount: Int = 0,
    // 4. Bank / Finance (Plaid + SimpleFIN + CSV)
    val plaidClientId: String = "",
    val plaidSecret: String = "",
    val plaidEnvironment: String = "sandbox", // "sandbox" or "production"
    val plaidAccessToken: String = "",
    val simpleFinUrlOrToken: String = "",
    val connectedBankSource: String? = null, // "Plaid API", "SimpleFIN", "CSV Statement"
    val liveTotalBalanceUsd: Double? = null,
    val liveMonthlySpendUsd: Double = 0.0,
    val liveDeliveryOrderCount: Int = 0,
    val liveDeliverySpendUsd: Double = 0.0
)
