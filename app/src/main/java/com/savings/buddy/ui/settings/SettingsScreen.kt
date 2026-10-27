package com.savings.buddy.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.data.THEME_DARK
import com.savings.buddy.data.THEME_LIGHT
import com.savings.buddy.data.THEME_SYSTEM
import com.savings.buddy.ui.common.simpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: com.savings.buddy.data.AppContainer,
    onGoAchievements: () -> Unit = {},
) {
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = simpleViewModelFactory { SettingsViewModel(container) })
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var showImportConfirm by remember { mutableStateOf<android.net.Uri?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { vm.exportTo(context, it) } }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { showImportConfirm = it } }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* 拒绝也不阻塞：核心功能不受影响 */ }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    androidx.compose.material3.Scaffold(
        snackbarHost = { SnackbarHost(snackbar) { data -> Snackbar(data) } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("设置", style = MaterialTheme.typography.titleLarge)

            // 外观
            SectionCard("外观") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.theme == THEME_SYSTEM,
                        onClick = { vm.setTheme(THEME_SYSTEM) },
                        label = { Text("跟随系统") },
                    )
                    FilterChip(
                        selected = state.theme == THEME_LIGHT,
                        onClick = { vm.setTheme(THEME_LIGHT) },
                        label = { Text("浅色") },
                    )
                    FilterChip(
                        selected = state.theme == THEME_DARK,
                        onClick = { vm.setTheme(THEME_DARK) },
                        label = { Text("深色") },
                    )
                }
            }

            // 提醒
            SectionCard("每日提醒") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("每天提醒我攒钱", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "提醒时间 ${"%02d".format(state.reminderHour)}:${"%02d".format(state.reminderMinute)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = { on ->
                            if (on && Build.VERSION.SDK_INT >= 33 &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            vm.setReminder(context, on, state.reminderHour, state.reminderMinute)
                        },
                    )
                }
                if (state.reminderEnabled) {
                    TextButton(onClick = { showTimePicker = true }) { Text("修改提醒时间") }
                }
            }

            // 成就
            SectionCard("成就") {
                Text(
                    "已解锁的攒钱里程碑",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onGoAchievements) { Text("查看成就") }
            }

            // 数据
            SectionCard("数据") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { exportLauncher.launch("saving_assistant_backup.json") }) {
                        Text("导出数据（JSON 备份）")
                    }
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }) {
                        Text("导入数据（覆盖现有数据）")
                    }
                    TextButton(onClick = { showClearConfirm = true }) {
                        Text("清除全部数据", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // 关于
            SectionCard("关于") {
                Text("Mossy v1.1.2", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "隐私说明：所有数据仅保存在本机，不上传服务器，不要求账号。" +
                        "本应用不接触资金，不提供支付或转账功能，实际攒钱由你自行完成。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showTimePicker) {
        val tpState = rememberTimePickerState(
            initialHour = state.reminderHour,
            initialMinute = state.reminderMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择提醒时间") },
            text = { TimePicker(state = tpState) },
            confirmButton = {
                TextButton(onClick = {
                    vm.setReminder(context, true, tpState.hour, tpState.minute)
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } },
        )
    }

    showImportConfirm?.let { uri ->
        AlertDialog(
            onDismissRequest = { showImportConfirm = null },
            title = { Text("覆盖现有数据？") },
            text = { Text("导入备份将删除当前全部目标与流水，并替换为备份内容。此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = { vm.importFrom(context, uri); showImportConfirm = null }) {
                    Text("覆盖导入", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = null }) { Text("取消") } },
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("清除全部数据？") },
            text = { Text("所有目标、计划与流水将被删除且无法恢复。建议先导出备份。") },
            confirmButton = {
                TextButton(onClick = { vm.clearData(); showClearConfirm = false }) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}
