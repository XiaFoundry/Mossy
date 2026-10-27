package com.savings.buddy.business

import com.savings.buddy.data.Goal
import com.savings.buddy.data.GoalStatus

/** 目标对外展示状态（含由累计金额推导的「已完成/已逾期」） */
enum class GoalDisplay { EMPTY, RUNNING, COMPLETED, OVERDUE, PAUSED, ARCHIVED }

object GoalDisplayCalc {

    fun of(goal: Goal?, savedCents: Long, todayEpochDay: Long): GoalDisplay {
        if (goal == null) return GoalDisplay.EMPTY
        return when (goal.status) {
            GoalStatus.PAUSED -> GoalDisplay.PAUSED
            GoalStatus.ARCHIVED -> GoalDisplay.ARCHIVED
            GoalStatus.COMPLETED -> GoalDisplay.COMPLETED
            GoalStatus.IN_PROGRESS -> when {
                savedCents >= goal.targetCents -> GoalDisplay.COMPLETED
                DailySuggestion.isOverdue(goal, savedCents, todayEpochDay) -> GoalDisplay.OVERDUE
                else -> GoalDisplay.RUNNING
            }
        }
    }

    fun label(display: GoalDisplay): String = when (display) {
        GoalDisplay.EMPTY -> "无目标"
        GoalDisplay.RUNNING -> "进行中"
        GoalDisplay.COMPLETED -> "已达成"
        GoalDisplay.OVERDUE -> "已逾期"
        GoalDisplay.PAUSED -> "已暂停"
        GoalDisplay.ARCHIVED -> "已归档"
    }
}
