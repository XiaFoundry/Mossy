package com.savings.buddy

import com.savings.buddy.business.DailySuggestion
import com.savings.buddy.data.Goal
import com.savings.buddy.data.GoalStatus
import com.savings.buddy.data.PlanType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailySuggestionTest {

    private fun goal(
        target: Long = 100_000,
        plan: PlanType = PlanType.FIXED,
        daily: Long = 2_000,
        deadline: Long? = null,
        status: GoalStatus = GoalStatus.IN_PROGRESS,
    ) = Goal(
        id = 1, name = "测试", targetCents = target, planType = plan,
        dailyFixedCents = daily, deadlineEpochDay = deadline, status = status, createdAtEpochDay = 0,
    )

    // ---------- 固定金额 ----------
    @Test
    fun `固定金额模式返回计划值`() {
        assertEquals(2_000, DailySuggestion.cents(goal(), savedCents = 35_000, todayEpochDay = 100))
    }

    @Test
    fun `修改固定计划不影响已完成判定`() {
        // 计划金额为 0 也不产生负数
        assertEquals(0, DailySuggestion.cents(goal(daily = -5), 0, 100))
    }

    // ---------- 目标倒推 ----------
    @Test
    fun `倒推按剩余金额与天数向上取整`() {
        // 剩余 100000 分，剩 3 天 -> ceil(100000/3)=33334
        val g = goal(plan = PlanType.REVERSE, deadline = 103)
        assertEquals(33_334, DailySuggestion.cents(g, savedCents = 0, todayEpochDay = 100))
        // 整除时不进位：剩余 90000，剩 3 天 -> 30000
        assertEquals(30_000, DailySuggestion.cents(g, savedCents = 10_000, todayEpochDay = 100))
    }

    @Test
    fun `截止日期为当天时建议为剩余金额`() {
        val g = goal(plan = PlanType.REVERSE, deadline = 100)
        assertEquals(65_000, DailySuggestion.cents(g, savedCents = 35_000, todayEpochDay = 100))
    }

    @Test
    fun `已逾期不生成负数建议`() {
        val g = goal(plan = PlanType.REVERSE, deadline = 97)
        assertEquals(0, DailySuggestion.cents(g, savedCents = 35_000, todayEpochDay = 100))
        assertTrue(DailySuggestion.isOverdue(g, 35_000, 100))
        assertEquals(3, DailySuggestion.overdueDays(g, 35_000, 100))
    }

    @Test
    fun `没有截止日期时倒推返回零`() {
        val g = goal(plan = PlanType.REVERSE, deadline = null)
        assertEquals(0, DailySuggestion.cents(g, 0, 100))
    }

    @Test
    fun `目标已完成建议为零`() {
        val g = goal(plan = PlanType.REVERSE, deadline = 110)
        assertEquals(0, DailySuggestion.cents(g, savedCents = 100_000, todayEpochDay = 100))
        assertFalse(DailySuggestion.isOverdue(g, 100_000, 100))
    }

    @Test
    fun `超额完成建议为零`() {
        val g = goal(plan = PlanType.FIXED)
        assertEquals(0, DailySuggestion.cents(g, savedCents = 120_000, todayEpochDay = 100))
    }

    // ---------- 状态边界 ----------
    @Test
    fun `暂停与归档不产生建议`() {
        assertEquals(0, DailySuggestion.cents(goal(status = GoalStatus.PAUSED), 0, 100))
        assertEquals(0, DailySuggestion.cents(goal(status = GoalStatus.ARCHIVED), 0, 100))
    }

    @Test
    fun `已完成状态不产生建议也不算逾期`() {
        val g = goal(status = GoalStatus.COMPLETED, plan = PlanType.REVERSE, deadline = 97)
        assertEquals(0, DailySuggestion.cents(g, 0, 100))
        assertFalse(DailySuggestion.isOverdue(g, 0, 100))
    }
}
