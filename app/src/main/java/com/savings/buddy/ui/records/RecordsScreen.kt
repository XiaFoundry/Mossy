package com.savings.buddy.ui.records

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.business.Money
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.data.RecordWithGoal
import com.savings.buddy.data.SavingRecord
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.components.RecordDialog
import com.savings.buddy.ui.home.dateLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RecordsUiState(
    val goals: List<Goal> = emptyList(),
    val records: List<RecordWithGoal> = emptyList(),
    val filterGoalId: Long? = null,
)

class RecordsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.repository
    private val filter = MutableStateFlow<Long?>(null)

    val state = combine(repo.observeGoals(), repo.observeRecordsWithGoal(), filter) { goals, all, f ->
        RecordsUiState(
            goals = goals,
            records = if (f == null) all else all.filter { it.record.goalId == f },
            filterGoalId = f,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())

    fun setFilter(goalId: Long?) { filter.value = goalId }

    fun addRecord(goalId: Long, cents: Long, note: String, day: Long) {
        viewModelScope.launch { repo.addRecord(goalId, cents, note, day) }
    }

    fun updateRecord(record: SavingRecord, amountCents: Long, note: String, day: Long, goalId: Long) {
        viewModelScope.launch {
            repo.updateRecord(record.copy(amountCents = amountCents, note = note, saveEpochDay = day, goalId = goalId))
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch { repo.deleteRecord(id) }
    }
}

@Composable
fun RecordsScreen(container: AppContainer) {
    val vm: RecordsViewModel = viewModel(factory = simpleViewModelFactory { RecordsViewModel(container) })
    val state by vm.state.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecordWithGoal?>(null) }
    val today = LocalDate.now().toEpochDay()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { adding = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  记一笔")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 顶栏：标题 + 目标筛选
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("历史记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box {
                    androidx.compose.material3.FilterChip(
                        selected = state.filterGoalId != null,
                        onClick = { showFilterMenu = true },
                        label = {
                            Text(
                                state.goals.find { it.id == state.filterGoalId }?.name ?: "全部目标"
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.FilterList, contentDescription = null) },
                    )
                    DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                        DropdownMenuItem(text = { Text("全部目标") }, onClick = { vm.setFilter(null); showFilterMenu = false })
                        state.goals.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g.name) },
                                onClick = { vm.setFilter(g.id); showFilterMenu = false },
                            )
                        }
                    }
                }
            }

            if (state.records.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("暂无记录", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "点击右下角「记一笔」开始记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.records.size, key = { state.records[it].record.id }) { i ->
                        val item = state.records[i]
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { editing = item },
                        ) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Row {
                                        Text(
                                            dateLabel(item.record.saveEpochDay, today),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Spacer(Modifier.size(8.dp))
                                        Text(
                                            item.goalName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    if (item.record.note.isNotBlank()) {
                                        Text(
                                            item.record.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Text(
                                    "+" + Money.format(item.record.amountCents),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        RecordDialog(
            goals = state.goals,
            activeGoal = null,
            editing = null,
            defaultSuggestionCents = 0,
            todayEpochDay = today,
            onDismiss = { adding = false },
            onSave = { goalId, cents, note, day -> vm.addRecord(goalId, cents, note, day); adding = false },
        )
    }

    editing?.let { item ->
        RecordDialog(
            goals = state.goals,
            activeGoal = state.goals.find { it.id == item.record.goalId },
            editing = item.record,
            defaultSuggestionCents = 0,
            todayEpochDay = today,
            onDismiss = { editing = null },
            onSave = { goalId, cents, note, day ->
                vm.updateRecord(item.record, cents, note, day, goalId)
                editing = null
            },
            onDelete = { vm.deleteRecord(item.record.id); editing = null },
        )
    }
}
