package com.example.data

import com.example.data.connectors.RealCalendarTasksResult
import com.example.data.connectors.RealFinanceSyncResult
import com.example.data.connectors.RealHealthSyncResult
import com.example.data.connectors.RealSocialSyncResult
import com.example.model.DemoRecoveryStage
import com.example.model.MemoryItem
import com.example.model.MuseDemoScenario
import com.example.model.TamagotchiMood
import java.util.Locale

/**
 * Deterministic cross-channel trade-off engine.
 *
 * Evaluates the same named rules behind the scripted demo scenarios against REAL synced
 * telemetry, so Live mode can genuinely surface "Success Trap" / "Money Leak" /
 * "Connection Debt" from the user's own data instead of only single-channel alerts.
 *
 * Pure Kotlin with no Android dependencies: fully unit-testable on the JVM.
 */
data class LiveRuleMatch(
    val ruleId: String,
    val scenario: MuseDemoScenario,
    val explanation: String,
    val mood: TamagotchiMood,
    val speech: String,
    val stage: DemoRecoveryStage = DemoRecoveryStage.PROACTIVE_CONCERN
)

object LifeRuleEngine {

    private val valuesKeywords = listOf("family", "friend", "love", "relationship", "parents", "sibling", "brother", "sister", "mother", "father", "wife", "husband", "partner", "kids", "children", "son", "daughter")

    /**
     * Returns the highest-priority matching rule, or null when live telemetry shows
     * no cross-channel trade-off pattern. Priority: Success Trap > Money Leak > Connection Debt.
     */
    fun evaluateLiveRules(
        health: RealHealthSyncResult?,
        calendar: RealCalendarTasksResult?,
        finance: RealFinanceSyncResult?,
        social: RealSocialSyncResult?,
        pinnedValues: List<MemoryItem> = emptyList()
    ): LiveRuleMatch? {
        return detectSuccessTrap(health, calendar, finance, social)
            ?: detectMoneyLeak(health, calendar, finance)
            ?: detectConnectionDebt(health, calendar, finance, social, pinnedValues)
    }

    /**
     * Rule 1 — The Success Trap: output looks thriving (late-night work throughput, healthy
     * savings) while sleep collapses and social plans get canceled to fund it.
     */
    private fun detectSuccessTrap(
        health: RealHealthSyncResult?,
        calendar: RealCalendarTasksResult?,
        finance: RealFinanceSyncResult?,
        social: RealSocialSyncResult?
    ): LiveRuleMatch? {
        if (health?.isConnected != true || calendar?.isConnected != true) return null
        if (health.avgSleepHours7d <= 0f) return null // No real sleep data yet

        val sleepDrop = health.priorAvgSleepHours7d - health.avgSleepHours7d
        val sleepCollapsed = health.avgSleepHours7d < 6.5f && sleepDrop >= 1.0f
        if (!sleepCollapsed) return null

        val overwork = calendar.lateNightEvents7d >= 2
        if (!overwork) return null

        // Canceled plans and neglected contacts are distinct signals: count both toward
        // the trigger, but never describe one as the other.
        val canceledPlans = calendar.canceledOrDeclined14d +
            (social?.facebookDeclinedEvents ?: 0)
        val neglectedContacts = social?.neglectedStarredContacts ?: 0
        if (canceledPlans + neglectedContacts < 2) return null

        val savingsNote = if (finance?.isConnected == true && finance.totalInflow30dUsd > 0) {
            val rate = (finance.totalInflow30dUsd - finance.totalSpend30dUsd) / finance.totalInflow30dUsd
            if (rate >= 0.10) " despite +${(rate * 100).toInt()}% savings" else ""
        } else ""

        val socialDetail = buildList {
            if (canceledPlans > 0) add("$canceledPlans canceled/declined plan${if (canceledPlans == 1) "" else "s"}")
            if (neglectedContacts > 0) add("$neglectedContacts neglected close contact${if (neglectedContacts == 1) "" else "s"}")
        }.joinToString(" + ")

        val explanation = "LIVE RULE TRIGGERED: Success Trap — sleep dropped " +
            "${fmt1(health.priorAvgSleepHours7d)}h → ${fmt1(health.avgSleepHours7d)}h " +
            "+ ${calendar.lateNightEvents7d} late-night work sessions " +
            "+ $socialDetail$savingsNote."

        return LiveRuleMatch(
            ruleId = "SUCCESS_TRAP",
            scenario = MuseDemoScenario.SUCCESS_TRAP,
            explanation = explanation,
            mood = TamagotchiMood.SICK,
            speech = "Live Success Trap: you're pushing ${calendar.lateNightEvents7d} late nights on " +
                "${fmt1(health.avgSleepHours7d)}h sleep while $socialDetail piled up. " +
                "Let's protect tonight's rest and reconnect this weekend!"
        )
    }

    /**
     * Rule 2 — The Invisible Money Leak: repeated late-night delivery spend driven by
     * evening exhaustion (poor sleep and/or late work), not hunger.
     */
    private fun detectMoneyLeak(
        health: RealHealthSyncResult?,
        calendar: RealCalendarTasksResult?,
        finance: RealFinanceSyncResult?
    ): LiveRuleMatch? {
        if (finance?.isConnected != true) return null
        if (finance.deliveryOrderCount < 3) return null

        val poorSleep = health?.isConnected == true &&
            health.avgSleepHours7d > 0f && health.avgSleepHours7d < 6.5f
        val lateWork = calendar?.isConnected == true && calendar.lateNightEvents7d >= 2
        if (!poorSleep && !lateWork) return null

        val cause = when {
            poorSleep && lateWork -> "evening exhaustion (${fmt1(health!!.avgSleepHours7d)}h sleep + ${calendar!!.lateNightEvents7d} late sessions)"
            poorSleep -> "evening exhaustion (${fmt1(health!!.avgSleepHours7d)}h sleep)"
            else -> "evening exhaustion (${calendar!!.lateNightEvents7d} late work sessions)"
        }

        return LiveRuleMatch(
            ruleId = "MONEY_LEAK",
            scenario = MuseDemoScenario.INVISIBLE_MONEY_LEAK,
            explanation = "LIVE RULE TRIGGERED: Money Leak — ${finance.deliveryOrderCount} delivery orders " +
                "($${fmt0(finance.deliverySpendUsd)}) correlating with $cause.",
            mood = TamagotchiMood.TIRED,
            // Correlation, not causation: orders coincide with exhaustion; we don't claim why.
            speech = "Live Money Leak: ${finance.deliveryOrderCount} delivery orders ($${fmt0(finance.deliverySpendUsd)}) " +
                "alongside $cause. Want a 5-minute home dinner and an early wind-down?"
        )
    }

    /**
     * Rule 3 — The Connection Debt: body and budget look fine, but close relationships are
     * quietly drifting — optionally contradicting a pinned core value.
     */
    private fun detectConnectionDebt(
        health: RealHealthSyncResult?,
        calendar: RealCalendarTasksResult?,
        finance: RealFinanceSyncResult?,
        social: RealSocialSyncResult?,
        pinnedValues: List<MemoryItem>
    ): LiveRuleMatch? {
        if (social?.isConnected != true) return null
        if (health?.report?.isAlert == true || finance?.report?.isAlert == true) return null

        val neglected = social.neglectedStarredContacts
        val declined = social.facebookDeclinedEvents + (calendar?.canceledOrDeclined14d ?: 0)
        if (neglected < 1 && declined < 2) return null

        val conflictValue = pinnedValues.firstOrNull { v ->
            val text = "${v.title} ${v.content}".lowercase(Locale.US)
            valuesKeywords.any { text.contains(it) }
        }
        val valuesNote = if (conflictValue != null) {
            " despite your pinned value '${conflictValue.title}'"
        } else ""

        val driftDetail = buildList {
            if (neglected >= 1) add("$neglected neglected close contact${if (neglected == 1) "" else "s"}")
            if (declined >= 1) add("$declined declined/canceled plan${if (declined == 1) "" else "s"}")
        }.joinToString(" + ")

        // Never claim stability without evidence: unconnected channels are unknown.
        val healthKnownStable = health?.isConnected == true
        val budgetKnownStable = finance?.isConnected == true
        val stabilityNote = when {
            healthKnownStable && budgetKnownStable -> "health and budget channels look stable"
            healthKnownStable -> "health looks stable but budget is unknown (bank not connected)"
            budgetKnownStable -> "budget looks stable but health is unknown (Health Connect not linked)"
            else -> "health and budget are both unknown (connect Health Connect and your bank to complete the picture)"
        }

        return LiveRuleMatch(
            ruleId = "CONNECTION_DEBT",
            scenario = MuseDemoScenario.CONNECTION_DEBT,
            explanation = "LIVE RULE TRIGGERED: Connection Debt — $driftDetail$valuesNote; " +
                "$stabilityNote.",
            mood = TamagotchiMood.SAD,
            speech = "Live Connection Debt: $driftDetail$valuesNote. " +
                "Want to send a 1-tap check-in to someone who matters?"
        )
    }

    private fun fmt1(v: Float): String = String.format(Locale.US, "%.1f", v)
    private fun fmt0(v: Double): String = String.format(Locale.US, "%.0f", v)
}
