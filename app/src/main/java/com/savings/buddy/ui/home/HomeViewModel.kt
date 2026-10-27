package com.savings.buddy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savings.buddy.business.DailySuggestion
import com.savings.buddy.business.GoalDisplay
import com.savings.buddy.business.GoalDisplayCalc
import com.savings.buddy.business.ProgressCalculator
import com.savings.buddy.business.ProgressResult
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.data.SavingRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val hasGoal: Boolean = false,
    val goal: Goal? = null,
    val progress: ProgressResult? = null,
    val display: GoalDisplay = GoalDisplay.EMPTY,
    val suggestionCents: Long = 0,
    val overdueDays: Long = 0,
    val recent: List<SavingRecord> = emptyList(),
    val monthTotalCents: Long = 0,
    val allTotalCents: Long = 0,
    val goals: List<Goal> = emptyList(),
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.repository

    val state: StateFlow<HomeUiState> = combine(
        repo.currentGoalId,
        repo.observeGoals(),
        repo.observeRecords(),
    ) { currentId, goals, records ->
        val today = LocalDate.now().toEpochDay()
        val goal = goals.find { it.id == currentId }
            ?: goals.firstOrNull { it.status == com.savings.buddy.data.GoalStatus.IN_PROGRESS }
            ?: goals.firstOrNull()
        if (goal == null) {
            HomeUiState(goals = goals)
        } else {
            val saved = records.filter { it.goalId == goal.id }.sumOf { it.amountCents }
            val progress = ProgressCalculator.calculate(saved, goal.targetCents)
            val display = GoalDisplayCalc.of(goal, saved, today)
            val todayDate = LocalDate.ofEpochDay(today)
            HomeUiState(
                hasGoal = true,
                goal = goal,
                progress = progress,
                display = display,
                suggestionCents = DailySuggestion.cents(goal, saved, today),
                overdueDays = DailySuggestion.overdueDays(goal, saved, today),
                recent = records.filter { it.goalId == goal.id }.take(3),
                monthTotalCents = records.filter {
                    val d = LocalDate.ofEpochDay(it.saveEpochDay)
                    d.year == todayDate.year && d.month == todayDate.month
                }.sumOf { it.amountCents },
                allTotalCents = records.sumOf { it.amountCents },
                goals = goals,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun setCurrentGoal(id: Long) {
        viewModelScope.launch { repo.setCurrentGoal(id) }
    }

    /** 防重复提交：对话框提交后立即关闭，且保存动作一次性执行 */
    fun addRecord(goalId: Long, amountCents: Long, note: String, saveEpochDay: Long) {
        viewModelScope.launch {
            repo.addRecord(goalId, amountCents, note, saveEpochDay)
        }
    }
}
