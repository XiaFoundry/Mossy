package com.savings.buddy.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.savings.buddy.ui.common.fullDateLabel
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.common.timeLabel
import com.savings.buddy.ui.components.RecordDialog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecordDetailViewModel(
    private val container: AppContainer,
    recordId: Long,
) : ViewModel() {

    private val repo = container.repository

    val record = repo.observeRecordById(recordId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val goals = repo.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList<Goal>())

    fun update(record: RecordWithGoal, amountCents: Long, note: String, day: Long, goalId: Long) {
        viewModelScope.launch {
            repo.updateRecord(record.record.copy(amountCents = amountCents, note = note, saveEpochDay = day, goalId = goalId))
            container.achievementManager.onRecordsChanged()
        }
    }

    fun delete(id: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repo.deleteRecord(id)
            container.achievementManager.onRecordsChanged()
            onDeleted()
        }
    }
}

/** 记录详情页：展示单条流水的完整信息，右上角编辑图标可修改/删除 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    container: AppContainer,
    recordId: Long,
    onBack: () -> Unit,
) {
    val vm: RecordDetailViewModel = viewModel(
        key = "record_$recordId",
        factory = simpleViewModelFactory { RecordDetailViewModel(container, recordId) },
    )
    val item by vm.record.collectAsState()
    val goals by vm.goals.collectAsState()
    var showEdit by remember { mutableStateOf(false) }
    val today = LocalDate.now().toEpochDay()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 编辑入口（矢量图标），等效「修改记录」
                    IconButton(onClick = { if (item != null) showEdit = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "修改记录")
                    }
                },
            )
        },
    ) { padding ->
        val record = item
        if (record == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("记录不存在或已被删除", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "+" + Money.format(record.record.amountCents),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            record.goalName,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow("日期", fullDateLabel(record.record.saveEpochDay))
                        HorizontalDivider()
                        DetailRow("时间", timeLabel(record.record.createdAtMillis))
                        HorizontalDivider()
                        DetailRow("目标", record.goalName)
                        HorizontalDivider()
                        DetailRow("增加数额", Money.format(record.record.amountCents))
                        HorizontalDivider()
                        DetailRow("备注", record.record.note.ifBlank { "无" })
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showEdit && item != null) {
        RecordDialog(
            goals = goals,
            editing = item!!.record,
            todayEpochDay = today,
            onDismiss = { showEdit = false },
            onSave = { goalId, cents, note, day ->
                vm.update(item!!, cents, note, day, goalId)
                showEdit = false
            },
            onDelete = {
                val id = item!!.record.id
                showEdit = false
                vm.delete(id, onDeleted = onBack)
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.3f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.7f),
        )
    }
}
