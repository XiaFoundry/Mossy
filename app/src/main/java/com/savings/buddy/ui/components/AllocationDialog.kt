package com.savings.buddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.savings.buddy.business.Money
import com.savings.buddy.data.Goal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 攒钱分配对话框：一笔攒钱按金额分配到多个目标。
 * - 快捷候选固定 10/20/50/100 元，点击后平均分配到各目标
 * - 各目标金额可单独调整，要求「已分配 = 总金额」且至少一项大于 0
 * - 支持补记（不允许未来日期）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocationDialog(
    goals: List<Goal>,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSave: (allocations: List<Pair<Long, Long>>, note: String, saveEpochDay: Long) -> Unit,
) {
    var totalText by remember { mutableStateOf("") }
    var allocTexts by remember { mutableStateOf(goals.associate { it.id to "" }) }
    var note by remember { mutableStateOf("") }
    var saveDay by remember { mutableStateOf(todayEpochDay) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val quickCents = listOf(1_000L, 2_000L, 5_000L, 10_000L) // 10 / 20 / 50 / 100 元

    fun redistribute(totalCents: Long) {
        val n = goals.size
        if (n == 0) return
        val base = totalCents / n
        val rem = totalCents % n
        allocTexts = goals.indices.associate { i ->
            goals[i].id to Money.toYuanInput(base + if (i < rem) 1L else 0L)
        }
    }

    val total = Money.parseToCents(totalText)
    val allocated = allocTexts.values.sumOf { Money.parseToCents(it) ?: 0L }
    val remaining = (total ?: 0L) - allocated

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录攒钱") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = totalText,
                    onValueChange = {
                        totalText = it; error = null
                        Money.parseToCents(it)?.let { cents -> redistribute(cents) }
                    },
                    label = { Text("本次攒钱总额（元）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickCents.forEach { cents ->
                        FilterChip(
                            selected = Money.toYuanInput(cents) == totalText.trim(),
                            onClick = {
                                totalText = Money.toYuanInput(cents)
                                redistribute(cents)
                                error = null
                            },
                            label = { Text(Money.format(cents)) },
                        )
                    }
                }

                Text("分配到各目标", style = MaterialTheme.typography.labelLarge)
                if (goals.isEmpty()) {
                    Text(
                        "还没有进行中的目标，请先到「目标」页创建",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                goals.forEach { g ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            g.name,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        OutlinedTextField(
                            value = allocTexts[g.id] ?: "",
                            onValueChange = { v ->
                                allocTexts = allocTexts + (g.id to v); error = null
                            },
                            label = { Text("元") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                if (total != null) {
                    Text(
                        when {
                            remaining > 0 -> "未分配 ${Money.format(remaining)}"
                            remaining < 0 -> "超出总额 ${Money.format(-remaining)}"
                            else -> "已全部分配"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remaining == 0L) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                    )
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
                    val t = Money.parseToCents(totalText)
                    val items = allocTexts.mapNotNull { (id, v) ->
                        Money.parseToCents(v)?.let { id to it }
                    }
                    val valid = items.all { it.second >= 0 }
                    val positiveItems = items.filter { it.second > 0 }
                    when {
                        goals.isEmpty() -> error = "还没有进行中的目标"
                        t == null -> error = "总额格式不正确，最多两位小数"
                        t <= 0 -> error = "总额必须大于零"
                        !valid -> error = "分配金额格式不正确"
                        saveDay > todayEpochDay -> error = "不能选择未来日期"
                        remaining != 0L -> error = "分配金额之和必须等于总额"
                        positiveItems.isEmpty() -> error = "至少分配一个目标"
                        else -> {
                            submitted = true
                            onSave(positiveItems, note.trim(), saveDay)
                        }
                    }
                },
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
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
