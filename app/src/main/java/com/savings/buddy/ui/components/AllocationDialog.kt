package com.savings.buddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.savings.buddy.business.Allocation
import com.savings.buddy.business.Money
import com.savings.buddy.data.Goal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 记录攒钱对话框：
 * - 默认单目标模式，默认选中「已攒最多」的目标，可切换
 * - 可一键切换「分配到多个目标」：展开全部目标勾选，默认平均分配（余数给第一个），可手动调整
 * - 分配之和不能超过本次总额；快捷候选固定 10/20/50/100
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocationDialog(
    goals: List<Goal>,
    defaultGoalId: Long?,
    todayEpochDay: Long,
    onDismiss: () -> Unit,
    onSave: (allocations: List<Pair<Long, Long>>, note: String, saveEpochDay: Long) -> Unit,
) {
    var totalText by remember { mutableStateOf("") }
    var multiSelect by remember { mutableStateOf(false) }
    var singleGoalId by remember {
        mutableStateOf(defaultGoalId?.takeIf { goals.any { g -> g.id == it } } ?: goals.firstOrNull()?.id ?: 0L)
    }
    var selectedIds by remember {
        mutableStateOf(
            setOfNotNull(defaultGoalId?.takeIf { goals.any { g -> g.id == it } } ?: goals.firstOrNull()?.id)
        )
    }
    var singleAmountText by remember { mutableStateOf("") }
    var multiAmounts by remember { mutableStateOf(emptyMap<Long, String>()) }
    var listExpanded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var saveDay by remember { mutableStateOf(todayEpochDay) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val quickCents = listOf(1_000L, 2_000L, 5_000L, 10_000L) // 10 / 20 / 50 / 100 元

    fun applyTotal(cents: Long) {
        if (multiSelect) {
            multiAmounts = Allocation.distribute(cents, selectedIds.toList())
                .associate { (id, v) -> id to Money.toYuanInput(v) }
        } else {
            singleAmountText = Money.toYuanInput(cents)
        }
    }

    fun toggleGoal(id: Long) {
        val newSet = if (id in selectedIds) selectedIds - id else selectedIds + id
        selectedIds = newSet
        Money.parseToCents(totalText)?.let { cents ->
            multiAmounts = Allocation.distribute(cents, newSet.toList())
                .associate { (gid, v) -> gid to Money.toYuanInput(v) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录攒钱") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // 总额 + 快捷候选
                OutlinedTextField(
                    value = totalText,
                    onValueChange = {
                        totalText = it; error = null
                        Money.parseToCents(it)?.let { cents -> applyTotal(cents) }
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
                                applyTotal(cents)
                                error = null
                            },
                            label = { Text(Money.format(cents)) },
                        )
                    }
                }

                if (goals.isEmpty()) {
                    Text(
                        "还没有进行中的目标，请先到「目标」页创建",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    // 模式切换
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !multiSelect,
                            onClick = {
                                if (multiSelect) {
                                    multiSelect = false
                                    Money.parseToCents(totalText)?.let { cents ->
                                        singleAmountText = Money.toYuanInput(cents)
                                    }
                                    error = null
                                }
                            },
                            label = { Text("单目标") },
                        )
                        FilterChip(
                            selected = multiSelect,
                            onClick = {
                                if (!multiSelect) {
                                    multiSelect = true
                                    Money.parseToCents(totalText)?.let { cents ->
                                        multiAmounts = Allocation.distribute(cents, selectedIds.toList())
                                            .associate { (id, v) -> id to Money.toYuanInput(v) }
                                    }
                                    error = null
                                }
                            },
                            label = { Text("分配到多个目标") },
                        )
                    }

                    if (!multiSelect) {
                        // 单目标：选择目标 + 一个金额输入
                        Box {
                            var showGoalMenu by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { showGoalMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("目标：${goals.find { it.id == singleGoalId }?.name ?: "请选择"}")
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            androidx.compose.material3.DropdownMenu(
                                expanded = showGoalMenu,
                                onDismissRequest = { showGoalMenu = false },
                            ) {
                                goals.forEach { g ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(g.name) },
                                        onClick = { singleGoalId = g.id; showGoalMenu = false },
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = singleAmountText,
                            onValueChange = { singleAmountText = it; error = null },
                            label = { Text("金额（元）") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        // 多目标：可展开的勾选列表
                        OutlinedButton(
                            onClick = { listExpanded = !listExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                if (listExpanded) "收起目标列表" else "展开目标列表（已选 ${selectedIds.size} 个）"
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        if (listExpanded) {
                            goals.forEach { g ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = g.id in selectedIds,
                                        onCheckedChange = { toggleGoal(g.id) },
                                    )
                                    Text(
                                        g.name,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (g.id in selectedIds) {
                                        OutlinedTextField(
                                            value = multiAmounts[g.id] ?: "",
                                            onValueChange = { v ->
                                                multiAmounts = multiAmounts + (g.id to v); error = null
                                            },
                                            label = { Text("元") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                        if (!listExpanded && selectedIds.isNotEmpty()) {
                            val sum = multiAmounts.values.sumOf { Money.parseToCents(it) ?: 0L }
                            Text(
                                "已选 ${selectedIds.size} 个目标，共 ${Money.format(sum)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
                    val t = Money.parseToCents(totalText)
                    when {
                        goals.isEmpty() -> error = "还没有进行中的目标"
                        t == null -> error = "总额格式不正确，最多两位小数"
                        t <= 0 -> error = "总额必须大于零"
                        saveDay > todayEpochDay -> error = "不能选择未来日期"
                    }
                    if (error != null) return@Button

                    if (!multiSelect) {
                        val cents = Money.parseToCents(singleAmountText)
                        when {
                            singleGoalId <= 0 -> error = "请选择目标"
                            cents == null || cents <= 0 -> error = "金额必须大于零"
                            cents > t!! -> error = "金额不能超过本次总额"
                            else -> {
                                submitted = true
                                onSave(listOf(singleGoalId to cents), note.trim(), saveDay)
                            }
                        }
                    } else {
                        val items = multiAmounts.mapNotNull { (id, v) ->
                            Money.parseToCents(v)?.let { id to it }
                        }
                        val sum = items.sumOf { it.second }
                        val allValid = multiAmounts.values.all { Money.parseToCents(it) != null }
                        val positive = items.filter { it.second > 0 }
                        when {
                            selectedIds.isEmpty() -> error = "请至少勾选一个目标"
                            !allValid -> error = "分配金额格式不正确"
                            sum > t!! -> error = "分配之和不能超过本次总额"
                            positive.isEmpty() -> error = "至少分配一个大于零的金额"
                            else -> {
                                submitted = true
                                onSave(positive, note.trim(), saveDay)
                            }
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
