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
 * repeatable even when a previous run left a scenario selected. No API key is
 * entered, so Consult deterministically resolves through the Offline Rules
 * Engine — the test asserts that honest fallback path, not cloud inference.
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
    fun successTrapJourney_dormantToOfflineAdvice() {
        // 1. Avatar home, normalized to the honest empty state.
        compose.onNodeWithTag("avatar_only_home_screen").assertIsDisplayed()
        ensureScenariosOpen()
        compose.onNodeWithText("Live Data", substring = true).performClick()
        compose.onNodeWithText("Dormant", substring = true).assertIsDisplayed()

        // 2. Pick the Success Trap: Lumi falls visibly sick.
        ensureScenariosOpen()
        compose.onNodeWithText("1. Success Trap").performClick()
        compose.onNodeWithText("Visibly Sick", substring = true).assertIsDisplayed()

        // 3. Open the hub and consult (no key → offline engine).
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
        compose.onNodeWithTag("home_send_to_muse_button").performClick()

        // 4. The offline advice card arrives with honest provenance. It composes
        // below Consult, so scroll the hub list until it appears.
        var adviceFound = false
        repeat(10) {
            if (compose.onAllNodesWithText("Offline Rules Engine", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            ) {
                adviceFound = true
            } else {
                compose.onNodeWithTag("attune_screen")
                    .performTouchInput { swipeUp() }
            }
        }
        check(adviceFound) { "Offline advice card never appeared after Consult" }
        // Same card: Trap headline + honest unscreened-safety line.
        compose.onNodeWithText("High Focus", substring = true).assertExists()
        compose.onNodeWithText("Unscreened", substring = true).assertExists()
    }
}
