package com.savings.buddy

import com.savings.buddy.business.Money
import com.savings.buddy.business.RingColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MoneyTest {

    @Test
    fun `格式化整数元`() {
        assertEquals("¥0", Money.format(0))
        assertEquals("¥350", Money.format(35_000))
        assertEquals("¥1,000", Money.format(100_000))
        assertEquals("¥12,345", Money.format(1_234_500))
    }

    @Test
    fun `格式化带小数`() {
        assertEquals("¥12.34", Money.format(1_234))
        assertEquals("¥0.05", Money.format(5))
        assertEquals("¥1,234.50", Money.format(123_450))
    }

    @Test
    fun `格式化负数`() {
        assertEquals("¥-12.34", Money.format(-1_234))
    }

    @Test
    fun `解析合法输入`() {
        assertEquals(1234L, Money.parseToCents("12.34"))
        assertEquals(5L, Money.parseToCents("0.05"))
        assertEquals(35000L, Money.parseToCents("350"))
        assertEquals(1230L, Money.parseToCents("12.3"))
        assertEquals(0L, Money.parseToCents("0"))
    }

    @Test
    fun `拒绝非法输入`() {
        assertNull(Money.parseToCents(""))
        assertNull(Money.parseToCents("abc"))
        assertNull(Money.parseToCents("-5"))
        assertNull(Money.parseToCents("1.234"))
        assertNull(Money.parseToCents("1,000"))
        assertNull(Money.parseToCents(".5"))
        assertNull(Money.parseToCents("12."))
    }

    @Test
    fun `分转元输入字符串往返`() {
        assertEquals("12.34", Money.toYuanInput(1234))
        assertEquals("350", Money.toYuanInput(35000))
        assertEquals("0.05", Money.toYuanInput(5))
        assertEquals("", Money.toYuanInput(0))
    }
}

class RingColorsTest {

    @Test
    fun `端点与关键帧一致`() {
        val stops = RingColors.STOPS
        stops.forEach { (p, rgb) ->
            val c = RingColors.colorAt(p)
            assertTrue(abs(c.r - rgb.r) < 1e-6)
            assertTrue(abs(c.g - rgb.g) < 1e-6)
            assertTrue(abs(c.b - rgb.b) < 1e-6)
        }
    }

    @Test
    fun `进度收敛到0到1`() {
        // 超界不崩溃且颜色等于端点
        val cNeg = RingColors.colorAt(-0.5)
        val cOver = RingColors.colorAt(1.5)
        assertEquals(RingColors.colorAt(0.0).r, cNeg.r, 1e-9)
        assertEquals(RingColors.colorAt(1.0).r, cOver.r, 1e-9)
    }

    @Test
    fun `亮度单调递增无突变`() {
        // OKLab 亮度应随进度整体上升（允许极小的数值波动）
        var prev = RingColors.lightnessAt(0.0)
        var maxDrop = 0.0
        var i = 1
        while (i <= 100) {
            val l = RingColors.lightnessAt(i / 100.0)
            maxDrop = maxOf(maxDrop, prev - l)
            prev = l
            i++
        }
        assertTrue("亮度出现明显回落: $maxDrop", maxDrop < 0.02)
    }

    @Test
    fun `argb输出合法`() {
        val argb = RingColors.argbAt(0.5)
        assertEquals(0xFF, (argb ushr 24) and 0xFF)
        assertTrue((argb and 0xFFFFFF) != 0)
    }
}
