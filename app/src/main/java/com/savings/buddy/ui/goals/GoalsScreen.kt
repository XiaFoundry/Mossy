package com.savings.buddy.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.business.GoalDisplay
import com.savings.buddy.business.GoalDisplayCalc
import com.savings.buddy.business.Money
import com.savings.buddy.business.ProgressCalculator
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.data.GoalStatus
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.components.ringColorFor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GoalRow(
    val goal: Goal,
    val savedCents: Long,
    val progress: Float,
    val percent: Int,
    val display: GoalDisplay,
)

data class GoalsUiState(val rows: List<GoalRow> = emptyList())

class GoalsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.repository

    val state = combine(repo.observeGoals(), repo.observeRecords()) { goals, records ->
        val today = LocalDate.now().toEpochDay()
        GoalsUiState(
            rows = goals.map { g ->
                val saved = records.filter { it.goalId == g.id }.sumOf { it.amountCents }
                val p = ProgressCalculator.calculate(saved, g.targetCents)
                GoalRow(
                    goal = g,
                    savedCents = saved,
                    progress = p.progress,
                    percent = p.percent,
                    display = GoalDisplayCalc.of(g, saved, today),
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalsUiState())

    fun togglePause(row: GoalRow) = viewModelScope.launch {
        val newStatus = if (row.goal.status == GoalStatus.PAUSED) GoalStatus.IN_PROGRESS else GoalStatus.PAUSED
        repo.setGoalStatus(row.goal, newStatus)
    }

    fun archive(row: GoalRow) = viewModelScope.launch { repo.setGoalStatus(row.goal, GoalStatus.ARCHIVED) }

    /** 取消归档：恢复为进行中 */
    fun unarchive(row: GoalRow) = viewModelScope.launch { repo.setGoalStatus(row.goal, GoalStatus.IN_PROGRESS) }

    fun delete(goal: Goal) = viewModelScope.launch { repo.deleteGoal(goal.id) }
}

@Composable
fun GoalsScreen(
    container: AppContainer,
    onEditGoal: (Long) -> Unit,
    onCreateGoal: () -> Unit,
) {
    val vm: GoalsViewModel = viewModel(factory = simpleViewModelFactory { GoalsViewModel(container) })
    val state by vm.state.collectAsState()
    var menuFor by remember { mutableStateOf<GoalRow?>(null) }
    var confirmDelete by remember { mutableStateOf<Goal?>(null) }
    var archivedExpanded by rememberSaveable { mutableStateOf(false) }

    val activeRows = state.rows.filter { it.goal.status != GoalStatus.ARCHIVED }
    val archivedRows = state.rows.filter { it.goal.status == GoalStatus.ARCHIVED }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onCreateGoal) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  新建目标")
            }
        },
    ) { padding ->
        if (state.rows.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("还没有储蓄目标", style = MaterialTheme.typography.titleMedium)
                Text(
                    "点击右下角创建第一个目标",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(activeRows.size, key = { activeRows[it].goal.id }) { i ->
                    GoalCard(
                        row = activeRows[i],
                        menuExpanded = menuFor?.goal?.id == activeRows[i].goal.id,
                        onMenuExpandedChange = { if (!it) menuFor = null },
                        onCardClick = { onEditGoal(activeRows[i].goal.id) },
                        onMenu = { menuFor = activeRows[i] },
                        onPauseResume = { vm.togglePause(activeRows[i]) },
                        onArchive = { vm.archive(activeRows[i]) },
                        onUnarchive = { vm.unarchive(activeRows[i]) },
                        onEdit = { onEditGoal(activeRows[i].goal.id) },
                        onDelete = { confirmDelete = activeRows[i].goal },
                    )
                }

                // 归档折叠区
                if (archivedRows.isNotEmpty()) {
                    item(key = "archived_header") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { archivedExpanded = !archivedExpanded },
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "归档",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "${archivedRows.size} 个目标",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = if (archivedExpanded) "收起" else "展开",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                    if (archivedExpanded) {
                        items(archivedRows.size, key = { archivedRows[it].goal.id }) { i ->
                            GoalCard(
                                row = archivedRows[i],
                                menuExpanded = menuFor?.goal?.id == archivedRows[i].goal.id,
                                onMenuExpandedChange = { if (!it) menuFor = null },
                                onCardClick = { onEditGoal(archivedRows[i].goal.id) },
                                onMenu = { menuFor = archivedRows[i] },
                                onPauseResume = { vm.togglePause(archivedRows[i]) },
                                onArchive = { vm.archive(archivedRows[i]) },
                                onUnarchive = { vm.unarchive(archivedRows[i]) },
                                onEdit = { onEditGoal(archivedRows[i].goal.id) },
                                onDelete = { confirmDelete = archivedRows[i].goal },
                            )
                        }
                    }
                }
            }
        }
    }

    // 删除确认
    confirmDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("删除目标「${goal.name}」？") },
            text = { Text("该目标下的所有攒钱流水将被一并删除，且无法恢复。建议先导出备份。") },
            confirmButton = {
                TextButton(onClick = { vm.delete(goal); confirmDelete = null }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun GoalCard(
    row: GoalRow,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onCardClick: () -> Unit,
    onMenu: () -> Unit,
    onPauseResume: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 状态色点：随进度从红色逐渐过渡到绿色
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(ringColorFor(row.progress)),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    row.goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    GoalDisplayCalc.label(row.display),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (row.display == GoalDisplay.OVERDUE) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // 菜单锚点：DropdownMenu 放在按钮所在的 Box 内，展开位置紧贴按钮
                Box {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多操作")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { onMenuExpandedChange(false) },
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (row.goal.status == GoalStatus.PAUSED) "恢复进行" else "暂停") },
                            onClick = { onPauseResume(); onMenuExpandedChange(false) },
                        )
                        if (row.goal.status == GoalStatus.ARCHIVED) {
                            DropdownMenuItem(
                                text = { Text("取消归档") },
                                onClick = { onUnarchive(); onMenuExpandedChange(false) },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("归档") },
                                onClick = { onArchive(); onMenuExpandedChange(false) },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = { onEdit(); onMenuExpandedChange(false) },
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            onClick = { onDelete(); onMenuExpandedChange(false) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { row.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = ringColorFor(row.progress),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Text(
                    "${Money.format(row.savedCents)} / ${Money.format(row.goal.targetCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${row.percent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            row.goal.deadlineEpochDay?.let {
                Text(
                    "截止 ${LocalDate.ofEpochDay(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** 目标主题颜色（与 colorIndex 对应） */
fun goalColor(index: Int): Color = when (((index % 6) + 6) % 6) {
    0 -> Color(0xFFE4715C)
    1 -> Color(0xFFE0A63A)
    2 -> Color(0xFF35A08F)
    3 -> Color(0xFF5B8DEF)
    4 -> Color(0xFF9C6ADE)
    else -> Color(0xFF2E9E4F)
}
