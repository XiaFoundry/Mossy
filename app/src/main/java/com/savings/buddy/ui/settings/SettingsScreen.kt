package com.savings.buddy.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.savings.buddy.data.LANG_EN
import com.savings.buddy.data.LANG_JA
import com.savings.buddy.data.LANG_KO
import com.savings.buddy.data.LANG_SYSTEM
import com.savings.buddy.data.LANG_ZH_CN
import com.savings.buddy.data.LANG_ZH_TW
import com.savings.buddy.data.PALETTE_SYSTEM
import com.savings.buddy.data.THEME_DARK
import com.savings.buddy.data.THEME_LIGHT
import com.savings.buddy.data.THEME_SYSTEM
import com.savings.buddy.ui.common.simpleViewModelFactory
import com.savings.buddy.ui.i18n.LocalStrings
import com.savings.buddy.ui.i18n.Strings
import com.savings.buddy.ui.theme.ThemePalettes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: com.savings.buddy.data.AppContainer,
    onGoAchievements: () -> Unit = {},
) {
    val t = LocalStrings.current
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = simpleViewModelFactory { SettingsViewModel(container) })
    val state by vm.state.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var showImportConfirm by remember { mutableStateOf<android.net.Uri?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var paletteExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }

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
            Text(t.setTitle, style = MaterialTheme.typography.titleLarge)

            // 外观
            SectionCard(t.setAppearance) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.theme == THEME_SYSTEM,
                        onClick = { vm.setTheme(THEME_SYSTEM) },
                        label = { Text(t.setThemeSystem) },
                    )
                    FilterChip(
                        selected = state.theme == THEME_LIGHT,
                        onClick = { vm.setTheme(THEME_LIGHT) },
                        label = { Text(t.setThemeLight) },
                    )
                    FilterChip(
                        selected = state.theme == THEME_DARK,
                        onClick = { vm.setTheme(THEME_DARK) },
                        label = { Text(t.setThemeDark) },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 主题色详细设置：点击展开/收起
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { paletteExpanded = !paletteExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(t.setPalette, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        currentPalettePreview(state.palette).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                    Spacer(Modifier.size(6.dp))
                    Text(
                        paletteLabel(state.palette, t),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = if (paletteExpanded) t.cdCollapse else t.cdExpand,
                    )
                }

                if (paletteExpanded) {
                    Spacer(Modifier.height(4.dp))
                    PaletteOptionRow(
                        label = t.setPaletteSystem,
                        sub = if (Build.VERSION.SDK_INT >= 31) t.setPaletteSystemSubModern
                        else t.setPaletteSystemSubLegacy,
                        selected = state.palette == PALETTE_SYSTEM,
                        preview = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        onClick = { vm.setPalette(PALETTE_SYSTEM) },
                    )
                    ThemePalettes.ALL.forEach { spec ->
                        PaletteOptionRow(
                            label = spec.label,
                            sub = null,
                            selected = state.palette == spec.key,
                            preview = spec.preview,
                            onClick = { vm.setPalette(spec.key) },
                        )
                    }
                }
            }

            // 语言（无论当前语言是什么，旁边始终带 (Language) 标注）
            SectionCard(t.setLanguage + " (Language)") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { languageExpanded = !languageExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(t.setLanguage, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            t.setLanguageSub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        languageLabel(state.language, t),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = if (languageExpanded) t.cdCollapse else t.cdExpand,
                    )
                }

                if (languageExpanded) {
                    Spacer(Modifier.height(4.dp))
                    // 系统语言选项（本地化文案）
                    LanguageOptionRow(
                        label = t.langSystem,
                        selected = state.language == LANG_SYSTEM,
                        onClick = { vm.setLanguage(LANG_SYSTEM) },
                    )
                    // 各语言选项以自己的语言显示
                    LanguageOptionRow("English", state.language == LANG_EN, onClick = { vm.setLanguage(LANG_EN) })
                    LanguageOptionRow("简体中文", state.language == LANG_ZH_CN, onClick = { vm.setLanguage(LANG_ZH_CN) })
                    LanguageOptionRow("繁體中文", state.language == LANG_ZH_TW, onClick = { vm.setLanguage(LANG_ZH_TW) })
                    LanguageOptionRow("日本語", state.language == LANG_JA, onClick = { vm.setLanguage(LANG_JA) })
                    LanguageOptionRow("한국어", state.language == LANG_KO, onClick = { vm.setLanguage(LANG_KO) })
                }
            }

            // 提醒
            SectionCard(t.setReminder) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.setReminderDaily, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            t.setReminderTime.format("%02d:%02d".format(state.reminderHour, state.reminderMinute)),
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
                    TextButton(onClick = { showTimePicker = true }) { Text(t.setReminderChange) }
                }
            }

            // 成就
            SectionCard(t.setAchievements) {
                Text(
                    t.setAchDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onGoAchievements) { Text(t.setAchView) }
            }

            // 数据
            SectionCard(t.setData) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { exportLauncher.launch("mossy_backup.json") }) {
                        Text(t.setExport)
                    }
                    TextButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }) {
                        Text(t.setImport)
                    }
                    TextButton(onClick = { showClearConfirm = true }) {
                        Text(t.setClear, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // 关于
            SectionCard(t.setAbout) {
                Text(t.setVersion.format("1.2.0"), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    t.setPrivacy,
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
            title = { Text(t.setReminderChange) },
            text = { TimePicker(state = tpState) },
            confirmButton = {
                TextButton(onClick = {
                    vm.setReminder(context, true, tpState.hour, tpState.minute)
                    showTimePicker = false
                }) { Text(t.commonConfirm) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(t.commonCancel) } },
        )
    }

    showImportConfirm?.let { uri ->
        AlertDialog(
            onDismissRequest = { showImportConfirm = null },
            title = { Text(t.importConfirmTitle) },
            text = { Text(t.importConfirmMsg) },
            confirmButton = {
                TextButton(onClick = { vm.importFrom(context, uri); showImportConfirm = null }) {
                    Text(t.importConfirmYes, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showImportConfirm = null }) { Text(t.commonCancel) } },
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(t.clearConfirmTitle) },
            text = { Text(t.clearConfirmMsg) },
            confirmButton = {
                TextButton(onClick = { vm.clearData(); showClearConfirm = false }) {
                    Text(t.clearConfirmYes, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text(t.commonCancel) } },
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

/** 当前主题色的预览色（跟随系统时取当前生效色） */
@Composable
private fun currentPalettePreview(palette: Int): List<Color> {
    return if (palette == PALETTE_SYSTEM) {
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
        )
    } else {
        ThemePalettes.byKey(palette).preview
    }
}

private fun paletteLabel(palette: Int, t: Strings): String =
    if (palette == PALETTE_SYSTEM) {
        t.setPaletteSystem
            .replace("（默认）", "")
            .replace("(default)", "")
    } else ThemePalettes.byKey(palette).label

private fun languageLabel(language: Int, t: Strings): String = when (language) {
    LANG_EN -> "English"
    LANG_ZH_CN -> "简体中文"
    LANG_ZH_TW -> "繁體中文"
    LANG_JA -> "日本語"
    LANG_KO -> "한국어"
    else -> t.langSystem
}

/** 语言选项行 */
@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** 主题色选项行：色块预览 + 名称 + 选中态 */
@Composable
private fun PaletteOptionRow(
    label: String,
    sub: String?,
    selected: Boolean,
    preview: List<Color>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            preview.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            sub?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (selected) {
            Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
        }
    }
}
