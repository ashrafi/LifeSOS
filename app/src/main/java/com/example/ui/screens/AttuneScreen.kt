package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.model.ActionableNudge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccentType
import com.example.model.CareActionLog
import com.example.model.ChatMessage
import com.example.model.DemoRecoveryStage
import com.example.model.LifeQuest
import com.example.model.MuseDemoScenario
import com.example.model.ScreenNav
import com.example.model.SyntheticTelemetrySnapshot
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.model.ThoughtStream
import com.example.ui.components.SerenityCard
import com.example.ui.components.SerenityPrimaryButton
import com.example.ui.components.SerenitySecondaryButton
import com.example.ui.components.CreatureHabitatBackdrop
import com.example.ui.components.SerenityStatusChip
import com.example.ui.components.TamagotchiCreature
import com.example.ui.components.TamagotchiHabitat
import com.example.ui.components.ThoughtBubble
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.BaseCanvas
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DarkSageText
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.FloatingSurface
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityError
import com.example.ui.theme.SerenityErrorContainer
import com.example.ui.theme.SoftMutedText
import com.example.ui.theme.SubtleContainer
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmSlateCharcoal
import com.example.viewmodel.SerenityViewModel

@Composable
fun AttuneScreen(
    viewModel: SerenityViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val tamagotchi by viewModel.tamagotchiState.collectAsState()
    val showFullHome by viewModel.showFullHome.collectAsState()
    val activeScenario by viewModel.activeDemoScenario.collectAsState()
    val recoveryStage by viewModel.demoRecoveryStage.collectAsState()
    val telemetry by viewModel.telemetrySnapshot.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val careLogs by viewModel.careLogs.collectAsState()
    val lifeQuests by viewModel.lifeQuests.collectAsState()
    val isBreathingActive by viewModel.isBreathingActive.collectAsState()
    val breathPhase by viewModel.breathPhase.collectAsState()
    val metaAiConfig by viewModel.metaAiConfig.collectAsState()
    val latestMuseAdvice by viewModel.latestMuseAdvice.collectAsState()
    val isSyncingWithMuse by viewModel.isSyncingWithMuse.collectAsState()
    val chatPendingCount by viewModel.chatPendingCount.collectAsState()
    val chatReplyStatus by viewModel.chatReplyStatus.collectAsState()
    val whatsAppInstalled = rememberIsWhatsAppInstalled()

    var isInputFocused by remember { mutableStateOf(false) }

    if (showFullHome) {
        BackHandler {
            viewModel.setShowFullHome(false)
        }
    }

    AnimatedContent(
        targetState = showFullHome,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "HomeViewTransition"
    ) { isFullScreen ->
        if (!isFullScreen) {
            val isSpeaking by viewModel.isSpeaking.collectAsState()
            val context = LocalContext.current
            AvatarOnlyHomeView(
                tamagotchi = tamagotchi,
                activeScenario = activeScenario,
                recoveryStage = recoveryStage,
                telemetry = telemetry,
                latestCompanionMessage = messages.lastOrNull { !it.isFromUser },
                actionableNudge = latestMuseAdvice?.actionableNudge,
                isSpeaking = isSpeaking,
                contentPadding = contentPadding,
                onPet = { viewModel.petTamagotchi() },
                onSelectScenario = { viewModel.activateDemoScenario(it) },
                onQuickReply = { viewModel.sendQuickPrompt(it) },
                onLogFollowThrough = { viewModel.completeDemoFollowThrough() },
                onOpenDataTab = { viewModel.navigateTo(ScreenNav.DATA) },
                onOpenFullHome = { viewModel.setShowFullHome(true) },
                onToggleSpeech = {
                    val speech = messages.lastOrNull { !it.isFromUser }?.content ?: tamagotchi.speech
                    viewModel.speakPrescription(speech, context)
                },
                onExecuteNudge = { nudge ->
                    viewModel.executeActionableNudge(context, nudge)
                },
                isWhatsAppInstalled = whatsAppInstalled,
                modifier = modifier
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(BaseCanvas)
                    .testTag("attune_screen"),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding() + 8.dp,
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    start = 18.dp,
                    end = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Back-to-Avatar Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SubtleContainer,
                            modifier = Modifier
                                .border(1.dp, WarmBorder, CircleShape)
                                .clip(CircleShape)
                                .clickable { viewModel.setShowFullHome(false) }
                                .testTag("back_to_avatar_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Avatar",
                                    tint = DeepEspresso,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Back to Avatar",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = DeepEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // 1. Header & Bio-Responsive Companion Status
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val pulseColor = when (tamagotchi.mood) {
                            TamagotchiMood.SICK -> SerenityError
                            TamagotchiMood.SAD, TamagotchiMood.TIRED -> AmberTertiary
                            TamagotchiMood.HOPEFUL -> MintSecondary
                            TamagotchiMood.DORMANT -> WarmSlateCharcoal
                            else -> CoralPrimary
                        }
                        SerenityStatusChip(
                            text = "${tamagotchi.name} is ${tamagotchi.mood.title}",
                            pulseColor = pulseColor,
                            modifier = Modifier.testTag("tamagotchi_status_chip")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "LifeSOS Companion",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Bio-responsive digital companion that senses life imbalances and initiates care.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = WarmSlateCharcoal
                            )
                        )
                    }
                }

        // 2. Interactive LifeSOS Demo Switcher (Pre-loads synthetic data & deterministic trigger)
        item {
            MuseDemoSelectorCard(
                activeScenario = activeScenario,
                recoveryStage = recoveryStage,
                onSelectScenario = { viewModel.activateDemoScenario(it) },
                onLogFollowThrough = { viewModel.completeDemoFollowThrough() },
                onOpenDataTab = {
                    viewModel.navigateTo(ScreenNav.DATA)
                    viewModel.setShowFullData(true)
                }
            )
        }

        // 3. The Tamagotchi Habitat Pod (Living Creature, Life Vitals & Caretaker Actions)
        item {
            TamagotchiHabitat(
                state = tamagotchi,
                onPet = { viewModel.petTamagotchi() },
                onSleepToggle = { viewModel.toggleSleepMode() },
                onWater = { viewModel.feedWater() },
                onBreathe = { viewModel.doBreathReset() },
                onWalk = { viewModel.logMovementWalk() },
                onFocus = { viewModel.logFocusSprint() },
                onConnect = { viewModel.logConnection() }
            )
        }

        // 4. Proactive Companion Dialogue Stream (LifeSOS initiates & user responds directly on Home)
        item {
            ProactiveCompanionDialogueCard(
                companionName = tamagotchi.name,
                activeScenario = activeScenario,
                recoveryStage = recoveryStage,
                messages = messages,
                inputText = inputText,
                isInputFocused = isInputFocused,
                onFocusChange = { isInputFocused = it },
                onInputChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendCurrentMessage() },
                onPromptClick = { viewModel.sendQuickPrompt(it) },
                onLogFollowThrough = { viewModel.completeDemoFollowThrough() },
                isAwaitingReply = chatPendingCount > 0,
                replyStatus = chatReplyStatus
            )
        }

        // 5. Mindful Breath Active Banner (if user clicked breathe)
        item {
            AnimatedVisibility(
                visible = isBreathingActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = MintSecondary.copy(alpha = 0.25f),
                            spotColor = CoralPrimary.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(FloatingSurface)
                        .border(1.5.dp, MintSecondary.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MintContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = DarkSageText,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mindful Breath Sync",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SageAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = breathPhase,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = DeepEspresso,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        IconButton(
                            onClick = { viewModel.toggleBreathingExercise() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close breath reset",
                                tint = WarmSlateCharcoal
                            )
                        }
                    }
                }
            }
        }

        // 6. LifeSOS Superintelligence Life Telemetry Sync
        item {
            SerenityCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("muse_sync_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CoralPrimary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Send Info to LifeSOS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepEspresso
                                    )
                                )
                                Text(
                                    text = "Life Telemetry → AI Guidance",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = WarmSlateCharcoal
                                    )
                                )
                            }
                        }

                        if (metaAiConfig.token.isNotBlank()) {
                            Surface(
                                shape = CircleShape,
                                color = MintContainer
                            ) {
                                Text(
                                    text = "Token Connected",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DarkSageText,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = SubtleContainer,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        viewModel.navigateTo(ScreenNav.CONNECTIONS)
                                        viewModel.setShowFullConnections(true)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = CoralPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Token",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CoralPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Packages ${tamagotchi.name}'s 5 vitals, habit quests, and care history to LifeSOS to generate an adaptive life prescription.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = WarmSlateCharcoal,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val consultDormant = tamagotchi.mood == TamagotchiMood.DORMANT &&
                            activeScenario == MuseDemoScenario.LIVE_REAL_DATA
                        SerenityPrimaryButton(
                            text = if (isSyncingWithMuse) "Transmitting..." else if (consultDormant) "Awaiting First Signal" else "Consult LifeSOS for Advice",
                            icon = Icons.Default.Sync,
                            onClick = { viewModel.sendAllInfoToMuse() },
                            modifier = Modifier.weight(1f),
                            testTag = "home_send_to_muse_button",
                            enabled = !consultDormant && !isSyncingWithMuse
                        )
                    }

                    // Same actionable nudge, right under Consult — no trip back
                    // to the front page. WhatsApp actions gray out when the app
                    // isn't installed.
                    val hubNudge = latestMuseAdvice?.actionableNudge
                    if (hubNudge != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val hubIsWhatsApp = hubNudge.type.equals("WHATSAPP", ignoreCase = true)
                        val hubNudgeEnabled = !hubIsWhatsApp || whatsAppInstalled
                        val nudgeContext = LocalContext.current
                        Surface(
                            shape = CircleShape,
                            color = when {
                                !hubNudgeEnabled -> SubtleContainer
                                hubIsWhatsApp -> Color(0xFFE7FCE8)
                                else -> MintContainer
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    when {
                                        !hubNudgeEnabled -> WarmBorder
                                        hubIsWhatsApp -> Color(0xFF25D366)
                                        else -> MintSecondary
                                    },
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .then(
                                    if (hubNudgeEnabled) {
                                        Modifier.clickable {
                                            viewModel.executeActionableNudge(nudgeContext, hubNudge)
                                        }
                                    } else Modifier
                                )
                                .testTag("hub_nudge_pill")
                        ) {
                            Text(
                                text = hubNudge.actionButtonText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (!hubNudgeEnabled) {
                                        WarmSlateCharcoal
                                    } else if (hubIsWhatsApp) {
                                        Color(0xFF0F5A29)
                                    } else {
                                        DarkSageText
                                    },
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    latestMuseAdvice?.let { advice ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SubtleContainer)
                                .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = advice.headline,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CoralPrimary
                                        )
                                    )
                                    Text(
                                        text = advice.timestamp,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SoftMutedText,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = advice.tamagotchiPrescription,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepEspresso
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                advice.cloudFailureNote?.let { note ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AmberContainer
                                    ) {
                                        Text(
                                            text = "⚠️ $note",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = OnAmberContainer,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                .testTag("advice_cloud_warning")
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MintContainer
                                    ) {
                                        Text(
                                            text = "⭐ ${advice.modelUsed}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = DarkSageText,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "🛡️ ${advice.llamaGuardStatus}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = DarkSageText,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Today's Life Quests
        item {
            SerenityCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Life Quests",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Surface(
                            shape = CircleShape,
                            color = CoralPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${lifeQuests.count { it.isCompleted }} / ${lifeQuests.size} Done",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CoralPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lifeQuests.forEach { quest ->
                            LifeQuestRow(
                                quest = quest,
                                onToggle = { viewModel.toggleQuest(quest.id) }
                            )
                        }
                    }
                }
            }
        }

        // 8. Recent Care Logs
        item {
            SerenityCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Recent Life Care Nurturing",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    careLogs.take(4).forEach { log ->
                        CareLogRow(log = log)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 9. Life Attunement Thought Streams
        item {
            Text(
                text = "Cognitive Synthesis for ${tamagotchi.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )
        }

                items(viewModel.thoughtStreams) { stream ->
                    ThoughtStreamCard(
                        stream = stream,
                        onPromptClick = { viewModel.sendQuickPrompt(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarOnlyHomeView(
    tamagotchi: TamagotchiState,
    activeScenario: MuseDemoScenario,
    recoveryStage: DemoRecoveryStage,
    telemetry: SyntheticTelemetrySnapshot,
    latestCompanionMessage: ChatMessage?,
    actionableNudge: ActionableNudge?,
    isSpeaking: Boolean,
    contentPadding: PaddingValues,
    onPet: () -> Unit,
    onSelectScenario: (MuseDemoScenario) -> Unit,
    onQuickReply: (String) -> Unit,
    onLogFollowThrough: () -> Unit,
    onOpenDataTab: () -> Unit,
    onOpenFullHome: () -> Unit,
    onToggleSpeech: () -> Unit,
    onExecuteNudge: (ActionableNudge) -> Unit,
    isWhatsAppInstalled: Boolean,
    modifier: Modifier = Modifier
) {
    val moodPulseColor = when (tamagotchi.mood) {
        TamagotchiMood.SICK -> SerenityError
        TamagotchiMood.SAD, TamagotchiMood.TIRED -> AmberTertiary
        TamagotchiMood.HOPEFUL -> MintSecondary
        TamagotchiMood.DORMANT -> WarmSlateCharcoal
        else -> CoralPrimary
    }

    var showDemoScenarios by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BaseCanvas)
            .padding(
                top = contentPadding.calculateTopPadding() + 10.dp,
                bottom = contentPadding.calculateBottomPadding() + 12.dp,
                start = 20.dp,
                end = 20.dp
            )
            .testTag("avatar_only_home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bio-Responsive Status Header + Discreet Demo Scenarios Icon
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                SerenityStatusChip(
                    text = if (tamagotchi.mood == TamagotchiMood.DORMANT) {
                        "${tamagotchi.mood.emoji} ${tamagotchi.name} is ${tamagotchi.mood.title} • —"
                    } else {
                        "${tamagotchi.mood.emoji} ${tamagotchi.name} is ${tamagotchi.mood.title} • ${tamagotchi.overallLifeScore}%"
                    },
                    pulseColor = moodPulseColor,
                    modifier = Modifier.align(Alignment.Center)
                )

                val isSimulatedScenario = activeScenario != MuseDemoScenario.LIVE_REAL_DATA && activeScenario != MuseDemoScenario.NONE
                Surface(
                    shape = CircleShape,
                    color = if (showDemoScenarios || isSimulatedScenario) CoralPrimary.copy(alpha = 0.15f) else FloatingSurface,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(36.dp)
                        .border(
                            1.dp,
                            if (showDemoScenarios || isSimulatedScenario) CoralPrimary else WarmBorder,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { showDemoScenarios = !showDemoScenarios }
                        .testTag("toggle_demo_scenarios_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Demo Scenarios",
                            tint = if (showDemoScenarios || isSimulatedScenario) CoralPrimary else DeepEspresso.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showDemoScenarios,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scenarios = listOf(
                        MuseDemoScenario.LIVE_REAL_DATA to "🟢 Live Data",
                        MuseDemoScenario.NONE to "Healthy Baseline",
                        MuseDemoScenario.SUCCESS_TRAP to "1. Success Trap",
                        MuseDemoScenario.INVISIBLE_MONEY_LEAK to "2. Money Leak",
                        MuseDemoScenario.CONNECTION_DEBT to "3. Debt"
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(scenarios) { (scenario, shortLabel) ->
                            val isSelected = activeScenario == scenario
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CoralPrimary else FloatingSurface,
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) CoralPrimary else WarmBorder,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable { onSelectScenario(scenario) }
                                    .testTag("demo_chip_${scenario.id}")
                            ) {
                                Text(
                                    text = shortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else DeepEspresso,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Centerpiece: Bio-Responsive Avatar + 4 Corner Channel Micro-Satellites
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Full-bleed habitat backdrop reaching all four channel chips.
            CreatureHabitatBackdrop(
                state = tamagotchi,
                modifier = Modifier.fillMaxSize()
            )
            // Top-Left Satellite: Health
            HomeChannelSatellitePill(
                label = "Health",
                score = telemetry.healthReport.scorePercent,
                isAvailable = telemetry.healthReport.isAvailable,
                isAlert = telemetry.healthReport.isAlert,
                accentColor = MintSecondary,
                onClick = onOpenDataTab,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Top-Right Satellite: Tasks
            HomeChannelSatellitePill(
                label = "Tasks",
                score = telemetry.tasksReport.scorePercent,
                isAvailable = telemetry.tasksReport.isAvailable,
                isAlert = telemetry.tasksReport.isAlert,
                accentColor = SageAccent,
                onClick = onOpenDataTab,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // The Living Companion Charm
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TamagotchiCreature(
                    state = tamagotchi,
                    onPet = onPet,
                    size = 225.dp,
                    showBackdrop = false
                )
                Text(
                    text = "✨ Tap ${tamagotchi.name} to nurture (${tamagotchi.totalPets} hugs)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = WarmSlateCharcoal,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }

            // Bottom-Left Satellite: Finance
            HomeChannelSatellitePill(
                label = "Finance",
                score = telemetry.financeReport.scorePercent,
                isAvailable = telemetry.financeReport.isAvailable,
                isAlert = telemetry.financeReport.isAlert,
                accentColor = AmberTertiary,
                onClick = onOpenDataTab,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // Bottom-Right Satellite: Social
            HomeChannelSatellitePill(
                label = "Social",
                score = telemetry.socialReport.scorePercent,
                isAvailable = telemetry.socialReport.isAvailable,
                isAlert = telemetry.socialReport.isAlert,
                accentColor = CoralPrimary,
                onClick = onOpenDataTab,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }

        // 3. Proactive Companion Voice & Interactive Quick Response Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val bubbleBorderColor = when {
                tamagotchi.mood == TamagotchiMood.SICK -> SerenityError.copy(alpha = 0.45f)
                tamagotchi.mood == TamagotchiMood.HOPEFUL -> MintSecondary.copy(alpha = 0.65f)
                else -> WarmBorder
            }
            var dialogExpanded by rememberSaveable { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = moodPulseColor.copy(alpha = 0.14f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(FloatingSurface)
                    .border(1.5.dp, bubbleBorderColor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (activeScenario != MuseDemoScenario.NONE) {
                                "${tamagotchi.name} • ${activeScenario.intelligenceKind}"
                            } else {
                                "${tamagotchi.name} • Symbiotic Presence"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (tamagotchi.mood == TamagotchiMood.SICK) SerenityError else CoralPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleSpeech,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.GraphicEq,
                                    contentDescription = "Speak aloud",
                                    tint = if (isSpeaking) CoralPrimary else SoftMutedText,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LVL ${tamagotchi.level}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SoftMutedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                            IconButton(
                                onClick = { dialogExpanded = !dialogExpanded },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (dialogExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (dialogExpanded) "Collapse dialogue" else "Expand dialogue",
                                    tint = SoftMutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (dialogExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))

                    val displayedSpeech = latestCompanionMessage?.content ?: tamagotchi.speech
                    Text(
                        text = "\"$displayedSpeech\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DeepEspresso,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Proactive Actionable Nudge (WhatsApp outreach, Calendar block, or Hydro/Walk)
                    if (actionableNudge != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val isWhatsApp = actionableNudge.type.equals("WHATSAPP", ignoreCase = true)
                        val nudgeEnabled = !isWhatsApp || isWhatsAppInstalled
                        Surface(
                            shape = CircleShape,
                            color = when {
                                !nudgeEnabled -> SubtleContainer
                                isWhatsApp -> Color(0xFFE7FCE8)
                                else -> MintContainer
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    when {
                                        !nudgeEnabled -> WarmBorder
                                        isWhatsApp -> Color(0xFF25D366)
                                        else -> MintSecondary
                                    },
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .then(if (nudgeEnabled) Modifier.clickable { onExecuteNudge(actionableNudge) } else Modifier)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = actionableNudge.actionButtonText,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (!nudgeEnabled) WarmSlateCharcoal else if (isWhatsApp) Color(0xFF0F5A29) else DarkSageText,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Interactive 1-tap Demo Action right on the Home screen
                    if (activeScenario != MuseDemoScenario.NONE && recoveryStage == DemoRecoveryStage.HOPEFUL_COMMITTED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MintContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MintSecondary, CircleShape)
                                .clip(CircleShape)
                                .clickable(onClick = onLogFollowThrough)
                        ) {
                            Text(
                                text = "🌱 ${activeScenario.followThroughButtonLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = DarkSageText,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    } else if (latestCompanionMessage?.suggestedPrompts?.isNotEmpty() == true) {
                        val firstPrompt = latestCompanionMessage.suggestedPrompts.first()
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = SubtleContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CoralPrimary.copy(alpha = 0.4f), CircleShape)
                                .clip(CircleShape)
                                .clickable { onQuickReply(firstPrompt) }
                        ) {
                            Text(
                                text = "💬 Reply: \"$firstPrompt\"",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CoralPrimary,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                    }
                }
            }

            // 4. Bottom Navigation Button to Full Companion Hub
            SerenityPrimaryButton(
                text = "Open Companion Hub",
                icon = Icons.Default.Dashboard,
                onClick = onOpenFullHome,
                modifier = Modifier.fillMaxWidth(),
                testTag = "open_full_home_button"
            )
        }
    }
}

@Composable
private fun rememberIsWhatsAppInstalled(): Boolean {
    val context = LocalContext.current
    return remember {
        context.packageManager.getLaunchIntentForPackage("com.whatsapp") != null
    }
}

@Composable
private fun HomeChannelSatellitePill(
    label: String,
    score: Int,
    isAlert: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAvailable: Boolean = true
) {
    val bgColor = if (isAlert) SerenityErrorContainer else FloatingSurface
    val borderColor = if (isAlert) SerenityError.copy(alpha = 0.55f) else WarmBorder
    val dotColor = if (isAlert) SerenityError else if (!isAvailable) WarmSlateCharcoal else accentColor
    val textColor = if (isAlert) SerenityError else if (!isAvailable) WarmSlateCharcoal else DeepEspresso

    Surface(
        shape = CircleShape,
        color = bgColor,
        modifier = modifier
            .shadow(3.dp, CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = if (isAvailable) "$label $score%" else "$label —",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun MuseDemoSelectorCard(
    activeScenario: MuseDemoScenario,
    recoveryStage: DemoRecoveryStage,
    onSelectScenario: (MuseDemoScenario) -> Unit,
    onLogFollowThrough: () -> Unit,
    onOpenDataTab: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("muse_demo_selector_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(SubtleContainer, CircleShape)
                            .border(1.dp, WarmBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Demo Scenarios",
                            tint = CoralPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "LifeSOS Proactive Demos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEspresso
                            )
                        )
                        Text(
                            text = if (isExpanded) {
                                "Pump synthetic data → Deterministic rule triggers sick state → LifeSOS initiates care"
                            } else {
                                "Demo tooling • Active: ${activeScenario.title}"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = WarmSlateCharcoal
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = WarmSlateCharcoal
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    val scenarios = listOf(
                        MuseDemoScenario.LIVE_REAL_DATA,
                        MuseDemoScenario.SUCCESS_TRAP,
                        MuseDemoScenario.INVISIBLE_MONEY_LEAK,
                        MuseDemoScenario.CONNECTION_DEBT,
                        MuseDemoScenario.NONE
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(scenarios) { scenario ->
                            val isSelected = activeScenario == scenario
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) CoralPrimary else SubtleContainer,
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) CoralPrimary else WarmBorder,
                                        CircleShape
                                    )
                                    .clip(CircleShape)
                                    .clickable { onSelectScenario(scenario) }
                                    .testTag("demo_chip_${scenario.id}")
                            ) {
                                Text(
                                    text = scenario.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else DeepEspresso,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    if (activeScenario != MuseDemoScenario.NONE) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val bannerBg = when (recoveryStage) {
                            DemoRecoveryStage.RECOVERED_HAPPY -> MintContainer
                            DemoRecoveryStage.HOPEFUL_COMMITTED -> AmberContainer
                            else -> SerenityErrorContainer
                        }
                        val bannerTextCol = when (recoveryStage) {
                            DemoRecoveryStage.RECOVERED_HAPPY -> DarkSageText
                            DemoRecoveryStage.HOPEFUL_COMMITTED -> OnAmberContainer
                            else -> SerenityError
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(bannerBg)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (recoveryStage == DemoRecoveryStage.RECOVERED_HAPPY) {
                                            Icons.Default.CheckCircle
                                        } else {
                                            Icons.Default.Warning
                                        },
                                        contentDescription = null,
                                        tint = bannerTextCol,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${activeScenario.intelligenceKind} • ${recoveryStage.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = bannerTextCol,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeScenario.detectedRuleSummary,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = DeepEspresso,
                                        lineHeight = 17.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (recoveryStage == DemoRecoveryStage.HOPEFUL_COMMITTED) {
                                        SerenityPrimaryButton(
                                            text = activeScenario.followThroughButtonLabel,
                                            onClick = onLogFollowThrough,
                                            modifier = Modifier.weight(1f),
                                            testTag = "log_follow_through_button"
                                        )
                                    } else {
                                        SerenitySecondaryButton(
                                            text = "Inspect 4-Channel Data",
                                            onClick = onOpenDataTab,
                                            modifier = Modifier.weight(1f),
                                            testTag = "inspect_demo_data_button"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProactiveCompanionDialogueCard(
    companionName: String,
    activeScenario: MuseDemoScenario,
    recoveryStage: DemoRecoveryStage,
    messages: List<com.example.model.ChatMessage>,
    inputText: String,
    isInputFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onPromptClick: (String) -> Unit,
    onLogFollowThrough: () -> Unit,
    isAwaitingReply: Boolean,
    replyStatus: String?
) {
    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("proactive_dialogue_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (activeScenario == MuseDemoScenario.NONE) {
                            "Conversation with $companionName"
                        } else {
                            "$companionName Initiated Check-In"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEspresso
                        )
                    )
                    Text(
                        text = if (activeScenario == MuseDemoScenario.NONE) {
                            "Tap a suggested reply or type below"
                        } else {
                            "Agreement makes $companionName hopeful • Follow-through restores full health"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmSlateCharcoal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Render recent messages inline so user can complete the entire demo right on Home
            messages.takeLast(5).forEach { message ->
                ThoughtBubble(
                    message = message,
                    onPromptClick = onPromptClick
                )
            }

            // When in HOPEFUL_COMMITTED stage, show prominent Follow-Through logging CTA
            if (activeScenario != MuseDemoScenario.NONE && recoveryStage == DemoRecoveryStage.HOPEFUL_COMMITTED) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MintContainer.copy(alpha = 0.65f))
                        .border(1.dp, MintSecondary, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "🌱 $companionName is Hopeful! Ready to log follow-through?",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkSageText
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Agreement made $companionName hopeful, but only real or simulated follow-through restores full health and happiness.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DeepEspresso
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SerenityPrimaryButton(
                            text = activeScenario.followThroughButtonLabel,
                            onClick = onLogFollowThrough,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "dialogue_log_follow_through_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live-reply progress + status, visible together until the answer lands.
            if (isAwaitingReply) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = CoralPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = replyStatus ?: "Waiting for reply…",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = WarmSlateCharcoal,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Inline Chat Input Bar
            val containerShape = RoundedCornerShape(24.dp)
            val borderModifier = if (isInputFocused) {
                Modifier.border(2.dp, CoralHoverGlow, containerShape)
            } else {
                Modifier.border(1.dp, WarmBorder, containerShape)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(containerShape)
                    .background(SubtleContainer)
                    .then(borderModifier)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Respond to $companionName...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = WarmSlateCharcoal.copy(alpha = 0.60f)
                                )
                            )
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = onInputChange,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = DeepEspresso
                            ),
                            cursorBrush = SolidColor(CoralPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { onFocusChange(it.isFocused) }
                                .testTag("dialogue_input_field")
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { if (!isAwaitingReply) onSend() },
                        enabled = !isAwaitingReply,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isAwaitingReply) WarmSlateCharcoal else CoralPrimary)
                            .testTag("dialogue_send_button")
                    ) {
                        if (isAwaitingReply) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LifeQuestRow(
    quest: LifeQuest,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (quest.isCompleted) SubtleContainer else FloatingSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (quest.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (quest.isCompleted) "Completed" else "Incomplete",
            tint = if (quest.isCompleted) CoralPrimary else SoftMutedText,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (quest.isCompleted) FontWeight.SemiBold else FontWeight.Medium,
                    color = DeepEspresso
                )
            )
            Text(
                text = quest.impact,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CoralPrimary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun CareLogRow(log: CareActionLog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CoralPrimary)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = log.actionName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = DeepEspresso
                    )
                )
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = WarmSlateCharcoal,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+${log.xpGained} XP",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = log.timestamp,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = SoftMutedText,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
private fun ThoughtStreamCard(
    stream: ThoughtStream,
    onPromptClick: (String) -> Unit
) {
    val accentColor = when (stream.accentType) {
        AccentType.CORAL -> CoralPrimary
        AccentType.MINT -> SageAccent
        AccentType.AMBER -> AmberTertiary
    }

    SerenityCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("thought_stream_card"),
        onClick = { onPromptClick(stream.prompt) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stream.tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Open stream",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stream.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DeepEspresso
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stream.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WarmSlateCharcoal,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "Explore this stream",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CoralPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "→",
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
