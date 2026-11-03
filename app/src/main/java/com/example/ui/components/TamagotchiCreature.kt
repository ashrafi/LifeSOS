package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CoralHoverGlow
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.DeepEspresso
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SageAccent
import com.example.ui.theme.SerenityError
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-bleed habitat backdrop for the front page: fills the whole centerpiece
 * so the background reaches all four channel chips. Mood-reactive sky with sun
 * when happy, drifting clouds when not, moon + stars while sleeping.
 */
@Composable
fun CreatureHabitatBackdrop(
    state: TamagotchiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HabitatBackdropIdle")
    val driftDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BackdropDrift"
    )
    val bgStars = remember {
        val r = kotlin.random.Random(7)
        List(18) { Pair(r.nextFloat(), r.nextFloat()) }
    }
    val bgPollen = remember {
        val r = kotlin.random.Random(21)
        List(10) { Pair(r.nextFloat(), r.nextFloat()) }
    }
    Canvas(modifier = modifier) {
        drawHabitatBackdrop(
            mood = state.mood,
            isSleeping = state.isSleeping,
            twinkleBase = Math.toRadians(driftDeg.toDouble()),
            stars = bgStars,
            pollen = bgPollen
        )
    }
}

/**
 * Shared habitat painter: mood-reactive sky, sun or clouds, twinkling stars /
 * drifting pollen, rolling hills, and a soft vignette. Used both as the
 * creature's own diorama and as the full-bleed front-page backdrop.
 */
private fun DrawScope.drawHabitatBackdrop(
    mood: TamagotchiMood,
    isSleeping: Boolean,
    twinkleBase: Double,
    stars: List<Pair<Float, Float>>,
    pollen: List<Pair<Float, Float>>
) {
    val canvasW = size.width
    val canvasH = size.height
    val isSunny = !isSleeping &&
        (mood == TamagotchiMood.RADIANT || mood == TamagotchiMood.HOPEFUL || mood == TamagotchiMood.BALANCED)
    val skyColors = when {
        isSleeping -> listOf(Color(0xFF2B3A67), Color(0xFF5A6FA8), Color(0xFF9AA8D0))
        mood == TamagotchiMood.DORMANT -> listOf(Color(0xFF8E8B86), Color(0xFFB9B5AE), Color(0xFFE3E0DA))
        mood == TamagotchiMood.SICK -> listOf(Color(0xFF9A97A3), Color(0xFFC4C1CC), Color(0xFFE8E6EC))
        mood == TamagotchiMood.SAD || mood == TamagotchiMood.TIRED ->
            listOf(Color(0xFF7E93B8), Color(0xFFA9BCD8), Color(0xFFDCE6F2))
        mood == TamagotchiMood.RADIANT -> listOf(Color(0xFF8FCBF5), Color(0xFFC4E7FA), Color(0xFFF0FAFF))
        mood == TamagotchiMood.HOPEFUL -> listOf(Color(0xFF9FE0C3), Color(0xFFCDF3DF), Color(0xFFF2FCF6))
        else -> listOf(Color(0xFFA8C6F0), Color(0xFFCFE2F8), Color(0xFFF2F7FE))
    }
    val hillBackColor = when {
        isSleeping -> Color(0xFF3D4C7A)
        mood == TamagotchiMood.DORMANT -> Color(0xFFA5A19A)
        mood == TamagotchiMood.SICK -> Color(0xFFB5B2BC)
        mood == TamagotchiMood.SAD || mood == TamagotchiMood.TIRED -> Color(0xFF93A7C4)
        mood == TamagotchiMood.RADIANT -> Color(0xFF8FD0A6)
        mood == TamagotchiMood.HOPEFUL -> Color(0xFF8FD8B4)
        else -> Color(0xFF9CC3E8)
    }
    val hillFrontColor = when {
        isSleeping -> Color(0xFF2E3B60)
        mood == TamagotchiMood.DORMANT -> Color(0xFF8B8781)
        mood == TamagotchiMood.SICK -> Color(0xFF9D9AA4)
        mood == TamagotchiMood.SAD || mood == TamagotchiMood.TIRED -> Color(0xFF7C92B5)
        mood == TamagotchiMood.RADIANT -> Color(0xFF6FBF8D)
        mood == TamagotchiMood.HOPEFUL -> Color(0xFF6FCBA0)
        else -> Color(0xFF84AEDD)
    }
    drawRect(
        brush = Brush.verticalGradient(skyColors),
        size = Size(canvasW, canvasH)
    )
    if (isSunny) {
        // Warm sun while happy.
        val sunCenter = Offset(canvasW * 0.78f, canvasH * 0.20f)
        drawCircle(color = Color(0xFFFFD66B).copy(alpha = 0.25f), radius = canvasW * 0.11f, center = sunCenter)
        drawCircle(color = Color(0xFFFFD66B), radius = canvasW * 0.062f, center = sunCenter)
    } else if (isSleeping) {
        // Cratered moon on the night sky.
        val moonCenter = Offset(canvasW * 0.78f, canvasH * 0.20f)
        drawCircle(color = Color(0xFFF4F1E4).copy(alpha = 0.25f), radius = canvasW * 0.11f, center = moonCenter)
        drawCircle(color = Color(0xFFF4F1E4), radius = canvasW * 0.062f, center = moonCenter)
        drawCircle(
            color = Color(0xFFD8D4C4), radius = canvasW * 0.014f,
            center = moonCenter + Offset(-canvasW * 0.02f, -canvasH * 0.012f)
        )
        drawCircle(
            color = Color(0xFFD8D4C4), radius = canvasW * 0.010f,
            center = moonCenter + Offset(canvasW * 0.018f, canvasH * 0.016f)
        )
    } else {
        // Puffy drifting clouds replace the sun whenever Lumi isn't happy.
        val cloudColor = Color(0xFFF4F1EB).copy(alpha = 0.94f)
        val shadeColor = Color(0xFFD9D4CA).copy(alpha = 0.7f)
        fun drawCloud(center: Offset, r: Float) {
            drawCircle(cloudColor, r, center + Offset(-r * 1.1f, r * 0.35f))
            drawCircle(cloudColor, r * 1.25f, center + Offset(-r * 0.1f, 0f))
            drawCircle(cloudColor, r * 0.9f, center + Offset(r * 0.95f, r * 0.3f))
            drawOval(
                cloudColor,
                topLeft = Offset(center.x - r * 2f, center.y - r * 0.1f),
                size = Size(r * 4f, r * 1.2f)
            )
            drawOval(
                shadeColor,
                topLeft = Offset(center.x - r * 1.7f, center.y + r * 0.35f),
                size = Size(r * 3.4f, r * 0.6f)
            )
        }
        val drift1 = sin(twinkleBase + 1.0).toFloat() * canvasW * 0.02f
        val drift2 = sin(twinkleBase + 3.5).toFloat() * canvasW * 0.02f
        drawCloud(Offset(canvasW * 0.24f + drift1, canvasH * 0.20f), canvasW * 0.055f)
        drawCloud(Offset(canvasW * 0.70f + drift2, canvasH * 0.33f), canvasW * 0.042f)
    }
    // Twinkling stars at night, drifting pollen on happy days.
    if (isSleeping) {
        stars.forEachIndexed { idx, (fx, fy) ->
            val alpha = 0.35f + 0.35f * sin(twinkleBase * 2 + idx).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = alpha.coerceIn(0.1f, 0.8f)),
                radius = (1f + (idx % 3) * 0.5f).dp.toPx(),
                center = Offset(fx * canvasW, fy * canvasH * 0.55f)
            )
        }
    } else if (isSunny) {
        pollen.forEachIndexed { idx, (fx, fy) ->
            val drift = sin(twinkleBase + idx).toFloat() * 4.dp.toPx()
            drawCircle(
                color = Color(0xFFFFF6D8).copy(alpha = 0.55f),
                radius = 1.6.dp.toPx(),
                center = Offset(fx * canvasW + drift, fy * canvasH * 0.6f)
            )
        }
    }
    // Rolling hills for Lumi to sit on.
    drawOval(
        color = hillBackColor,
        topLeft = Offset(-canvasW * 0.35f, canvasH * 0.72f),
        size = Size(canvasW * 1.1f, canvasH * 0.42f)
    )
    drawOval(
        color = hillFrontColor,
        topLeft = Offset(canvasW * 0.25f, canvasH * 0.80f),
        size = Size(canvasW * 1.1f, canvasH * 0.42f)
    )
    // Soft vignette for depth.
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.10f)),
            center = Offset(canvasW / 2f, canvasH / 2f),
            radius = canvasW * 0.72f
        ),
        size = Size(canvasW, canvasH)
    )
}

@Composable
fun TamagotchiCreature(
    state: TamagotchiState,
    onPet: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    showBackdrop: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TamagotchiIdle")

    val isSickOrSad = state.mood == TamagotchiMood.SICK ||
        state.mood == TamagotchiMood.SAD ||
        state.mood == TamagotchiMood.TIRED
    val isDormant = state.mood == TamagotchiMood.DORMANT

    val isHopeful = state.mood == TamagotchiMood.HOPEFUL

    // Slow, subdued motion when sick/sad; lively floating motion when healthy/hopeful
    val floatAmplitude = when {
        state.mood == TamagotchiMood.SICK -> 2.5f
        state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> 4f
        isHopeful -> 8f
        else -> 11f
    }

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -floatAmplitude,
        targetValue = floatAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    // Gentle aura breathing scale (weak when sick)
    val auraScale by infiniteTransition.animateFloat(
        initialValue = if (isSickOrSad) 0.82f else 0.92f,
        targetValue = if (isSickOrSad) 0.90f else 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraScale"
    )

    // Orbital bio-resonance angle for living satellite motes
    val orbitAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSickOrSad) 18000 else 9500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitAngle"
    )

    // Fast groove angle for periodic dance breaks
    val danceAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DanceAngle"
    )

    // Periodic natural blinking
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((3000..5000).random().toLong())
            isBlinking = true
            delay(160)
            isBlinking = false
        }
    }

    // Spontaneous dance breaks (healthy moods only; stops at once if mood sours)
    var isDancing by remember { mutableStateOf(false) }
    LaunchedEffect(isSickOrSad, isDormant, state.isSleeping) {
        var firstWait = true
        while (true) {
            delay(if (firstWait) (9000L..14000L).random() else (22000L..38000L).random())
            firstWait = false
            if (!isSickOrSad && !isDormant && !state.isSleeping) {
                isDancing = true
                delay(3600)
                isDancing = false
            }
        }
    }

    // Touch petting bounce reaction
    var petBounceTrigger by remember { mutableFloatStateOf(1f) }
    val animatedBounceScale by animateFloatAsState(
        targetValue = petBounceTrigger,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f),
        finishedListener = { petBounceTrigger = 1f },
        label = "PetBounce"
    )

    // Deterministic background sparkle positions (seeded once so nothing flickers).
    val bgStars = remember {
        val r = kotlin.random.Random(7)
        List(18) { Pair(r.nextFloat(), r.nextFloat()) }
    }
    val bgPollen = remember {
        val r = kotlin.random.Random(21)
        List(10) { Pair(r.nextFloat(), r.nextFloat()) }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .testTag("tamagotchi_creature")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    petBounceTrigger = 1.22f
                    onPet()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
                .then(if (showBackdrop) Modifier.clip(RoundedCornerShape(28.dp)) else Modifier)
        ) {
            val canvasW = this.size.width
            val canvasH = this.size.height
            val baseCenterX = canvasW / 2f
            var centerX = baseCenterX
            var centerY = (canvasH / 2f) + floatOffset

            // 0. Habitat Diorama Background (shared painter; the front page
            // runs it full-bleed instead via CreatureHabitatBackdrop).
            if (showBackdrop) {
                drawHabitatBackdrop(
                    mood = state.mood,
                    isSleeping = state.isSleeping,
                    twinkleBase = Math.toRadians(orbitAngleDeg.toDouble()),
                    stars = bgStars,
                    pollen = bgPollen
                )
            }

            // Dance groove: side-to-side sway + little hops (healthy moods only)
            val dancePhaseRad = Math.toRadians(danceAngleDeg.toDouble())
            val danceDX = if (isDancing) sin(dancePhaseRad).toFloat() * 5.dp.toPx() else 0f
            val danceHop = if (isDancing) -abs(sin(dancePhaseRad).toFloat()) * 5.dp.toPx() else 0f

            // 1. Aura Halo Bloom (Reflecting bio-responsive state)
            val lifeScore = state.overallLifeScore
            val auraColor = when {
                state.isSleeping -> Color(0xFFCCC2DC)
                state.mood == TamagotchiMood.SICK -> Color(0xFF9E9B95)
                state.mood == TamagotchiMood.DORMANT -> Color(0xFFA8A5A0)
                state.mood == TamagotchiMood.SAD -> Color(0xFF8DA3B8)
                state.mood == TamagotchiMood.TIRED -> Color(0xFFB39F93)
                state.mood == TamagotchiMood.HOPEFUL -> MintSecondary
                lifeScore >= 80 -> CoralPrimary
                lifeScore >= 60 -> MintSecondary
                else -> AmberTertiary
            }

            val auraAlpha = when {
                state.isSleeping -> 0.15f
                state.mood == TamagotchiMood.SICK -> 0.12f
                state.mood == TamagotchiMood.DORMANT -> 0.10f
                state.mood == TamagotchiMood.SAD -> 0.16f
                state.mood == TamagotchiMood.HOPEFUL -> 0.28f
                else -> 0.34f
            }

            val auraRadius = (canvasW * 0.44f) * auraScale * animatedBounceScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = auraAlpha),
                        auraColor.copy(alpha = auraAlpha * 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(centerX, centerY)
            )

            // Secondary Mint/Amber inner ring
            val innerAuraRadius = auraRadius * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isSickOrSad) auraColor.copy(alpha = 0.10f) else MintSecondary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = innerAuraRadius
                ),
                radius = innerAuraRadius,
                center = Offset(centerX, centerY)
            )

            // 1b. Levitation Ground Shadow (scales inversely with floatOffset for 3D depth)
            val shadowY = (canvasH / 2f) + (canvasH * 0.34f)
            val shadowScale = (1f - (floatOffset / 28f)).coerceIn(0.75f, 1.15f)
            drawOval(
                color = DeepEspresso.copy(alpha = 0.08f * shadowScale),
                topLeft = Offset(baseCenterX - (28.dp.toPx() * shadowScale), shadowY - 5.dp.toPx()),
                size = Size(56.dp.toPx() * shadowScale, 10.dp.toPx() * shadowScale)
            )

            // The ground shadow above stays put so the dance reads; everything below grooves.
            centerX += danceDX
            centerY += danceHop

            // 1c. Orbital Bio-Resonance Motes (4 living satellites representing Health, Tasks, Finance, Social)
            val orbitRadius = canvasW * 0.37f
            val moteColors = if (state.mood == TamagotchiMood.SICK) {
                listOf(SerenityError, AmberTertiary, Color(0xFF9E9B95), SerenityError)
            } else if (state.mood == TamagotchiMood.SAD) {
                listOf(Color(0xFF8DA3B8), MintSecondary.copy(alpha = 0.6f), AmberTertiary.copy(alpha = 0.6f), SerenityError)
            } else {
                listOf(MintSecondary, SageAccent, AmberTertiary, CoralPrimary)
            }
            moteColors.forEachIndexed { idx, moteColor ->
                val rad = Math.toRadians((orbitAngleDeg + idx * 90f).toDouble())
                val mx = centerX + (orbitRadius * cos(rad)).toFloat()
                val my = centerY + (orbitRadius * 0.68f * sin(rad)).toFloat()
                drawCircle(
                    color = moteColor.copy(alpha = if (isSickOrSad) 0.35f else 0.75f),
                    radius = if (idx % 2 == 0) 4.dp.toPx() else 3.dp.toPx(),
                    center = Offset(mx, my)
                )
            }

            // 2. Celestial Halo / Crown (Accessory - dims when sick)
            if (state.equippedAccessory.contains("Halo") || state.equippedAccessory.contains("Crown")) {
                val haloY = centerY - (canvasH * 0.30f) * animatedBounceScale
                val haloAlpha = if (isSickOrSad || isDormant) 0.35f else 1.0f
                drawOval(
                    brush = Brush.horizontalGradient(
                        listOf(
                            AmberTertiary.copy(alpha = haloAlpha),
                            CoralHoverGlow.copy(alpha = haloAlpha),
                            AmberTertiary.copy(alpha = haloAlpha)
                        )
                    ),
                    topLeft = Offset(centerX - 36.dp.toPx(), haloY - 8.dp.toPx()),
                    size = Size(72.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 3.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = haloAlpha),
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX, haloY - 8.dp.toPx())
                )
            }

            // 3a. Shared limb geometry + emotional pose (ears/arms/feet read the same mood)
            // Oblong body: narrower than it is tall, like the reference design.
            val bodyRX = (canvasW * 0.24f) * animatedBounceScale
            val bodyRY = (canvasW * 0.30f) * animatedBounceScale
            val isJoyful = !state.isSleeping && (state.mood == TamagotchiMood.RADIANT ||
                lifeScore >= 75 || isHopeful || petBounceTrigger > 1.1f || isDancing)
            val isDroopy = state.isSleeping || isSickOrSad || isDormant
            val wavePhase = Math.toRadians(orbitAngleDeg.toDouble())
            val waveAmp = when {
                isJoyful -> 1f
                isDroopy -> 0f
                else -> 0.45f
            }

            // 3. Round side arms like the feet, with matching dot beans. Up when
            // happy, down when sad, flapping alternately while dancing.
            val armColor = when {
                state.mood == TamagotchiMood.SICK -> Color(0xFFD8D0C5)
                state.mood == TamagotchiMood.DORMANT -> Color(0xFFC4C1BC)
                state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> Color(0xFFD7DEE8)
                else -> Color(0xFF7E5CE0)
            }
            val fingerColor = when {
                state.mood == TamagotchiMood.DORMANT -> Color(0xFFA8A5A0)
                isSickOrSad -> Color.White.copy(alpha = 0.5f)
                else -> MintSecondary
            }
            val armW = bodyRX * 0.52f
            val armH = bodyRX * 0.36f
            val armBaseY = centerY + bodyRY * 0.35f
            val armPoseY = when {
                isJoyful -> -bodyRY * 0.30f
                isDroopy -> bodyRY * 0.18f
                else -> 0f
            }
            listOf(-1f to 1f, 1f to -1f).forEach { (side, flapDir) ->
                val flapPhase = if (isDancing) dancePhaseRad else wavePhase
                val flapAmp = if (isDancing) 8.dp.toPx() else 2.5.dp.toPx() * waveAmp
                val flap = sin(flapPhase).toFloat() * flapAmp * flapDir
                val armCx = centerX + side * bodyRX * 1.02f
                val armCy = armBaseY + armPoseY + flap
                drawOval(
                    color = armColor,
                    topLeft = Offset(armCx - armW / 2f, armCy - armH / 2f),
                    size = Size(armW, armH)
                )
                // Three finger beans across each arm, like the toe beans.
                listOf(-0.20f, 0f, 0.20f).forEach { dotOffset ->
                    drawCircle(
                        color = fingerColor,
                        radius = 2.6.dp.toPx(),
                        center = Offset(armCx + dotOffset * armW, armCy - armH * 0.10f)
                    )
                }
            }

            // 3b. Cat ears: short rounded triangles in body tones with pink inners,
            // perky when joyful, flattened sideways when sick/sad/asleep. The wide
            // base keeps them stubby like the reference instead of spiky.
            val earWiggle = (sin(wavePhase * 2.0) * 2.0).toFloat() * 1.dp.toPx() * waveAmp
            val apexOutX = (if (isDroopy) 1.25f else 0.80f) * bodyRX
            val apexY = centerY - (if (isDroopy) 0.75f else if (isJoyful) 1.55f else 1.44f) * bodyRY + earWiggle
            val earOuterColor = when {
                state.mood == TamagotchiMood.SICK -> Color(0xFFE9E4DD)
                state.mood == TamagotchiMood.DORMANT -> Color(0xFFD8D5D0)
                state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> Color(0xFFE8ECF2)
                else -> Color(0xFF9B7BFF)
            }
            // Fixed pink: CoralPrimary resolves blue under the Meta Horizon theme.
            val innerEarColor = if (isSickOrSad || isDormant) Color(0xFFB5A4A0) else Color(0xFFFF8A9E).copy(alpha = 0.8f)
            listOf(-1f, 1f).forEach { side ->
                val baseInner = Offset(centerX + side * bodyRX * 0.25f, centerY - bodyRY * 0.92f)
                val baseOuter = Offset(centerX + side * bodyRX * 0.90f, centerY - bodyRY * 0.55f)
                val apex = Offset(centerX + side * apexOutX, apexY)
                val midBase = Offset((baseInner.x + baseOuter.x) / 2f, (baseInner.y + baseOuter.y) / 2f)
                // Rounded tip: control only 60% of the way out, so the peak domes softly.
                val tipControl = Offset(
                    midBase.x + (apex.x - midBase.x) * 1.2f,
                    midBase.y + (apex.y - midBase.y) * 1.2f
                )
                drawPath(
                    Path().apply {
                        moveTo(baseInner.x, baseInner.y)
                        quadraticTo(tipControl.x, tipControl.y, baseOuter.x, baseOuter.y)
                        close()
                    },
                    color = earOuterColor
                )
                // Inner-ear accent: same triangle pulled 45% toward its centroid.
                val centroid = Offset(
                    (baseInner.x + baseOuter.x + apex.x) / 3f,
                    (baseInner.y + baseOuter.y + apex.y) / 3f
                )
                fun shrink(p: Offset) = Offset(
                    centroid.x + (p.x - centroid.x) * 0.60f,
                    centroid.y + (p.y - centroid.y) * 0.60f
                )
                val inner0 = shrink(baseInner)
                val inner1 = shrink(baseOuter)
                val innerApex = shrink(apex)
                val innerMid = Offset((inner0.x + inner1.x) / 2f, (inner0.y + inner1.y) / 2f)
                val innerControl = Offset(
                    innerMid.x + (innerApex.x - innerMid.x) * 1.2f,
                    innerMid.y + (innerApex.y - innerMid.y) * 1.2f
                )
                drawPath(
                    Path().apply {
                        moveTo(inner0.x, inner0.y)
                        quadraticTo(innerControl.x, innerControl.y, inner1.x, inner1.y)
                        close()
                    },
                    color = innerEarColor
                )
            }

            // 3c. Little feet tucked under the body (alternating happy bounce when joyful)
            val footColor = if (isSickOrSad) Color(0xFFD8D0C5) else Color(0xFF7E5CE0)
            val toeColor = if (isSickOrSad) Color.White.copy(alpha = 0.5f) else MintSecondary
            val footW = bodyRX * 0.60f
            val footH = bodyRX * 0.40f
            val footBaseY = centerY + bodyRY * 0.92f
            listOf(-1f to 1f, 1f to -1f).forEach { (side, bounceDir) ->
                val stepPhase = if (isDancing) dancePhaseRad else wavePhase
                val stepAmp = if (isDancing) 6.dp.toPx() else 3.dp.toPx() * waveAmp
                val bounce = sin(stepPhase).toFloat() * stepAmp * bounceDir
                val footCx = centerX + side * bodyRX * 0.45f
                val footCy = footBaseY + bounce
                drawOval(
                    color = footColor,
                    topLeft = Offset(footCx - footW / 2f, footCy - footH / 2f),
                    size = Size(footW, footH)
                )
                // Three glowing toe beans across each foot
                listOf(-0.20f, 0f, 0.20f).forEach { toeOffset ->
                    drawCircle(
                        color = toeColor,
                        radius = 2.6.dp.toPx(),
                        center = Offset(footCx + toeOffset * footW, footCy - footH * 0.10f)
                    )
                }
            }

            // 4. Main Creature Body (Pale/wan when sick, soft pearlescent blush when healthy)
            // Opaque glossy vinyl shell.
            val bodyGradientColors = when {
                state.mood == TamagotchiMood.SICK -> listOf(
                    Color(0xFFF7F5F2),
                    Color(0xFFE9E4DD),
                    Color(0xFFD8D0C5)
                )
                state.mood == TamagotchiMood.DORMANT -> listOf(
                    Color(0xFFEDECEA),
                    Color(0xFFDAD8D4),
                    Color(0xFFC4C1BC)
                )
                state.mood == TamagotchiMood.SAD || state.mood == TamagotchiMood.TIRED -> listOf(
                    Color(0xFFF8F7FA),
                    Color(0xFFE8ECF2),
                    Color(0xFFD7DEE8)
                )
                else -> listOf(
                    Color(0xFFCDBDFF),
                    Color(0xFF9B7BFF),
                    Color(0xFF6D4FD0)
                )
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = bodyGradientColors,
                    center = Offset(centerX - 8.dp.toPx(), centerY - 10.dp.toPx()),
                    radius = bodyRY
                ),
                topLeft = Offset(centerX - bodyRX, centerY - bodyRY),
                size = Size(bodyRX * 2f, bodyRY * 2f)
            )

            // Subtle body outline for soft tactile definition
            drawOval(
                color = if (isSickOrSad) Color(0xFF9E9490).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.55f),
                topLeft = Offset(centerX - bodyRX, centerY - bodyRY),
                size = Size(bodyRX * 2f, bodyRY * 2f),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Glossy highlight blob (healthy glow; absent when sick)
            val glossAlpha = when {
                state.mood == TamagotchiMood.SICK -> 0f
                state.mood == TamagotchiMood.DORMANT -> 0f
                isSickOrSad -> 0.08f
                else -> 0.28f
            }
            if (glossAlpha > 0f) {
                drawOval(
                    color = Color.White.copy(alpha = glossAlpha),
                    topLeft = Offset(centerX - bodyRX * 0.85f, centerY - bodyRY * 0.72f),
                    size = Size(bodyRX * 0.7f, bodyRY * 0.5f)
                )
            }

            // 4b. Antenna stalk with mood-reactive glow tip (leans + dims when droopy)
            val antennaLean = if (isDroopy) {
                9.dp.toPx()
            } else {
                sin(wavePhase).toFloat() * 2.dp.toPx() * waveAmp
            }
            val antennaBase = Offset(centerX, centerY - bodyRY * 0.85f)
            val antennaTip = Offset(centerX + antennaLean, centerY - bodyRY * 1.32f)
            drawLine(
                color = if (isSickOrSad) Color(0xFFB5A4A0) else MintSecondary,
                start = antennaBase,
                end = antennaTip,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            if (!isSickOrSad) {
                drawCircle(
                    color = MintSecondary.copy(alpha = 0.28f),
                    radius = 11.dp.toPx(),
                    center = antennaTip
                )
            }
            drawCircle(
                color = if (isSickOrSad) Color(0xFFB5A4A0) else MintSecondary,
                radius = if (isSickOrSad) 5.dp.toPx() else 6.5.dp.toPx(),
                center = antennaTip
            )
            if (!isSickOrSad) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 2.dp.toPx(),
                    center = Offset(antennaTip.x - 1.5.dp.toPx(), antennaTip.y - 1.5.dp.toPx())
                )
            }

            // 5. Cheeks (Coral blush when healthy/hopeful; faint fever/wan tint when sick)
            val cheekOffset = 18.dp.toPx() * animatedBounceScale
            val cheekY = centerY + 12.dp.toPx()
            val cheekColor = when {
                state.mood == TamagotchiMood.SICK -> AmberTertiary.copy(alpha = 0.28f)
                state.mood == TamagotchiMood.DORMANT -> Color(0xFFA8A5A0).copy(alpha = 0.25f)
                state.mood == TamagotchiMood.SAD -> Color(0xFF8DA3B8).copy(alpha = 0.25f)
                // Fixed pink: CoralPrimary resolves blue under the Meta Horizon theme.
                else -> Color(0xFFFF8A9E).copy(alpha = 0.6f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cheekColor, Color.Transparent),
                    center = Offset(centerX - cheekOffset, cheekY),
                    radius = 14.dp.toPx()
                ),
                radius = 13.dp.toPx(),
                center = Offset(centerX - cheekOffset, cheekY)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(cheekColor, Color.Transparent),
                    center = Offset(centerX + cheekOffset, cheekY),
                    radius = 14.dp.toPx()
                ),
                radius = 13.dp.toPx(),
                center = Offset(centerX + cheekOffset, cheekY)
            )

            // 6. Expressive Eyes
            val eyeSpacing = 15.dp.toPx() * animatedBounceScale
            val eyeY = centerY - 2.dp.toPx()

            if (state.isSleeping) {
                val leftEyePath = Path().apply {
                    moveTo(centerX - eyeSpacing - 11.dp.toPx(), eyeY)
                    quadraticTo(centerX - eyeSpacing, eyeY + 7.dp.toPx(), centerX - eyeSpacing + 11.dp.toPx(), eyeY)
                }
                val rightEyePath = Path().apply {
                    moveTo(centerX + eyeSpacing - 11.dp.toPx(), eyeY)
                    quadraticTo(centerX + eyeSpacing, eyeY + 7.dp.toPx(), centerX + eyeSpacing + 11.dp.toPx(), eyeY)
                }
                drawPath(leftEyePath, DeepEspresso, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightEyePath, DeepEspresso, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

                val zPath = Path().apply {
                    moveTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 24.dp.toPx())
                    lineTo(centerX + eyeSpacing + 16.dp.toPx(), eyeY - 14.dp.toPx())
                    lineTo(centerX + eyeSpacing + 24.dp.toPx(), eyeY - 14.dp.toPx())
                }
                drawPath(zPath, SageAccent, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            } else if (isSickOrSad) {
                // Worried / tired eyes with angled eyebrows and sweat/tear drop
                val eyeRadius = 8.dp.toPx()
                drawCircle(color = DeepEspresso.copy(alpha = 0.85f), radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY + 2.dp.toPx()))
                drawCircle(color = DeepEspresso.copy(alpha = 0.85f), radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY + 2.dp.toPx()))

                // Worried inner-slanted brows
                val leftBrow = Path().apply {
                    moveTo(centerX - eyeSpacing - 9.dp.toPx(), eyeY - 9.dp.toPx())
                    lineTo(centerX - eyeSpacing + 5.dp.toPx(), eyeY - 13.dp.toPx())
                }
                val rightBrow = Path().apply {
                    moveTo(centerX + eyeSpacing - 5.dp.toPx(), eyeY - 13.dp.toPx())
                    lineTo(centerX + eyeSpacing + 9.dp.toPx(), eyeY - 9.dp.toPx())
                }
                drawPath(leftBrow, DeepEspresso.copy(alpha = 0.75f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightBrow, DeepEspresso.copy(alpha = 0.75f), style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

                // Little sweat / tear droplet on temple
                val dropX = centerX + eyeSpacing + 15.dp.toPx()
                val dropY = eyeY - 4.dp.toPx()
                drawCircle(
                    color = Color(0xFF63B3ED),
                    radius = 4.5.dp.toPx(),
                    center = Offset(dropX, dropY)
                )
            } else if (isBlinking || petBounceTrigger > 1.1f || isDancing) {
                // Happy curved eyes `^ ^`
                val leftHappy = Path().apply {
                    moveTo(centerX - eyeSpacing - 11.dp.toPx(), eyeY + 2.dp.toPx())
                    quadraticTo(centerX - eyeSpacing, eyeY - 8.dp.toPx(), centerX - eyeSpacing + 11.dp.toPx(), eyeY + 2.dp.toPx())
                }
                val rightHappy = Path().apply {
                    moveTo(centerX + eyeSpacing - 11.dp.toPx(), eyeY + 2.dp.toPx())
                    quadraticTo(centerX + eyeSpacing, eyeY - 8.dp.toPx(), centerX + eyeSpacing + 11.dp.toPx(), eyeY + 2.dp.toPx())
                }
                drawPath(leftHappy, DeepEspresso, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                drawPath(rightHappy, DeepEspresso, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            } else {
                // Big glossy eyes with white + mint double highlights
                val eyeRadius = 10.dp.toPx()
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX - eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 3.6.dp.toPx(), center = Offset(centerX - eyeSpacing - 3.dp.toPx(), eyeY - 3.dp.toPx()))
                drawCircle(color = MintSecondary, radius = 2.dp.toPx(), center = Offset(centerX - eyeSpacing + 2.4.dp.toPx(), eyeY + 3.4.dp.toPx()))
                drawCircle(color = DeepEspresso, radius = eyeRadius, center = Offset(centerX + eyeSpacing, eyeY))
                drawCircle(color = Color.White, radius = 3.6.dp.toPx(), center = Offset(centerX + eyeSpacing - 3.dp.toPx(), eyeY - 3.dp.toPx()))
                drawCircle(color = MintSecondary, radius = 2.dp.toPx(), center = Offset(centerX + eyeSpacing + 2.4.dp.toPx(), eyeY + 3.4.dp.toPx()))
            }

            // 7. Expressive Mouth
            val mouthY = centerY + 10.dp.toPx()
            if (state.isSleeping) {
                drawCircle(color = DeepEspresso, radius = 1.8.dp.toPx(), center = Offset(centerX, mouthY))
            } else if (isSickOrSad) {
                // Downturned worried/sad mouth
                val sadMouth = Path().apply {
                    moveTo(centerX - 7.dp.toPx(), mouthY + 3.dp.toPx())
                    quadraticTo(centerX, mouthY - 4.dp.toPx(), centerX + 7.dp.toPx(), mouthY + 3.dp.toPx())
                }
                drawPath(sadMouth, DeepEspresso, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            } else if (lifeScore >= 75 || isHopeful || petBounceTrigger > 1.1f || isDancing) {
                // Big joyful open smile :D
                val mouthPath = Path().apply {
                    moveTo(centerX - 6.dp.toPx(), mouthY - 2.dp.toPx())
                    quadraticTo(centerX, mouthY + 6.dp.toPx(), centerX + 6.dp.toPx(), mouthY - 2.dp.toPx())
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))
            } else {
                val mouthPath = Path().apply {
                    moveTo(centerX - 5.dp.toPx(), mouthY)
                    quadraticTo(centerX, mouthY + 4.dp.toPx(), centerX + 5.dp.toPx(), mouthY)
                }
                drawPath(mouthPath, DeepEspresso, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }

            // 8. Bio-Resonance Heart Crystal on Chest
            val crystalY = centerY + (bodyRY * 0.56f)
            val crystalColor = when (state.mood) {
                TamagotchiMood.SICK -> SerenityError
                TamagotchiMood.DORMANT -> Color(0xFFA8A5A0)
                TamagotchiMood.SAD, TamagotchiMood.TIRED -> Color(0xFF8DA3B8)
                TamagotchiMood.HOPEFUL -> MintSecondary
                else -> Color(0xFFFF6F9E)
            }
            drawCircle(
                color = crystalColor.copy(alpha = 0.25f * auraScale),
                radius = 7.dp.toPx(),
                center = Offset(centerX, crystalY)
            )
            drawCircle(
                color = crystalColor,
                radius = 3.5.dp.toPx(),
                center = Offset(centerX, crystalY)
            )

            // Dance music notes (visible only mid-boogie)
            if (isDancing) {
                val noteBobA = sin(dancePhaseRad).toFloat() * 4.dp.toPx()
                val noteBobB = sin(dancePhaseRad + Math.PI).toFloat() * 4.dp.toPx()
                listOf(
                    Offset(centerX - bodyRX * 1.25f, centerY - bodyRY * 1.05f + noteBobA),
                    Offset(centerX + bodyRX * 1.30f, centerY - bodyRY * 0.80f + noteBobB)
                ).forEach { notePos ->
                    drawCircle(
                        color = MintSecondary,
                        radius = 3.2.dp.toPx(),
                        center = notePos
                    )
                    drawLine(
                        color = MintSecondary,
                        start = Offset(notePos.x + 3.dp.toPx(), notePos.y),
                        end = Offset(notePos.x + 3.dp.toPx(), notePos.y - 10.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 9. Petting / State Particles (when petted, hopeful, or radiant)
            if (petBounceTrigger > 1.05f || isHopeful) {
                drawCircle(
                    color = CoralPrimary.copy(alpha = 0.85f),
                    radius = 4.dp.toPx(),
                    center = Offset(centerX - 28.dp.toPx(), centerY - 28.dp.toPx())
                )
                drawCircle(
                    color = MintSecondary.copy(alpha = 0.9f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX + 30.dp.toPx(), centerY - 24.dp.toPx())
                )
            }
        }
    }
}
