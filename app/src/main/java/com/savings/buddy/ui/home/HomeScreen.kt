package com.savings.buddy.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.business.GoalDisplay
import com.savings.buddy.business.GoalDisplayCalc
import com.savings.buddy.business.Money
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.components.RecordDialog
import com.savings.buddy.ui.components.ringColorFor
import java.time.LocalDate

@Composable
fun HomeScreen(
    container: AppContainer,
    onCreateGoal: () -> Unit,
    onGoRecords: () -> Unit,
) {
    val vm: HomeViewModel = viewModel(factory = simpleViewModelFactory { HomeViewModel(container) })
    val state by vm.state.collectAsState()

    if (!state.hasGoal) {
        EmptyHome(onCreateGoal = onCreateGoal)
        return
    }

    var showRecordDialog by remember { mutableStateOf(false) }
    var showGoalMenu by remember { mutableStateOf(false) }
    val today = LocalDate.now().toEpochDay()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("攒钱小助手", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box {
                    AssistChip(
                        onClick = { showGoalMenu = true },
                        label = { Text(state.goal?.name ?: "") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = "切换目标") },
                    )
                    DropdownMenu(expanded = showGoalMenu, onDismissRequest = { showGoalMenu = false }) {
                        state.goals.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                leadingIcon = {
                                    if (g.id == state.goal?.id) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                    }
                                },
                                onClick = { vm.setCurrentGoal(g.id); showGoalMenu = false },
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val progress = state.progress
                    Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                        com.savings.buddy.ui.components.RingProgress(
                            progress = progress?.progress ?: 0f,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${progress?.percent ?: 0}%",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ringColorFor(progress?.progress ?: 0f),
                                )
                                Text(
                                    Money.format(progress?.savedCents ?: 0),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "已攒 / ${Money.format(progress?.targetCents ?: 0)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            state.goal?.name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.size(8.dp))
                        StatusChip(state.display, state.overdueDays)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "剩余 ${Money.format(progress?.remainingCents ?: 0)}" +
                            (state.goal?.deadlineEpochDay?.let {
                                " · 截止 ${LocalDate.ofEpochDay(it)}"
                            } ?: ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if ((progress?.exceededCents ?: 0) > 0) {
                        Text(
                            "超额完成 ${Money.format(progress!!.exceededCents)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("今日建议", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    val text = when (state.display) {
                        GoalDisplay.RUNNING -> Money.format(state.suggestionCents) + " / 天"
                        GoalDisplay.COMPLETED -> "目标已达成，保持就好"
                        GoalDisplay.OVERDUE -> "已逾期 ${state.overdueDays} 天，剩余 ${Money.format(state.progress?.remainingCents ?: 0)}"
                        GoalDisplay.PAUSED -> "目标已暂停"
                        GoalDisplay.ARCHIVED -> "目标已归档"
                        GoalDisplay.EMPTY -> ""
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.display == GoalDisplay.OVERDUE) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        item {
            Button(
                onClick = { showRecordDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("记录攒钱", style = MaterialTheme.typography.titleMedium) }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("本月攒钱", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Money.format(state.monthTotalCents), style = MaterialTheme.typography.titleMedium)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("所有目标累计", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Money.format(state.allTotalCents), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("最近记录", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onGoRecords) { Text("查看全部") }
            }
        }

        if (state.recent.isEmpty()) {
            item {
                Text(
                    "还没有记录，点击「记录攒钱」开始第一笔吧",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(state.recent.size) { i ->
                val r = state.recent[i]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(dateLabel(r.saveEpochDay, today), style = MaterialTheme.typography.bodyMedium)
                            if (r.note.isNotBlank()) {
                                Text(
                                    r.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            "+" + Money.format(r.amountCents),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    if (showRecordDialog) {
        RecordDialog(
            goals = state.goals,
            activeGoal = state.goal,
            editing = null,
            defaultSuggestionCents = if (state.display == GoalDisplay.RUNNING) state.suggestionCents else 0,
            todayEpochDay = today,
            onDismiss = { showRecordDialog = false },
            onSave = { goalId, cents, note, day ->
                vm.addRecord(goalId, cents, note, day)
                showRecordDialog = false
            },
        )
    }
}

@Composable
private fun StatusChip(display: GoalDisplay, overdueDays: Long) {
    val label = when (display) {
        GoalDisplay.OVERDUE -> "已逾期 $overdueDays 天"
        else -> GoalDisplayCalc.label(display)
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
    )
}

@Composable
private fun EmptyHome(onCreateGoal: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("◎", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("还没有储蓄目标", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "创建一个目标，看着圆环一点点填满",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onCreateGoal) { Text("创建储蓄目标") }
    }
}

fun dateLabel(epochDay: Long, todayEpochDay: Long): String = when (epochDay) {
    todayEpochDay -> "今天"
    todayEpochDay - 1 -> "昨天"
    else -> LocalDate.ofEpochDay(epochDay).let { "${it.monthValue}月${it.dayOfMonth}日" }
}
