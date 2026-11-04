package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import org.junit.Rule
import org.junit.Test

/**
 * User-journey test: fresh-state dormant home → Success Trap → Consult →
 * offline advice card. Runs on device/emulator via
 * `:app:connectedDebugAndroidTest` (the emulator must be booted).
 *
 * The journey normalizes state first (Demo Scenarios → Live Data), so it is
 * repeatable even when a previous run left a scenario selected. Keyless runs
 * resolve through the Offline Rules Engine; if a Meta API key is entered on
 * the device, Consult goes to live Muse Spark — the test accepts either card
 * but requires exactly one honest provenance label (never a fallback wearing
 * the cloud's name).
 */
class SuccessTrapJourneyTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun ensureScenariosOpen() {
        // The flask toggles: only tap it when the chips are not already out.
        if (compose.onAllNodesWithText("1. Success Trap")
                .fetchSemanticsNodes().isEmpty()
        ) {
            compose.onNodeWithContentDescription("Demo Scenarios").performClick()
        }
    }

    @Test
    fun successTrapJourney_dormantToAdvice() {
        // 1. Avatar home, normalized to the honest empty state.
        compose.onNodeWithTag("avatar_only_home_screen").assertIsDisplayed()
        ensureScenariosOpen()
        compose.onNodeWithText("Live Data", substring = true).performClick()
        compose.onNodeWithText("Dormant", substring = true).assertIsDisplayed()

        // 2. Pick the Success Trap: Lumi falls visibly sick.
        ensureScenariosOpen()
        compose.onNodeWithText("1. Success Trap").performClick()
        compose.onNodeWithText("Visibly Sick", substring = true).assertIsDisplayed()

        // 3. Open the hub and consult (live Spark when keyed, else offline).
        compose.onNodeWithText("Open Companion Hub").performClick()
        // The hub is a lazy list: scroll until Consult is composed, then ease
        // it fully above the bottom nav bar (a tap at the screen edge would
        // hit the nav instead) using its measured position.
        compose.onNodeWithTag("attune_screen")
            .performScrollToNode(hasText("Consult LifeSOS for Advice"))
        val listH = compose.onNodeWithTag("attune_screen")
            .fetchSemanticsNode().size.height.toFloat()
        for (attempt in 1..8) {
            val btn = compose.onAllNodesWithText("Consult LifeSOS for Advice")
                .fetchSemanticsNodes().firstOrNull()
            if (btn == null) {
                // Overshot: ease back down.
                compose.onNodeWithTag("attune_screen").performTouchInput {
                    swipeDown(
                        startY = visibleSize.height * 0.4f,
                        endY = visibleSize.height * 0.55f
                    )
                }
                continue
            }
            val centerY = btn.positionInRoot.y + btn.size.height / 2f
            if (centerY < listH - 350f) break
            compose.onNodeWithTag("attune_screen").performTouchInput {
                swipeUp(
                    startY = visibleSize.height * 0.6f,
                    endY = visibleSize.height * 0.45f
                )
            }
        }
        // Never tap blind: re-verify the button sits above the nav bar, or
        // fail fast instead of tapping the nav underneath it.
        val tapTarget = compose.onAllNodesWithText("Consult LifeSOS for Advice")
            .fetchSemanticsNodes().firstOrNull()
        val tapY = tapTarget?.let { it.positionInRoot.y + it.size.height / 2f }
        check(tapY != null && tapY < listH - 350f) {
            "Consult button not safely tappable (y=$tapY listH=$listH)"
        }
        compose.onNodeWithTag("home_send_to_muse_button").performClick()
        // Prove the tap landed in the hub (a nav-bar mistap would leave it).
        compose.onNodeWithTag("attune_screen").assertExists()

        // 4. The advice card arrives with honest provenance — live Spark when a
        // key is entered, otherwise the offline engine. It composes below
        // Consult; drift down at most twice, then poll in place — continuous
        // scrolling would slide past the card's slot while the cloud responds.
        var adviceFound = false
        for (i in 0 until 45) {
            val live = compose.onAllNodesWithText("Meta Muse Spark 1.3", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
            val offline = compose.onAllNodesWithText("Offline Rules Engine", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
            if (live || offline) {
                adviceFound = true
                break
            }
            if (i == 8 || i == 17) {
                compose.onNodeWithTag("attune_screen").performTouchInput {
                    swipeUp(
                        startY = visibleSize.height * 0.65f,
                        endY = visibleSize.height * 0.40f
                    )
                }
            }
            Thread.sleep(1000)
        }
        check(adviceFound) { "No advice card appeared after Consult" }
        // Exactly one honest pairing must be visible.
        val liveCard = compose.onAllNodesWithText("Meta Muse Spark 1.3", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
        val offlineCard = compose.onAllNodesWithText("Offline Rules Engine", substring = true)
            .fetchSemanticsNodes().isNotEmpty()
        check(liveCard xor offlineCard) {
            "Advice provenance unclear: live=$liveCard offline=$offlineCard"
        }
        if (offlineCard) {
            compose.onNodeWithText("Unscreened", substring = true).assertExists()
        } else {
            compose.onNodeWithText("Prompt-Level Safety Guidance", substring = true).assertExists()
        }
    }
}
