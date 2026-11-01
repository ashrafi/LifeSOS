package com.example.data

import com.example.data.connectors.RealCalendarTasksResult
import com.example.data.connectors.RealFinanceSyncResult
import com.example.data.connectors.RealHealthSyncResult
import com.example.data.connectors.RealSocialSyncResult
import java.util.Locale

/**
 * Longitudinal memory moments: trend-aware lines proving Lumi remembers the user's
 * recent history, not just the current snapshot. Used as the healthy-path speech in
 * live mode so recovery and consistency get acknowledged out loud.
 *
 * Pure Kotlin with no Android dependencies: fully unit-testable on the JVM.
 */
object LifeMemoryMoments {

    /**
     * Returns the highest-priority memory line, or null when nothing notable stands out.
     * Priority: sleep recovery > delivery-free stretch > active day.
     */
    fun momentFor(
        health: RealHealthSyncResult?,
        calendar: RealCalendarTasksResult?,
        finance: RealFinanceSyncResult?,
        social: RealSocialSyncResult?
    ): String? {
        sleepRecovery(health)?.let { return it }
        deliveryFreeStretch(finance)?.let { return it }
        activeDay(health)?.let { return it }
        return null
    }

    private fun sleepRecovery(health: RealHealthSyncResult?): String? {
        if (health?.isConnected != true) return null
        if (health.avgSleepHours7d < 7.0f || health.priorAvgSleepHours7d <= 0f) return null
        val gain = health.avgSleepHours7d - health.priorAvgSleepHours7d
        if (gain < 0.5f) return null
        return "You've been sleeping better this week — " +
            "${fmt1(health.priorAvgSleepHours7d)}h → ${fmt1(health.avgSleepHours7d)}h. " +
            "I've never felt stronger."
    }

    private fun deliveryFreeStretch(finance: RealFinanceSyncResult?): String? {
        if (finance?.isConnected != true) return null
        if (finance.deliveryOrderCount != 0) return null
        return "A full stretch with zero late-night delivery orders. " +
            "Your evenings — and our budget — feel calmer already."
    }

    private fun activeDay(health: RealHealthSyncResult?): String? {
        if (health?.isConnected != true) return null
        if (health.stepsToday < 8000L) return null
        return "Eight thousand steps and counting today. Walking together is my favorite part of the day."
    }

    private fun fmt1(v: Float): String = String.format(Locale.US, "%.1f", v)
}
