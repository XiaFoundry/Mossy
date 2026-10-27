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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.business.Money
import com.savings.buddy.business.RecordBatch
import com.savings.buddy.business.RecordBatches
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.data.RecordWithGoal
import com.savings.buddy.data.SavingRecord
import com.savings.buddy.ui.common.dateLabel
import com.savings.buddy.ui.common.fullDateLabel
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.common.timeLabel
import com.savings.buddy.ui.components.RecordDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class RecordsUiState(
    val goals: List<Goal> = emptyList(),
    val batches: List<RecordBatch> = emptyList(),
    val filterGoalId: Long? = null,
)

class RecordsViewModel(private val container: AppContainer) : ViewModel() {
    private val repo = container.repository
    private val filter = MutableStateFlow<Long?>(null)

    val state = combine(repo.observeGoals(), repo.observeRecordsWithGoal(), filter) { goals, all, f ->
        val filtered = if (f == null) all else all.filter { it.record.goalId == f }
        RecordsUiState(
            goals = goals,
            batches = RecordBatches.group(filtered),
            filterGoalId = f,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordsUiState())

    fun setFilter(goalId: Long?) { filter.value = goalId }

    fun addRecord(goalId: Long, cents: Long, note: String, day: Long) {
        viewModelScope.launch {
            repo.addRecord(goalId, cents, note, day)
            container.achievementManager.onRecordsChanged()
        }
    }

    fun updateRecord(record: SavingRecord, amountCents: Long, note: String, day: Long, goalId: Long) {
        viewModelScope.launch {
            repo.updateRecord(record.copy(amountCents = amountCents, note = note, saveEpochDay = day, goalId = goalId))
            container.achievementManager.onRecordsChanged()
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repo.deleteRecord(id)
            container.achievementManager.onRecordsChanged()
        }
    }
}

@Composable
fun RecordsScreen(
    container: AppContainer,
    onOpenDetail: (Long) -> Unit = {},
) {
    val vm: RecordsViewModel = viewModel(factory = simpleViewModelFactory { RecordsViewModel(container) })
    val state by vm.state.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<RecordWithGoal?>(null) }
    var expandedBatches by rememberSaveable { mutableStateOf(setOf<String>()) }
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
                    FilterChip(
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

            if (state.batches.isEmpty()) {
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
                    items(state.batches.size, key = { state.batches[it].batchId }) { i ->
                        val batch = state.batches[i]
                        RecordBatchCard(
                            batch = batch,
                            todayEpochDay = today,
                            expanded = batch.batchId in expandedBatches,
                            onToggleExpand = {
                                expandedBatches = if (batch.batchId in expandedBatches) {
                                    expandedBatches - batch.batchId
                                } else {
                                    expandedBatches + batch.batchId
                                }
                            },
                            onOpenDetail = onOpenDetail,
                        )
                    }
                }
            }
        }
    }

    if (adding) {
        RecordDialog(
            goals = state.goals,
            editing = null,
            todayEpochDay = today,
            onDismiss = { adding = false },
            onSave = { goalId, cents, note, day -> vm.addRecord(goalId, cents, note, day); adding = false },
        )
    }

    editing?.let { item ->
        RecordDialog(
            goals = state.goals,
            editing = item.record,
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

/** 单条记录展示（点击进入详情页） */
@Composable
private fun RecordRow(
    item: RecordWithGoal,
    todayEpochDay: Long,
    onOpenDetail: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(item.record.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fullDateLabel(item.record.saveEpochDay) + " " + timeLabel(item.record.createdAtMillis),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                item.goalName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (item.record.note.isNotBlank()) {
                Text(
                    item.record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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

/** 批次卡片：单笔直接展示；多笔作为一个整体，点击展开查看各条 */
@Composable
private fun RecordBatchCard(
    batch: RecordBatch,
    todayEpochDay: Long,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    if (!batch.isMulti) {
        Card(modifier = Modifier.fillMaxWidth()) {
            RecordRow(batch.items.first(), todayEpochDay, onOpenDetail)
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                // 批次头：整体信息，点击展开/收起
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpand)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                dateLabel(batch.items.first().record.saveEpochDay, todayEpochDay) +
                                    " " + timeLabel(batch.createdAtMillis),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = if (expanded) "收起" else "展开",
                            )
                        }
                        Text(
                            "一次分配 · ${batch.items.size} 个目标",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "+" + Money.format(batch.totalCents),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                // 展开的单条明细
                if (expanded) {
                    HorizontalDivider()
                    batch.items.forEach { item ->
                        RecordRow(item, todayEpochDay, onOpenDetail)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
