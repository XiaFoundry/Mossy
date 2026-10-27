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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.savings.buddy.business.Money
import com.savings.buddy.data.AppContainer
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.components.AllocationDialog
import com.savings.buddy.ui.components.RingProgress
import com.savings.buddy.ui.components.ringColorFor
import java.time.LocalDate

/** 首页：全目标聚合环状进度图 */
@Composable
fun HomeScreen(
    container: AppContainer,
    onCreateGoal: () -> Unit,
) {
    val vm: HomeViewModel = viewModel(factory = simpleViewModelFactory { HomeViewModel(container) })
    val state by vm.state.collectAsState()

    if (!state.hasGoals) {
        EmptyHome(onCreateGoal = onCreateGoal)
        return
    }

    var showAllocationDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now().toEpochDay()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Mossy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                        RingProgress(
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
                                    Money.format(state.savedTotalCents),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "已攒 / ${Money.format(state.targetTotalCents)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "全部目标（${state.goalCount} 个，${state.activeGoalCount} 个进行中）",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "剩余 ${Money.format(progress?.remainingCents ?: 0)}" +
                            if ((progress?.exceededCents ?: 0) > 0) {
                                " · 超额 ${Money.format(progress!!.exceededCents)}"
                            } else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Button(
                onClick = { showAllocationDialog = true },
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
                        Text("累计攒钱", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Money.format(state.allTotalCents), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (showAllocationDialog) {
        val activeGoals = state.goals.filter {
            it.status == com.savings.buddy.data.GoalStatus.IN_PROGRESS
        }
        AllocationDialog(
            goals = activeGoals,
            defaultGoalId = state.mostSavedGoalId,
            todayEpochDay = today,
            onDismiss = { showAllocationDialog = false },
            onSave = { allocations, note, day ->
                vm.addAllocation(allocations, note, day)
                showAllocationDialog = false
            },
        )
    }
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
