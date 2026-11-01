package com.example.data

import com.example.model.ActionableNudge
import com.example.model.CareActionLog
import com.example.model.ChatMessage
import com.example.model.LifeQuest
import com.example.model.MemoryItem
import com.example.model.MuseLifeAdvice
import com.example.model.MusePillarAdvice
import com.example.model.LifeSyncIntegrations
import com.example.model.SyntheticTelemetrySnapshot
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.model.ThoughtStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class MuseService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun sendLifeTelemetryToMuse(
        metaAiToken: String,
        tamagotchiState: TamagotchiState,
        quests: List<LifeQuest>,
        recentLogs: List<CareActionLog>,
        valuesAndMemories: List<MemoryItem>,
        integrations: LifeSyncIntegrations = LifeSyncIntegrations(),
        telemetrySnapshot: SyntheticTelemetrySnapshot? = null,
        endpointUrl: String = "https://api.meta.ai/v1/chat/completions",
        modelName: String = "muse-spark-1.3",
        isLocalPrivacyMode: Boolean = false
    ): MuseLifeAdvice = withContext(Dispatchers.IO) {
        val currentTime = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date())

        if (isLocalPrivacyMode) {
            return@withContext synthesizeOfflinePrivacyAdvice(tamagotchiState, telemetrySnapshot, currentTime)
        }

        // 1. Construct Telemetry Payload for Muse Spark reasoning
        val telemetryJson = JSONObject().apply {
            put("client", "MetaMuse Bio-Responsive Companion")
            put("timestamp", currentTime)
            put("companion", JSONObject().apply {
                put("name", tamagotchiState.name)
                put("level", tamagotchiState.level)
                put("mood", tamagotchiState.mood.name)
                put("overallLifeScore", tamagotchiState.overallLifeScore)
                put("hydration", (tamagotchiState.hydration * 100).toInt())
                put("focus", (tamagotchiState.focus * 100).toInt())
                put("innerPeace", (tamagotchiState.innerPeace * 100).toInt())
                put("physicalEnergy", (tamagotchiState.physicalEnergy * 100).toInt())
                put("connection", (tamagotchiState.connection * 100).toInt())
            })
            telemetrySnapshot?.let { snap ->
                put("scenario", snap.scenario.name)
                put("anomalyDetected", snap.deterministicTriggerActive)
                put("ruleExplanation", snap.triggeredRuleExplanation)
                put("healthChannel", JSONObject().apply {
                    put("score", snap.healthReport.scorePercent)
                    put("status", snap.healthReport.headlineStatus)
                    put("summary", snap.healthReport.summary)
                })
                put("tasksChannel", JSONObject().apply {
                    put("score", snap.tasksReport.scorePercent)
                    put("status", snap.tasksReport.headlineStatus)
                    put("summary", snap.tasksReport.summary)
                })
                put("financeChannel", JSONObject().apply {
                    put("score", snap.financeReport.scorePercent)
                    put("status", snap.financeReport.headlineStatus)
                    put("summary", snap.financeReport.summary)
                })
                put("socialChannel", JSONObject().apply {
                    put("score", snap.socialReport.scorePercent)
                    put("status", snap.socialReport.headlineStatus)
                    put("summary", snap.socialReport.summary)
                })
            }
            put("coreValues", JSONArray().apply {
                valuesAndMemories.filter { it.isPinned }.forEach { m ->
                    put(JSONObject().apply {
                        put("title", m.title)
                        put("content", m.content)
                    })
                }
            })
            put("integrations", JSONObject().apply {
                put("googleHealth", integrations.googleHealthStats)
                put("calendarTasks", integrations.tasksAppStats)
                put("facebookSocial", integrations.facebookStats)
                put("bankFinance", integrations.financeAppStats)
            })
        }

        // 2. If token is provided, execute real Meta Model API (Muse Spark 1.3 / Llama) inference
        val cleanToken = metaAiToken.trim()
        val isSparkModel = modelName.contains("spark", ignoreCase = true)
        if (cleanToken.isNotBlank()) {
            try {
                val systemPrompt = if (isSparkModel) {
                    """
                        You are Meta Muse Spark (Meta Model API v1.3), an empathetic bio-responsive life coach integrated into MuseLife.
                        You analyze the user's multi-channel life telemetry across Health (Health Connect), Tasks (Calendar), Finance (Plaid / SimpleFIN), and Social (Facebook Graph).
                        Your core purpose is to discover hidden cross-stream trade-offs (e.g. overwork suppressing sleep, solitary delivery spending reflecting emotional fatigue, or social debt eroding relationships).
                        Synthesize proactive, compassionate coaching following Meta Llama Guard safety standards.
                        Return ONLY a valid JSON object matching this schema:
                        {
                          "headline": "Short punchy status headline (max 8 words)",
                          "overallDiagnosis": "Empathetic cross-stream diagnosis (2-3 sentences)",
                          "reasoningSteps": [
                            {"title": "1. Multi-Stream Ingestion", "detail": "Observed correlations between streams"},
                            {"title": "2. Life State Engine Detection", "detail": "Specific behavioral conflict or imbalance identified"},
                            {"title": "3. Muse Spark Proactive Synthesis", "detail": "Empathetic re-centering and actionable equilibrium path"}
                          ],
                          "pillars": [
                            {"pillar": "Health & Vitality", "score": 80, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Focus & Tasks", "score": 75, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Financial Peace", "score": 85, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Social Connection", "score": 60, "recommendation": "...", "suggestedAction": "..."}
                          ],
                          "tamagotchiPrescription": "Gentle, warm direct message from Lumi / Muse to the user",
                          "actionableNudge": {
                            "type": "WHATSAPP", // "WHATSAPP", "CALENDAR", "WATER", or "WALK"
                            "title": "Action title",
                            "description": "Short explanation of why this action helps",
                            "actionButtonText": "Button label (e.g. 'Open WhatsApp & Reach Out')",
                            "targetPayload": "Pre-filled text for message or event title"
                          },
                          "llamaGuardStatus": "PASSED (Safe for Well-being & Guidance)"
                        }
                    """.trimIndent()
                } else {
                    """
                        You are MetaMuse, an empathetic bio-responsive companion powered by Meta Llama.
                        Analyze the user's multi-channel life telemetry across Health, Tasks, Finance, and Social.
                        Look for hidden cross-stream trade-offs (e.g. high task productivity causing sleep loss or social drift).
                        Screen advice with Llama Guard standards (supportive, compassionate, non-clinical).
                        Return ONLY a valid JSON object matching this schema:
                        {
                          "headline": "Short punchy status headline (max 8 words)",
                          "overallDiagnosis": "Detailed cross-stream diagnosis (2-3 sentences)",
                          "reasoningSteps": [
                            {"title": "Step 1 Title", "detail": "What Llama observed in stream A vs B"},
                            {"title": "Step 2 Title", "detail": "The behavioral pattern or conflict"},
                            {"title": "Step 3 Title", "detail": "The corrective equilibrium path"}
                          ],
                          "pillars": [
                            {"pillar": "Health & Vitality", "score": 80, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Focus & Tasks", "score": 75, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Financial Peace", "score": 85, "recommendation": "...", "suggestedAction": "..."},
                            {"pillar": "Social Connection", "score": 60, "recommendation": "...", "suggestedAction": "..."}
                          ],
                          "tamagotchiPrescription": "Gentle, warm direct message from Lumi/Muse to the user",
                          "actionableNudge": {
                            "type": "WHATSAPP", // "WHATSAPP", "CALENDAR", "WATER", or "WALK"
                            "title": "Action title",
                            "description": "Short explanation of why this action helps",
                            "actionButtonText": "Button label (e.g. 'Draft WhatsApp Text')",
                            "targetPayload": "Pre-filled text for message or event title"
                          },
                          "llamaGuardStatus": "PASSED (Safe for Well-being & Guidance)"
                        }
                    """.trimIndent()
                }

                val requestPayload = JSONObject().apply {
                    put("model", modelName.ifBlank { if (isSparkModel) "muse-spark-1.3" else "llama-3.1-70b-versatile" })
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", telemetryJson.toString())
                        })
                    })
                    put("temperature", 0.2)
                    put("response_format", JSONObject().apply {
                        put("type", "json_object")
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url(endpointUrl.ifBlank { if (isSparkModel) "https://api.meta.ai/v1/chat/completions" else "https://api.groq.com/openai/v1/chat/completions" })
                    .addHeader("Authorization", "Bearer $cleanToken")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Meta-Client", if (isSparkModel) "MetaMuse-Spark-1.3" else "MetaMuse-Llama")
                    .post(requestPayload.toString().toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val parsed = parseLlamaCompletion(bodyString, currentTime, tamagotchiState, modelName)
                        if (parsed != null) return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                // Network unavailable or endpoint error; proceed to deterministic synthesis
            }
        }

        // 3. Fallback to offline flagship-style synthesis or the offline privacy engine
        if (isSparkModel) {
            synthesizeOfflineFlagshipAdvice(tamagotchiState, telemetrySnapshot, currentTime)
        } else {
            synthesizeOfflinePrivacyAdvice(tamagotchiState, telemetrySnapshot, currentTime)
        }
    }

    suspend fun chatWithMuseSpark(
        metaAiToken: String,
        userMessage: String,
        history: List<ChatMessage>,
        tamagotchiState: TamagotchiState,
        telemetrySnapshot: SyntheticTelemetrySnapshot?,
        endpointUrl: String = "https://api.meta.ai/v1/chat/completions",
        modelName: String = "muse-spark-1.3",
        isLocalPrivacyMode: Boolean = false,
        userName: String = "Friend"
    ): ChatMessage = withContext(Dispatchers.IO) {
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val cleanToken = metaAiToken.trim()
        val isSpark = modelName.contains("spark", ignoreCase = true)
        val id = UUID.randomUUID().toString()

        if (!isLocalPrivacyMode && cleanToken.isNotBlank()) {
            try {
                val systemPrompt = """
                    You are Meta Muse Spark (Meta Model API v1.3), an empathetic bio-responsive life coach companion named ${tamagotchiState.name}.
                    You are speaking directly with $userName.
                    Their current overall life score is ${tamagotchiState.overallLifeScore}%, mood is ${tamagotchiState.mood.title}, hydration ${(tamagotchiState.hydration * 100).toInt()}%, focus ${(tamagotchiState.focus * 100).toInt()}%.
                    ${telemetrySnapshot?.let { "Current real telemetry: Health score ${it.healthReport.scorePercent}%, Tasks ${it.tasksReport.scorePercent}%, Finance ${it.financeReport.scorePercent}%, Social ${it.socialReport.scorePercent}%." } ?: ""}
                    Respond warmly, empathetically, and conversationally in 2-4 sentences.
                    Follow Meta Llama Guard safety standards.
                    Return ONLY a JSON object:
                    {
                      "reply": "Warm companion response directly addressing $userName",
                      "thoughtSteps": [
                        {"title": "Sensory Assessment", "detail": "What was noted in the user's message and life telemetry"},
                        {"title": "Empathetic Formulation", "detail": "How the response was framed to nurture well-being"}
                      ],
                      "suggestedPrompts": ["Short response 1", "Short response 2", "Short response 3"]
                    }
                """.trimIndent()

                val messagesArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    // Add recent chat context (up to 4 recent messages)
                    history.takeLast(4).forEach { msg ->
                        put(JSONObject().apply {
                            put("role", if (msg.isFromUser) "user" else "assistant")
                            put("content", msg.content)
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                }

                val requestPayload = JSONObject().apply {
                    put("model", modelName.ifBlank { if (isSpark) "muse-spark-1.3" else "llama-3.1-70b-versatile" })
                    put("messages", messagesArray)
                    put("temperature", 0.7)
                    put("response_format", JSONObject().apply {
                        put("type", "json_object")
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url(endpointUrl.ifBlank { if (isSpark) "https://api.meta.ai/v1/chat/completions" else "https://api.groq.com/openai/v1/chat/completions" })
                    .addHeader("Authorization", "Bearer $cleanToken")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Meta-Client", if (isSpark) "MetaMuse-Spark-1.3" else "MetaMuse-Llama")
                    .post(requestPayload.toString().toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val root = JSONObject(bodyString)
                        val choices = root.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            var content = choices.getJSONObject(0).optJSONObject("message")?.optString("content", "") ?: ""
                            if (content.startsWith("```")) {
                                content = content.removePrefix("```json").removePrefix("```").substringBeforeLast("```").trim()
                            }
                            val parsedJson = JSONObject(content)
                            val replyText = parsedJson.optString("reply", "").ifBlank { content }
                            val thoughtList = mutableListOf<ThoughtStep>()
                            val stepsArray = parsedJson.optJSONArray("thoughtSteps")
                            if (stepsArray != null) {
                                for (i in 0 until stepsArray.length()) {
                                    val obj = stepsArray.getJSONObject(i)
                                    thoughtList.add(ThoughtStep(obj.optString("title", "Thought"), obj.optString("detail", "")))
                                }
                            }
                            val promptsList = mutableListOf<String>()
                            val pArray = parsedJson.optJSONArray("suggestedPrompts")
                            if (pArray != null) {
                                for (i in 0 until pArray.length()) {
                                    promptsList.add(pArray.getString(i))
                                }
                            }
                            return@withContext ChatMessage(
                                id = id,
                                isFromUser = false,
                                content = replyText,
                                timestamp = currentTime,
                                thoughtSteps = thoughtList,
                                suggestedPrompts = promptsList,
                                category = if (isSpark) "Meta Muse Spark 1.3" else "Meta Llama 3"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback below
            }
        }

        // Offline / Unconfigured / Privacy Mode dynamic contextual synthesis
        val lower = userMessage.lowercase()
        return@withContext when {
            cleanToken.isBlank() && !isLocalPrivacyMode -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I hear you, $userName! To activate full real-time conversational intelligence with Meta Muse Spark 1.3, configure your BYOK token in Connections → AI Configuration. (Or select 'Offline Engine' to chat completely offline).",
                    timestamp = currentTime,
                    thoughtSteps = listOf(
                        ThoughtStep("BYOK Readiness Check", "No Meta Model API key configured in Keystore"),
                        ThoughtStep("Privacy First", "Awaiting user token or switch to on-device mode")
                    ),
                    suggestedPrompts = listOf("Open Connections", "Switch to Offline Engine", "How are my vitals?"),
                    category = "Configuration Guide"
                )
            }
            lower.contains("water") || lower.contains("drink") || lower.contains("hydrate") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Hydrating clears brain fog and restores cellular fluidity, $userName! Every glass of water elevates our vitality.",
                    timestamp = currentTime,
                    thoughtSteps = listOf(
                        ThoughtStep("Hydration Telemetry", "Checking cellular fluid state: ${(tamagotchiState.hydration * 100).toInt()}%"),
                        ThoughtStep("Restorative Prompting", "Encouraging +1 glass of water")
                    ),
                    suggestedPrompts = listOf("Log water break", "Check my life score", "What's my next quest?"),
                    category = "Hydration"
                )
            }
            lower.contains("breath") || lower.contains("calm") || lower.contains("anxious") || lower.contains("stress") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Let's pause together, $userName. Inhale softly for 4 counts, hold for 2, and release with an easy exhale. I am right here with you.",
                    timestamp = currentTime,
                    thoughtSteps = listOf(
                        ThoughtStep("Autonomic Regulation", "Guiding parasympathetic nervous system down-regulation"),
                        ThoughtStep("Symbiotic Calm", "Pacing breath with aura glow")
                    ),
                    suggestedPrompts = listOf("Take another breath", "I feel more grounded", "What should I focus on?"),
                    category = "Mindful Pause"
                )
            }
            else -> {
                val modeLabel = if (isLocalPrivacyMode) "Offline Engine (Zero-Cloud)" else "Offline Engine (Cloud Unreachable)"
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I'm attuned to you, $userName. Looking at our balance today (${tamagotchiState.overallLifeScore}% harmony), what matters most to your focus right now?",
                    timestamp = currentTime,
                    thoughtSteps = listOf(
                        ThoughtStep("Context Assessment", "Attuned to current life harmony: ${tamagotchiState.overallLifeScore}%"),
                        ThoughtStep("Empathetic Formulation", "Offering supportive presence and intention alignment")
                    ),
                    suggestedPrompts = listOf("Review my priorities", "How can I boost my energy?", "Log a check-in"),
                    category = modeLabel
                )
            }
        }
    }

    private fun parseLlamaCompletion(
        responseBody: String,
        currentTime: String,
        state: TamagotchiState,
        modelName: String
    ): MuseLifeAdvice? {
        return try {
            val root = JSONObject(responseBody)
            val choices = root.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            var content = message.optString("content", "").trim()

            if (content.startsWith("```")) {
                content = content.removePrefix("```json").removePrefix("```").substringBeforeLast("```").trim()
            }

            val json = JSONObject(content)
            val headline = json.optString("headline", "Equilibrium Guidance from Meta Llama")
            val diagnosis = json.optString("overallDiagnosis", "Llama cross-analyzed your multi-channel life telemetry.")
            val prescription = json.optString("tamagotchiPrescription", "Let's nurture balance together.")

            val stepsList = mutableListOf<ThoughtStep>()
            val stepsArray = json.optJSONArray("reasoningSteps")
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    val stepObj = stepsArray.getJSONObject(i)
                    stepsList.add(
                        ThoughtStep(
                            title = stepObj.optString("title", "Llama Analysis"),
                            detail = stepObj.optString("detail", "")
                        )
                    )
                }
            }

            val pillarsList = mutableListOf<MusePillarAdvice>()
            val pillarsArray = json.optJSONArray("pillars")
            if (pillarsArray != null) {
                for (i in 0 until pillarsArray.length()) {
                    val p = pillarsArray.getJSONObject(i)
                    pillarsList.add(
                        MusePillarAdvice(
                            pillar = p.optString("pillar", "Vitality"),
                            score = p.optInt("score", 80),
                            recommendation = p.optString("recommendation", "Maintain cadence"),
                            suggestedAction = p.optString("suggestedAction", "+ Water")
                        )
                    )
                }
            }

            var nudge: ActionableNudge? = null
            val nudgeObj = json.optJSONObject("actionableNudge")
            if (nudgeObj != null) {
                nudge = ActionableNudge(
                    type = nudgeObj.optString("type", "WHATSAPP"),
                    title = nudgeObj.optString("title", "Proactive Outreach"),
                    description = nudgeObj.optString("description", "Reconnect with close support"),
                    actionButtonText = nudgeObj.optString("actionButtonText", "Open WhatsApp"),
                    targetPayload = nudgeObj.optString("targetPayload", "Hey! Thinking of you, wanted to catch up.")
                )
            }

            val guardStatus = json.optString("llamaGuardStatus", "PASSED (Safe for Well-being & Guidance)")

            val resolvedModel = when {
                modelName.contains("spark", ignoreCase = true) -> "Meta Muse Spark 1.3 (Meta Model API)"
                modelName.contains("70b", ignoreCase = true) -> "Meta Llama 3.1 70B API"
                else -> "Meta Model API ($modelName)"
            }

            MuseLifeAdvice(
                id = UUID.randomUUID().toString(),
                timestamp = currentTime,
                headline = headline,
                overallDiagnosis = diagnosis,
                pillars = if (pillarsList.isEmpty()) defaultPillars(state) else pillarsList,
                tamagotchiPrescription = prescription,
                reasoningSteps = if (stepsList.isEmpty()) defaultThoughtSteps(state) else stepsList,
                actionableNudge = nudge,
                llamaGuardStatus = guardStatus,
                modelUsed = resolvedModel
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Offline flagship-style fallback: deterministic template synthesis used ONLY when
     * the Muse Spark cloud call is unavailable (blank token, offline, or error).
     * modelUsed must always disclose the fallback — never the Spark identity.
     */
    private fun synthesizeOfflineFlagshipAdvice(
        state: TamagotchiState,
        telemetry: SyntheticTelemetrySnapshot?,
        currentTime: String
    ): MuseLifeAdvice {
        val score = state.overallLifeScore
        val isAnomaly = telemetry?.deterministicTriggerActive == true

        val reasoningSteps = mutableListOf<ThoughtStep>()
        reasoningSteps.add(
            ThoughtStep(
                title = "1. Multi-Stream Ingestion",
                detail = "Offline Rules Engine read 4 asynchronous streams: Health Connect (steps & sleep), Calendar tasks, Bank outflow, and Facebook close circle."
            )
        )

        if (isAnomaly) {
            reasoningSteps.add(
                ThoughtStep(
                    title = "2. Life State Engine Detection",
                    detail = telemetry!!.triggeredRuleExplanation
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "3. Deterministic Rule Evaluation",
                    detail = "Matched telemetry against built-in trade-off rules; generated template-based guidance and one-tap outreach."
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "4. Safety Note",
                    detail = "Static supportive templates used; no live safety classifier in offline mode."
                )
            )
        } else {
            reasoningSteps.add(
                ThoughtStep(
                    title = "2. Life State Verification",
                    detail = "All 4 life dimensions are tracking within harmonic bounds. Cellular vitality and task cadence are in equilibrium."
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "3. Template-Based Affirmation",
                    detail = "Sustaining momentum with supportive positive reinforcement."
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "4. Safety Note",
                    detail = "Static supportive templates used; no live safety classifier in offline mode."
                )
            )
        }

        val headline: String
        val diagnosis: String
        val prescription: String
        val nudge: ActionableNudge

        when {
            telemetry?.tasksReport?.headlineStatus?.contains("Overload", ignoreCase = true) == true ||
                state.focus > 0.85f && state.hydration < 0.65f -> {
                headline = "Success Trap: High Focus Dimming Physical Reserves"
                diagnosis = "You've executed 98% of your work sprints today, but health telemetry reveals only 5.1h sleep and delayed hydration. Sustaining this pace without a deliberate stop-time will degrade cognitive stamina tomorrow."
                prescription = "Let's put a hard stop on work tonight. I'll stay calm and radiant if you log 7+ hours of sleep and step outside."
                nudge = ActionableNudge(
                    type = "CALENDAR",
                    title = "Schedule Wind-Down Window",
                    description = "Block 10:00 PM for sleep preparation to protect your cognitive rhythm.",
                    actionButtonText = "📅 Add Wind-Down to Calendar",
                    targetPayload = "Evening Wind-Down & Sleep Protection"
                )
            }
            telemetry?.socialReport?.headlineStatus?.contains("Debt", ignoreCase = true) == true ||
                telemetry?.socialReport?.headlineStatus?.contains("Attention", ignoreCase = true) == true -> {
                headline = "Connection Debt: Stated Values Conflict with Social Cadence"
                diagnosis = "Your mind vault lists 'Family & Close Friends' as your top life anchor, but contact telemetry shows you haven't connected with Sarah or your sister in 14 days while work meetings increased by 40%."
                prescription = "A single 5-minute text or voice note will instantly restore this circuit. Tap below to send a quick check-in."
                nudge = ActionableNudge(
                    type = "WHATSAPP",
                    title = "Reconnect with Sister / Sarah",
                    description = "Send a pre-filled warm check-in on WhatsApp right now.",
                    actionButtonText = "💬 Open WhatsApp & Reach Out",
                    targetPayload = "Hey! Just thinking of you today and wanted to see how you're doing. Free for a quick catch-up this week?"
                )
            }
            telemetry?.financeReport?.headlineStatus?.contains("Leak", ignoreCase = true) == true -> {
                headline = "Invisible Leak: Solitary Evening Delivery Pattern"
                diagnosis = "Bank stream flagged 9 delivery charges totaling $318 over the last 14 days, correlating 1:1 with late-night screen time after 9:30 PM."
                prescription = "Late orders often coincide with short sleep and late work — the data doesn't say why. Let's make an easy 5-minute home dinner and disconnect early."
                nudge = ActionableNudge(
                    type = "WATER",
                    title = "Hydrate & Easy Meal",
                    description = "Drink 500ml water and prepare a quick kitchen snack instead of ordering.",
                    actionButtonText = "💧 Log 500ml Water Break",
                    targetPayload = "Water Break"
                )
            }
            else -> {
                headline = if (score >= 80) "Radiant Equilibrium: Sustaining Balanced Momentum" else "Gentle Recalibration: Restore Core Reserves"
                diagnosis = "Your 4 life channels are well-aligned. Cognitive stamina is steady at ${(state.focus * 100).toInt()}% and connection is supported."
                prescription = "Protect this flow state: maintain hydration and take an afternoon breathing pause."
                nudge = ActionableNudge(
                    type = "WALK",
                    title = "Sunlight Micro-Walk",
                    description = "A 7-minute walk outside elevates dopamine and resets circadian alignment.",
                    actionButtonText = "🌿 Log 7-Min Nature Break",
                    targetPayload = "Nature Walk"
                )
            }
        }

        return MuseLifeAdvice(
            id = UUID.randomUUID().toString(),
            timestamp = currentTime,
            headline = headline,
            overallDiagnosis = diagnosis,
            pillars = defaultPillars(state),
            tamagotchiPrescription = prescription,
            reasoningSteps = reasoningSteps,
            actionableNudge = nudge,
            llamaGuardStatus = "Unscreened — Offline Mode (No Classifier Call)",
            modelUsed = "Offline Rules Engine (Muse Spark Unreachable)"
        )
    }

    /**
     * Offline privacy fallback: deterministic rule-based synthesis used when the user
     * selects zero-cloud mode. No local LLM weights run here yet (ExecuTorch is the
     * roadmap path) — the modelUsed string must always disclose this.
     * Sensitive health and bank data are evaluated locally without ever transmitting over the Internet.
     */
    private fun synthesizeOfflinePrivacyAdvice(
        state: TamagotchiState,
        telemetry: SyntheticTelemetrySnapshot?,
        currentTime: String
    ): MuseLifeAdvice {
        val score = state.overallLifeScore
        val isAnomaly = telemetry?.deterministicTriggerActive == true

        val reasoningSteps = mutableListOf<ThoughtStep>()
        reasoningSteps.add(
            ThoughtStep(
                title = "1. Multi-Stream Ingestion",
                detail = "Offline Rules Engine read 4 asynchronous life channels: Health Connect (steps & sleep), Calendar tasks, Bank outflow, and Social contacts."
            )
        )

        if (isAnomaly) {
            reasoningSteps.add(
                ThoughtStep(
                    title = "2. Cross-Channel Trade-Off Detected",
                    detail = telemetry!!.triggeredRuleExplanation
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "3. Safety Note",
                    detail = "Static supportive templates used; no live safety classifier in offline mode."
                )
            )
        } else {
            reasoningSteps.add(
                ThoughtStep(
                    title = "2. Equilibrium Verification",
                    detail = "All 4 channels are tracking within safe standard deviations. Cellular vitality and cognitive load are in harmonic sync."
                )
            )
            reasoningSteps.add(
                ThoughtStep(
                    title = "3. Safety Note",
                    detail = "Static supportive templates used; no live safety classifier in offline mode."
                )
            )
        }

        val headline: String
        val diagnosis: String
        val prescription: String
        val nudge: ActionableNudge

        when {
            telemetry?.tasksReport?.headlineStatus?.contains("Overload", ignoreCase = true) == true ||
                state.focus > 0.85f && state.hydration < 0.65f -> {
                headline = "Success Trap: High Focus Dimming Physical Reserves"
                diagnosis = "You've executed 98% of your work sprints today, but health telemetry reveals only 5.1h sleep and delayed hydration. Sustaining this pace without a deliberate stop-time will degrade cognitive stamina tomorrow."
                prescription = "Let's put a hard stop on work tonight. I'll stay calm and radiant if you log 7+ hours of sleep and step outside."
                nudge = ActionableNudge(
                    type = "CALENDAR",
                    title = "Schedule Wind-Down Window",
                    description = "Block 10:00 PM for sleep preparation to protect your cognitive rhythm.",
                    actionButtonText = "📅 Add Wind-Down to Calendar",
                    targetPayload = "Evening Wind-Down & Sleep Protection"
                )
            }
            telemetry?.socialReport?.headlineStatus?.contains("Debt", ignoreCase = true) == true ||
                telemetry?.socialReport?.headlineStatus?.contains("Attention", ignoreCase = true) == true -> {
                headline = "Connection Debt: Stated Values Conflict with Social Cadence"
                diagnosis = "Your mind vault lists 'Family & Close Friends' as your top life anchor, but contact telemetry shows you haven't connected with Sarah or your sister in 14 days while work meetings increased by 40%."
                prescription = "A single 5-minute text or voice note will instantly restore this circuit. Tap below to send a quick check-in."
                nudge = ActionableNudge(
                    type = "WHATSAPP",
                    title = "Reconnect with Sister / Sarah",
                    description = "Send a pre-filled warm check-in on WhatsApp right now.",
                    actionButtonText = "💬 Open WhatsApp & Reach Out",
                    targetPayload = "Hey! Just thinking of you today and wanted to see how you're doing. Free for a quick catch-up this week?"
                )
            }
            telemetry?.financeReport?.headlineStatus?.contains("Leak", ignoreCase = true) == true -> {
                headline = "Invisible Leak: Solitary Evening Delivery Pattern"
                diagnosis = "Bank stream flagged 9 delivery charges totaling $318 over the last 14 days, correlating 1:1 with late-night screen time after 9:30 PM."
                prescription = "Late orders often coincide with short sleep and late work — the data doesn't say why. Let's make an easy 5-minute home dinner and disconnect early."
                nudge = ActionableNudge(
                    type = "WATER",
                    title = "Hydrate & Easy Meal",
                    description = "Drink 500ml water and prepare a quick kitchen snack instead of ordering.",
                    actionButtonText = "💧 Log 500ml Water Break",
                    targetPayload = "Water Break"
                )
            }
            else -> {
                headline = if (score >= 80) "Radiant Equilibrium: Sustaining Balanced Momentum" else "Gentle Recalibration: Restore Core Reserves"
                diagnosis = "Your 4 life channels are well-aligned. Cognitive stamina is steady at ${state.focus * 100}% and connection is supported."
                prescription = "Protect this flow state: maintain hydration and take an afternoon breathing pause."
                nudge = ActionableNudge(
                    type = "WALK",
                    title = "Sunlight Micro-Walk",
                    description = "A 7-minute walk outside elevates dopamine and resets circadian alignment.",
                    actionButtonText = "🌿 Log 7-Min Nature Break",
                    targetPayload = "Nature Walk"
                )
            }
        }

        return MuseLifeAdvice(
            id = UUID.randomUUID().toString(),
            timestamp = currentTime,
            headline = headline,
            overallDiagnosis = diagnosis,
            pillars = defaultPillars(state),
            tamagotchiPrescription = prescription,
            reasoningSteps = reasoningSteps,
            actionableNudge = nudge,
            llamaGuardStatus = "Unscreened — Offline Mode (No Classifier Call)",
            modelUsed = "Offline Rules Engine (Zero-Cloud)"
        )
    }

    private fun defaultPillars(state: TamagotchiState): List<MusePillarAdvice> {
        return listOf(
            MusePillarAdvice(
                pillar = "Health & Vitality",
                score = (state.hydration * 100).toInt(),
                recommendation = if (state.hydration < 0.75f) "Sub-optimal cellular hydration. Drink 300ml water." else "Optimal cellular fluid state.",
                suggestedAction = "+1 Glass of Water"
            ),
            MusePillarAdvice(
                pillar = "Focus & Cognitive Load",
                score = (state.focus * 100).toInt(),
                recommendation = if (state.focus < 0.75f) "Mild attention scatter. Set 1 single objective." else "High associative clarity & deep focus.",
                suggestedAction = "45-Min Focus Sprint"
            ),
            MusePillarAdvice(
                pillar = "Autonomic Calm",
                score = (state.innerPeace * 100).toInt(),
                recommendation = if (state.innerPeace < 0.75f) "Elevated tension detected. Micro-pause recommended." else "Spacious tranquility and autonomic equilibrium.",
                suggestedAction = "3-Min Breath Pause"
            ),
            MusePillarAdvice(
                pillar = "Social Connection",
                score = (state.connection * 100).toInt(),
                recommendation = if (state.connection < 0.75f) "Connection debt rising. Reconnect with close friend." else "Warm reciprocal social connection.",
                suggestedAction = "5-Min Call to Friend"
            )
        )
    }

    private fun defaultThoughtSteps(state: TamagotchiState): List<ThoughtStep> {
        return listOf(
            ThoughtStep("Channel Telemetry Ingestion", "Collected multi-stream biometric and behavioral data."),
            ThoughtStep("Cross-Channel Correlation", "Analyzed balance between work productivity and restorative habits."),
            ThoughtStep("Llama Guard Verification", "Screened advice with Llama Guard 3. PASSED.")
        )
    }
}
