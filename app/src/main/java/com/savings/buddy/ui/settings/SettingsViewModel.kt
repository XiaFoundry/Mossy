package com.savings.buddy.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.savings.buddy.data.AppContainer
import com.savings.buddy.data.LANG_SYSTEM
import com.savings.buddy.data.PALETTE_SYSTEM
import com.savings.buddy.data.THEME_DARK
import com.savings.buddy.data.THEME_LIGHT
import com.savings.buddy.data.THEME_SYSTEM
import com.savings.buddy.reminder.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val theme: Int = THEME_SYSTEM,
    val palette: Int = PALETTE_SYSTEM,
    val language: Int = LANG_SYSTEM,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)

    /** 一次性操作提示（导出/导入/清除结果） */
    val message: StateFlow<String?> = _message

    val state: StateFlow<SettingsUiState> = combine(
        container.settingsStore.theme,
        container.settingsStore.palette,
        container.settingsStore.language,
        container.settingsStore.reminderEnabled,
        container.settingsStore.reminderTime,
    ) { theme, palette, language, enabled, time ->
        SettingsUiState(theme, palette, language, enabled, time.first, time.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun consumeMessage() { _message.value = null }

    fun setTheme(mode: Int) = viewModelScope.launch { container.settingsStore.setTheme(mode) }

    fun setPalette(palette: Int) = viewModelScope.launch { container.settingsStore.setPalette(palette) }

    fun setLanguage(language: Int) = viewModelScope.launch { container.settingsStore.setLanguage(language) }

    fun setReminder(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        viewModelScope.launch {
            container.settingsStore.setReminder(enabled, hour, minute)
            if (enabled) ReminderScheduler.schedule(context, hour, minute)
            else ReminderScheduler.cancel(context)
        }
    }

    fun exportTo(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) { container.backupManager.exportJson() }
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw IllegalStateException("无法写入所选位置")
                }
                _message.value = "导出成功"
            } catch (e: Exception) {
                _message.value = "导出失败：${e.message}"
            }
        }
    }

    fun importFrom(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: throw IllegalStateException("无法读取所选文件")
                }
                withContext(Dispatchers.IO) { container.backupManager.importJson(json) }
                _message.value = "导入成功，已覆盖现有数据"
            } catch (e: IllegalArgumentException) {
                _message.value = e.message ?: "备份文件校验失败"
            } catch (e: Exception) {
                _message.value = "导入失败：${e.message}"
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { container.backupManager.clearAllData() }
            _message.value = "已清除全部数据"
        }
    }
}
