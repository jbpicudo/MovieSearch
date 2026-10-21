package com.velychko.sdk.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReturnCounterTest {

    @Test
    fun `the launch itself is not a return`() {
        assertFalse(ReturnCounter(triggerOnReturn = 2).onAppForegrounded())
    }

    @Test
    fun `fires exactly once, on the second return`() {
        val counter = ReturnCounter(triggerOnReturn = 2)

        val results = List(6) { counter.onAppForegrounded() }

        // launch, return 1, return 2, return 3, ...
        assertEquals(listOf(false, false, true, false, false, false), results)
    }
}
