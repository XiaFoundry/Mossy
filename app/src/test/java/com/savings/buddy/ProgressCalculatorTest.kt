package com.savings.buddy

import com.savings.buddy.business.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `正常进度`() {
        val r = ProgressCalculator.calculate(35_000, 100_000)
        assertEquals(0.35f, r.progress, 0.0001f)
        assertEquals(35, r.percent)
        assertEquals(65_000, r.remainingCents)
        assertFalse(r.isCompleted)
        assertEquals(0, r.exceededCents)
    }

    @Test
    fun `进度不会为负`() {
        val r = ProgressCalculator.calculate(-5_000, 100_000)
        assertEquals(0f, r.progress, 0.0001f)
        assertEquals(0, r.percent)
        assertEquals(100_000, r.remainingCents)
    }

    @Test
    fun `进度不会超过100并且显示真实累计`() {
        val r = ProgressCalculator.calculate(120_000, 100_000)
        assertEquals(1f, r.progress, 0.0001f)
        assertEquals(100, r.percent)
        assertEquals(0, r.remainingCents)
        assertEquals(20_000, r.exceededCents)
        assertTrue(r.isCompleted)
    }

    @Test
    fun `零进度`() {
        val r = ProgressCalculator.calculate(0, 100_000)
        assertEquals(0f, r.progress, 0.0001f)
        assertEquals(0, r.percent)
        assertEquals(100_000, r.remainingCents)
        assertFalse(r.isCompleted)
    }

    @Test
    fun `百分比四舍五入为整数`() {
        // 34.5% -> 35（HALF_UP）
        assertEquals(35, ProgressCalculator.calculate(34_500, 100_000).percent)
        // 33.4% -> 33
        assertEquals(33, ProgressCalculator.calculate(33_400, 100_000).percent)
        // 99.5% -> 100，但进度仍未封顶
        val r = ProgressCalculator.calculate(99_500, 100_000)
        assertEquals(100, r.percent)
        assertTrue(r.progress < 1f)
        assertEquals(500, r.remainingCents)
    }

    @Test
    fun `目标金额非法时进度为零`() {
        val r = ProgressCalculator.calculate(500, 0)
        assertEquals(0f, r.progress, 0.0001f)
        assertEquals(0, r.percent)
    }

    @Test
    fun `剩余天数计算`() {
        assertEquals(5L, ProgressCalculator.remainingDays(105, 100))
        assertEquals(0L, ProgressCalculator.remainingDays(100, 100))
        assertEquals(-3L, ProgressCalculator.remainingDays(97, 100))
        assertNull(ProgressCalculator.remainingDays(null, 100))
    }

    @Test
    fun `safeProgress收敛`() {
        assertEquals(0f, ProgressCalculator.safeProgress(-0.5f), 0f)
        assertEquals(1f, ProgressCalculator.safeProgress(1.5f), 0f)
        assertEquals(0.5f, ProgressCalculator.safeProgress(0.5f), 0f)
    }
}
