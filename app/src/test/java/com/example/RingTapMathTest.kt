package com.example

import com.example.ui.screens.resolveRingTap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pins the radar ring hit-test: taps near a ring radius select it (outer →
 * inner = Health, Tasks, Finance, Social); core, far-outside, and dead-zone
 * taps clear the selection.
 */
class RingTapMathTest {

    private val outer = 100f
    private val gap = 20f
    private val stroke = 10f
    private val slop = 6f

    private fun tap(dist: Float) = resolveRingTap(dist, outer, gap, stroke, slop)

    @Test
    fun tapOnEachRing_selectsThatChannel() {
        assertEquals(0, tap(100f))
        assertEquals(1, tap(80f))
        assertEquals(2, tap(60f))
        assertEquals(3, tap(40f))
    }

    @Test
    fun tapNearRing_withinSlop_selectsIt() {
        assertEquals(0, tap(95f))
        assertEquals(3, tap(46f))
    }

    @Test
    fun tapOnCore_clearsSelection() {
        assertNull(tap(0f))
        assertNull(tap(20f))
    }

    @Test
    fun tapFarOutside_clearsSelection() {
        assertNull(tap(200f))
        assertNull(tap(115f))
    }
}
