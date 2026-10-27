package com.savings.buddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.savings.buddy.business.Money
import com.savings.buddy.data.Goal
import com.savings.buddy.data.SavingRecord
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 记录攒钱对话框：新建与编辑共用。
 * 金额必须大于零、最多两位小数；支持快捷金额与补记（不允许未来日期）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDialog(
    goals: List<Goal>,
    activeGoal: Goal?,
    editing: SavingRecord?,
    defaultSuggestionCents: Long,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSave: (goalId: Long, amountCents: Long, note: String, saveEpochDay: Long) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var amountText by remember {
        mutableStateOf(editing?.let { Money.toYuanInput(it.amountCents) } ?: "")
    }
    var note by remember { mutableStateOf(editing?.note ?: "") }
    var goalId by remember {
        mutableStateOf(editing?.goalId ?: activeGoal?.id ?: goals.firstOrNull()?.id ?: 0L)
    }
    var saveDay by remember { mutableStateOf(editing?.saveEpochDay ?: todayEpochDay) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var showGoalMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedGoal = goals.find { it.id == goalId }
    val quickAmounts = buildList {
        if (defaultSuggestionCents > 0) add("建议 " to defaultSuggestionCents)
        addAll(listOf(1_000L, 2_000L, 5_000L, 10_000L).map { Money.format(it) to it })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "记录攒钱" else "修改记录") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (goals.size > 1) {
                    Column {
                        OutlinedButton(onClick = { showGoalMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("目标：${selectedGoal?.name ?: "请选择"}")
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = showGoalMenu, onDismissRequest = { showGoalMenu = false }) {
                            goals.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.name) },
                                    onClick = { goalId = g.id; showGoalMenu = false },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; error = null },
                    label = { Text("金额（元）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAmounts.forEach { (label, cents) ->
                        FilterChip(
                            selected = Money.toYuanInput(cents) == amountText.trim(),
                            onClick = { amountText = Money.toYuanInput(cents); error = null },
                            label = { Text(label) },
                        )
                    }
                }

                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("日期：${LocalDate.ofEpochDay(saveDay)}" + if (saveDay == todayEpochDay) "（今天）" else "")
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                )

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !submitted,
                onClick = {
                    val cents = Money.parseToCents(amountText)
                    when {
                        cents == null -> error = "金额格式不正确，最多两位小数"
                        cents <= 0 -> error = "金额必须大于零"
                        saveDay > todayEpochDay -> error = "不能选择未来日期"
                        goalId <= 0 -> error = "请选择目标"
                        else -> {
                            submitted = true
                            onSave(goalId, cents, note.trim(), saveDay)
                        }
                    }
                },
            ) { Text("保存") }
        },
        dismissButton = {
            Row {
                if (editing != null && onDelete != null) {
                    TextButton(
                        enabled = !submitted,
                        onClick = { submitted = true; onDelete() },
                    ) { Text("删除", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        },
    )

    if (showDatePicker) {
        val pickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = saveDay * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        saveDay = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
