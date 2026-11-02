package com.example

import com.example.data.connectors.FacebookSocialConnector
import com.example.data.connectors.HealthConnectManager
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncFallbackMathTest {

  @Test
  fun stepCounterFirstReadBaselinesToZero() {
    // A 65,000 cumulative reading on first sight must NOT display as today's steps.
    val (today, baseline) = HealthConnectManager.resolveDailySteps(
      currentTotal = 65000L,
      baseline = -1L,
      baselineDay = "",
      today = "2026-11-01"
    )
    assertEquals(0L, today)
    assertEquals(65000L, baseline)
  }

  @Test
  fun stepCounterSameDayReturnsDelta() {
    val (today, baseline) = HealthConnectManager.resolveDailySteps(
      currentTotal = 68500L,
      baseline = 65000L,
      baselineDay = "2026-11-01",
      today = "2026-11-01"
    )
    assertEquals(3500L, today)
    assertEquals(65000L, baseline)
  }

  @Test
  fun stepCounterRebootAndRolloverRebaseline() {
    // Reboot: counter reset below the stored baseline.
    val (rebootToday, rebootBaseline) = HealthConnectManager.resolveDailySteps(
      currentTotal = 120L,
      baseline = 65000L,
      baselineDay = "2026-11-01",
      today = "2026-11-01"
    )
    assertEquals(0L, rebootToday)
    assertEquals(120L, rebootBaseline)

    // New day: stale baseline is replaced.
    val (rollToday, rollBaseline) = HealthConnectManager.resolveDailySteps(
      currentTotal = 69000L,
      baseline = 65000L,
      baselineDay = "2026-11-01",
      today = "2026-11-02"
    )
    assertEquals(0L, rollToday)
    assertEquals(69000L, rollBaseline)
  }

  @Test
  fun neglectedCountsStaleAndNeverContacted() {
    val now = 1_793_000_000_000L
    val cutoff = now - (14L * 24L * 3600_000L)
    val times = listOf(
      0L, // never contacted -> neglected
      cutoff - 1, // 14 days + 1ms ago -> neglected
      cutoff + 1, // just inside the window -> fine
      now // today -> fine
    )
    assertEquals(2, FacebookSocialConnector.countNeglectedStarred(times, cutoff))
  }

  @Test
  fun neglectedEmptyListIsZero() {
    assertEquals(
      0,
      FacebookSocialConnector.countNeglectedStarred(emptyList(), 1_793_000_000_000L)
    )
  }
}
