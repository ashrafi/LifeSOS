package com.example

import com.example.data.connectors.HealthConnectManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the deterministic demo sample plan: 7 plausible days, oldest → today.
 * The Health Connect insert path itself needs a device; this guards the values.
 */
class HealthSampleDataTest {

    @Test
    fun samplePlan_coversSevenDays() {
        assertEquals(7, HealthConnectManager.buildSampleWeekPlan().size)
    }

    @Test
    fun samplePlan_valuesArePlausible() {
        HealthConnectManager.buildSampleWeekPlan().forEach { day ->
            assertTrue("steps=${day.steps}", day.steps in 1L..30_000L)
            assertTrue("sleep=${day.sleepHours}", day.sleepHours in 4.0f..10.0f)
            assertTrue("hr=${day.restingHrBpm}", day.restingHrBpm in 40L..120L)
            assertTrue("water=${day.hydrationLiters}", day.hydrationLiters in 0.2..4.0)
        }
    }

    @Test
    fun samplePlan_isDeterministic() {
        assertEquals(
            HealthConnectManager.buildSampleWeekPlan(),
            HealthConnectManager.buildSampleWeekPlan()
        )
    }
}
