package com.example

import com.example.data.LifeRuleEngine
import com.example.data.connectors.RealCalendarTasksResult
import com.example.data.connectors.RealFinanceSyncResult
import com.example.data.connectors.RealHealthSyncResult
import com.example.data.connectors.RealSocialSyncResult
import com.example.model.ChannelTelemetryReport
import com.example.model.MemoryItem
import com.example.model.TamagotchiMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LifeRuleEngineTest {

  private fun report(score: Int = 80, alert: Boolean = false) = ChannelTelemetryReport(
    channelName = "Test",
    headlineStatus = "OK",
    summary = "Test summary",
    scorePercent = score,
    isAlert = alert,
    metrics = emptyList(),
    historyLog = emptyList()
  )

  private fun health(
    sleep: Float = 7.4f,
    priorSleep: Float = 7.5f,
    alert: Boolean = false
  ) = RealHealthSyncResult(
    isConnected = true,
    statusLine = "connected",
    stepsToday = 8000L,
    avgSleepHours7d = sleep,
    priorAvgSleepHours7d = priorSleep,
    restingOrAvgHrBpm = 62L,
    hydrationLitersToday = 1.8,
    weeklySleepHours = listOf(sleep, sleep, sleep, sleep, sleep, sleep, sleep),
    report = report(alert = alert)
  )

  private fun calendar(lateNights: Int = 0, canceled: Int = 0) = RealCalendarTasksResult(
    isConnected = true,
    statusLine = "connected",
    eventsTodayCount = 5,
    completedTodayCount = 4,
    upcomingWeekCount = 12,
    lateNightEvents7d = lateNights,
    canceledOrDeclined14d = canceled,
    googleTasksSynced = false,
    googleTasksCompleted = 0,
    googleTasksPending = 0,
    report = report()
  )

  private fun finance(
    deliveries: Int = 0,
    deliverySpend: Double = 0.0,
    spend: Double = 4000.0,
    inflow: Double = 5000.0
  ) = RealFinanceSyncResult(
    isConnected = true,
    sourceName = "Test Bank",
    statusLine = "connected",
    totalBalanceUsd = 12000.0,
    totalSpend30dUsd = spend,
    totalInflow30dUsd = inflow,
    deliveryOrderCount = deliveries,
    deliverySpendUsd = deliverySpend,
    errorMessage = null,
    report = report()
  )

  private fun social(neglected: Int = 0, declinedFb: Int = 0) = RealSocialSyncResult(
    isConnected = true,
    statusLine = "connected",
    facebookConnected = true,
    facebookUserName = "Tester",
    facebookFriendCount = 120,
    facebookAttendingEvents = 2,
    facebookDeclinedEvents = declinedFb,
    contactsConnected = true,
    starredCloseContacts = 4,
    neglectedStarredContacts = neglected,
    birthdaysLoggedCount = 1,
    errorMessage = null,
    report = report()
  )

  @Test
  fun successTrapDetectedFromLiveTelemetry() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(sleep = 5.0f, priorSleep = 7.5f),
      calendar = calendar(lateNights = 3, canceled = 2),
      finance = finance(),
      social = social(neglected = 1)
    )

    assertNotNull(match)
    assertEquals("SUCCESS_TRAP", match!!.ruleId)
    assertEquals(TamagotchiMood.SICK, match.mood)
    assertTrue(match.explanation.contains("7.5h → 5.0h"))
    assertTrue(match.explanation.contains("+20% savings"))
  }

  @Test
  fun moneyLeakDetectedFromLiveTelemetry() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(sleep = 6.0f, priorSleep = 6.2f),
      calendar = calendar(lateNights = 0, canceled = 0),
      finance = finance(deliveries = 5, deliverySpend = 170.0),
      social = social()
    )

    assertNotNull(match)
    assertEquals("MONEY_LEAK", match!!.ruleId)
    assertEquals(TamagotchiMood.TIRED, match.mood)
    assertTrue(match.explanation.contains("5 delivery orders"))
  }

  @Test
  fun connectionDebtDetectedWithValuesAlignment() {
    val values = listOf(
      MemoryItem(
        id = "v1",
        title = "Family First",
        content = "Never sacrifice close family time for work.",
        category = "Values",
        date = "today",
        isPinned = true
      )
    )
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(),
      calendar = calendar(),
      finance = finance(),
      social = social(neglected = 2),
      pinnedValues = values
    )

    assertNotNull(match)
    assertEquals("CONNECTION_DEBT", match!!.ruleId)
    assertEquals(TamagotchiMood.SAD, match.mood)
    assertTrue(match.explanation.contains("Family First"))
  }

  @Test
  fun successTrapTakesPriorityOverMoneyLeak() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(sleep = 5.2f, priorSleep = 7.4f),
      calendar = calendar(lateNights = 4, canceled = 3),
      finance = finance(deliveries = 6, deliverySpend = 210.0),
      social = social(neglected = 1)
    )

    assertNotNull(match)
    assertEquals("SUCCESS_TRAP", match!!.ruleId)
  }

  @Test
  fun healthyTelemetryMatchesNoRule() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(),
      calendar = calendar(),
      finance = finance(deliveries = 1, deliverySpend = 24.0),
      social = social()
    )

    assertNull(match)
  }

  @Test
  fun successTrapSeparatesCanceledPlansFromNeglectedContacts() {
    // Neglected contacts count toward the trigger but must never be called canceled plans.
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(sleep = 5.0f, priorSleep = 7.5f),
      calendar = calendar(lateNights = 3, canceled = 0),
      finance = finance(),
      social = social(neglected = 2)
    )

    assertNotNull(match)
    assertTrue(match!!.explanation.contains("2 neglected close contacts"))
    assertFalse(match.explanation.contains("canceled"))
  }

  @Test
  fun moneyLeakClaimsCorrelationNotCausation() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = health(sleep = 6.0f, priorSleep = 6.2f),
      calendar = calendar(),
      finance = finance(deliveries = 4, deliverySpend = 130.0),
      social = social()
    )

    assertNotNull(match)
    assertTrue(match!!.speech.contains("alongside"))
    assertFalse(match.speech.contains("fueled by"))
  }

  @Test
  fun connectionDebtMarksUnconnectedChannelsUnknown() {
    val match = LifeRuleEngine.evaluateLiveRules(
      health = null,
      calendar = null,
      finance = null,
      social = social(neglected = 2)
    )

    assertNotNull(match)
    assertTrue(match!!.explanation.contains("unknown"))
    assertFalse(match.explanation.contains("look stable"))
  }

  @Test
  fun disconnectedChannelsMatchNoRule() {
    assertNull(LifeRuleEngine.evaluateLiveRules(null, null, null, null))
    assertNull(
      LifeRuleEngine.evaluateLiveRules(
        health = health(),
        calendar = null,
        finance = null,
        social = null
      )
    )
  }
}
