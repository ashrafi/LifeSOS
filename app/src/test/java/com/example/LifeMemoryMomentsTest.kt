package com.example

import com.example.data.LifeMemoryMoments
import com.example.data.connectors.RealFinanceSyncResult
import com.example.data.connectors.RealHealthSyncResult
import com.example.model.ChannelTelemetryReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeMemoryMomentsTest {

  private fun report() = ChannelTelemetryReport(
    channelName = "Test",
    headlineStatus = "OK",
    summary = "Test summary",
    scorePercent = 85,
    isAlert = false,
    metrics = emptyList(),
    historyLog = emptyList()
  )

  private fun health(sleep: Float = 7.4f, priorSleep: Float = 7.5f, steps: Long = 4000L) =
    RealHealthSyncResult(
      isConnected = true,
      statusLine = "connected",
      stepsToday = steps,
      avgSleepHours7d = sleep,
      priorAvgSleepHours7d = priorSleep,
      restingOrAvgHrBpm = 60L,
      hydrationLitersToday = 1.9,
      weeklySleepHours = listOf(sleep, sleep, sleep, sleep, sleep, sleep, sleep),
      report = report()
    )

  private fun finance(deliveries: Int = 1) = RealFinanceSyncResult(
    isConnected = true,
    sourceName = "Test Bank",
    statusLine = "connected",
    totalBalanceUsd = 12000.0,
    totalSpend30dUsd = 4000.0,
    totalInflow30dUsd = 5000.0,
    deliveryOrderCount = deliveries,
    deliverySpendUsd = 24.0,
    errorMessage = null,
    report = report()
  )

  @Test
  fun sleepRecoveryTakesPriority() {
    val moment = LifeMemoryMoments.momentFor(
      health = health(sleep = 7.6f, priorSleep = 6.8f, steps = 9000L),
      calendar = null,
      finance = finance(deliveries = 0),
      social = null
    )

    assertNotNull(moment)
    assertTrue(moment!!.contains("sleeping better"))
    assertTrue(moment.contains("6.8h → 7.6h"))
  }

  @Test
  fun deliveryFreeStretchIsRemembered() {
    val moment = LifeMemoryMoments.momentFor(
      health = health(),
      calendar = null,
      finance = finance(deliveries = 0),
      social = null
    )

    assertNotNull(moment)
    assertTrue(moment!!.contains("zero late-night delivery"))
  }

  @Test
  fun activeDayIsRemembered() {
    val moment = LifeMemoryMoments.momentFor(
      health = health(steps = 9500L),
      calendar = null,
      finance = finance(deliveries = 2),
      social = null
    )

    assertNotNull(moment)
    assertTrue(moment!!.contains("Eight thousand steps"))
  }

  @Test
  fun unremarkableWeekReturnsNull() {
    assertNull(
      LifeMemoryMoments.momentFor(
        health = health(),
        calendar = null,
        finance = finance(deliveries = 2),
        social = null
      )
    )
    assertNull(LifeMemoryMoments.momentFor(null, null, null, null))
  }

  @Test
  fun smallSleepGainIsNotCelebrated() {
    // +0.2h is noise, not recovery: no moment (and no false memory).
    val moment = LifeMemoryMoments.momentFor(
      health = health(sleep = 7.3f, priorSleep = 7.1f),
      calendar = null,
      finance = finance(deliveries = 2),
      social = null
    )
    assertNull(moment)
  }
}
