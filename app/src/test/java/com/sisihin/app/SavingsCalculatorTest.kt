package com.sisihin.app

import com.sisihin.app.core.util.SavingsCalculator
import com.sisihin.app.domain.model.PlanFrequency
import org.junit.Assert.assertEquals
import org.junit.Test

class SavingsCalculatorTest {
    @Test
    fun percent_capsAt99WhenRemaining() {
        assertEquals(99.0, SavingsCalculator.percent(1000, 999), 0.001)
    }
    @Test
    fun percent_100WhenComplete() {
        assertEquals(100.0, SavingsCalculator.percent(1000, 1000), 0.001)
    }
    @Test
    fun estimateText() {
        assertEquals("2 Hari Lagi", SavingsCalculator.estimateText(20000, 10000, PlanFrequency.DAILY))
    }
}