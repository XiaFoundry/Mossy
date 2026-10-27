package com.savings.buddy.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.business.Money
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.Goal
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.i18n.LocalStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GoalEditViewModel(private val container: AppContainer) : ViewModel() {

    val loaded = MutableStateFlow<Goal?>(null)

    fun load(goalId: Long?) {
        if (goalId == null) return
        viewModelScope.launch { loaded.value = container.database.goalDao().getByIdOnce(goalId) }
    }

    fun save(
        goalId: Long?,
        name: String,
        targetText: String,
        deadlineEpochDay: Long?,
        note: String,
        colorIndex: Int,
        t: com.savings.buddy.ui.i18n.Strings,
        onError: (String) -> Unit,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) { onError(t.geErrNameEmpty); return@launch }
            val target = Money.parseToCents(targetText)
            if (target == null) { onError(t.geErrTargetFormat); return@launch }
            if (target <= 0) { onError(t.geErrTargetPositive); return@launch }
            // 新建时不允许已经过期的截止日期；编辑旧目标不强制
            if (goalId == null && deadlineEpochDay != null && deadlineEpochDay < LocalDate.now().toEpochDay()) {
                onError(t.geErrDeadlinePast); return@launch
            }

            if (goalId != null && loaded.value == null) {
                onError(t.geErrNotLoaded); return@launch
            }
            val existing = goalId?.let { loaded.value } ?: Goal(
                name = trimmedName, targetCents = target, createdAtEpochDay = LocalDate.now().toEpochDay(),
            )
            val updated = existing.copy(
                name = trimmedName,
                targetCents = target,
                deadlineEpochDay = deadlineEpochDay,
                note = note.trim(),
                colorIndex = colorIndex,
            )
            container.repository.saveGoal(updated)
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    container: AppContainer,
    goalId: Long?,
    onBack: () -> Unit,
) {
    val t = LocalStrings.current
    val vm: GoalEditViewModel = viewModel(factory = simpleViewModelFactory { GoalEditViewModel(container) })
    LaunchedEffect(goalId) { vm.load(goalId) }

    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var colorIndex by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(vm.loaded.value) {
        val g = vm.loaded.value ?: return@LaunchedEffect
        if (!initialized) {
            name = g.name
            targetText = Money.toYuanInput(g.targetCents)
            deadline = g.deadlineEpochDay
            note = g.note
            colorIndex = g.colorIndex
            initialized = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (goalId == null) t.geCreateTitle else t.geEditTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t.geBack)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text(t.geName) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it; error = null },
                label = { Text(t.geTargetAmount) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text(t.geDeadlineOptional, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(deadline?.let { LocalDate.ofEpochDay(it).toString() } ?: t.geDeadlineNone)
                }
                if (deadline != null) {
                    TextButton(onClick = { deadline = null }) { Text(t.geClearDeadline) }
                }
            }

            Column {
                Text(t.geThemeColor, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { i ->
                        val selected = colorIndex == i
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(goalColor(i))
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                    shape = CircleShape,
                                )
                                .clickable { colorIndex = i },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(t.geNoteLabel) },
                modifier = Modifier.fillMaxWidth(),
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    vm.save(goalId, name, targetText, deadline, note, colorIndex, t,
                        onError = { error = it }, onSuccess = onBack)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text(if (goalId == null) t.geBtnCreate else t.geBtnSave) }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (deadline ?: LocalDate.now().toEpochDay()) * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        deadline = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                }) { Text(t.commonConfirm) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(t.commonCancel) } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
