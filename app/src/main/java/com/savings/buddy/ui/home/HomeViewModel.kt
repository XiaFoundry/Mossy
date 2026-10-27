package com.savings.buddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savings.buddy.business.ProgressCalculator
import com.savings.buddy.business.ProgressResult
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 首页：所有目标的聚合攒钱状态 */
data class HomeUiState(
    val hasGoals: Boolean = false,
    /** 全部目标累计已攒（分） */
    val savedTotalCents: Long = 0,
    /** 全部目标总金额（分） */
    val targetTotalCents: Long = 0,
    val progress: ProgressResult? = null,
    /** 进行中的目标数 */
    val activeGoalCount: Int = 0,
    val goalCount: Int = 0,
    val monthTotalCents: Long = 0,
    val allTotalCents: Long = 0,
    val goals: List<Goal> = emptyList(),
    /** 已攒金额最多的进行中目标（分配对话框的默认选择） */
    val mostSavedGoalId: Long? = null,
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.repository

    val state: StateFlow<HomeUiState> = combine(
        repo.observeGoals(),
        repo.observeRecords(),
    ) { goals, records ->
        val todayDate = LocalDate.now()
        val today = todayDate.toEpochDay()
        val savedTotal = records.sumOf { it.amountCents }
        val targetTotal = goals.sumOf { it.targetCents }
        val progress = if (targetTotal > 0) {
            ProgressCalculator.calculate(savedTotal, targetTotal)
        } else null
        val activeGoals = goals.filter { it.status == com.savings.buddy.data.GoalStatus.IN_PROGRESS }
        val savedByGoal = records.groupBy { it.goalId }.mapValues { entry -> entry.value.sumOf { it.amountCents } }
        val mostSavedGoalId = activeGoals.maxByOrNull { savedByGoal[it.id] ?: 0L }?.id
            ?: activeGoals.firstOrNull()?.id
        HomeUiState(
            hasGoals = goals.isNotEmpty(),
            savedTotalCents = savedTotal,
            targetTotalCents = targetTotal,
            progress = progress,
            activeGoalCount = activeGoals.size,
            goalCount = goals.size,
            monthTotalCents = records.filter {
                val d = LocalDate.ofEpochDay(it.saveEpochDay)
                d.year == todayDate.year && d.month == todayDate.month
            }.sumOf { it.amountCents },
            allTotalCents = savedTotal,
            goals = goals,
            mostSavedGoalId = mostSavedGoalId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** 一笔攒钱分配到多个目标，保存后触发成就评估 */
    fun addAllocation(allocations: List<Pair<Long, Long>>, note: String, saveEpochDay: Long) {
        viewModelScope.launch {
            repo.addAllocatedRecords(allocations, note, saveEpochDay)
            container.achievementManager.onRecordsChanged()
        }
    }
}
