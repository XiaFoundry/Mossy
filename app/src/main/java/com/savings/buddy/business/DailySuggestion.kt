package com.savings.buddy.business

import com.savings.buddy.data.Goal
import com.savings.buddy.data.GoalStatus
import com.savings.buddy.data.PlanType
import kotlin.math.max

/**
 * 每日建议金额。
 * - 固定金额：直接取计划值，修改计划只影响后续建议。
 * - 目标倒推：ceil(剩余金额 / 剩余天数)，整数分运算。
 * 边界：无截止日期→0（创建时应阻止）；今天截止→剩余全额；已逾期→0（不生成负数建议）；
 *       已完成→0；暂停/归档→0（不参与每日建议）。
 */
object DailySuggestion {

    fun cents(goal: Goal, savedCents: Long, todayEpochDay: Long): Long {
        if (goal.status != GoalStatus.IN_PROGRESS) return 0
        if (savedCents >= goal.targetCents) return 0
        return when (goal.planType) {
            PlanType.FIXED -> max(0L, goal.dailyFixedCents)
            PlanType.REVERSE -> {
                val deadline = goal.deadlineEpochDay ?: return 0
                val remaining = goal.targetCents - savedCents
                val days = deadline - todayEpochDay
                when {
                    days < 0 -> 0L
                    days == 0L -> remaining
                    else -> (remaining + days - 1) / days
                }
            }
        }
    }

    /** 是否已逾期（进行中且未完成且截止日期已过） */
    fun isOverdue(goal: Goal, savedCents: Long, todayEpochDay: Long): Boolean {
        if (goal.status != GoalStatus.IN_PROGRESS) return false
        if (savedCents >= goal.targetCents) return false
        val deadline = goal.deadlineEpochDay ?: return false
        return deadline < todayEpochDay
    }

    /** 逾期天数，未逾期返回 0 */
    fun overdueDays(goal: Goal, savedCents: Long, todayEpochDay: Long): Long =
        if (isOverdue(goal, savedCents, todayEpochDay)) todayEpochDay - (goal.deadlineEpochDay ?: todayEpochDay) else 0L
}
