package com.savings.buddy.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** 主题模式 */
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

class SettingsStore(private val context: Context) {

    private val KEY_THEME = intPreferencesKey("theme")
    private val KEY_CURRENT_GOAL = longPreferencesKey("current_goal_id")
    private val KEY_REMINDER_ENABLED = intPreferencesKey("reminder_enabled")
    private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
    private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")

    val theme: Flow<Int> = context.dataStore.data.map { it[KEY_THEME] ?: THEME_SYSTEM }

    val currentGoalId: Flow<Long?> = context.dataStore.data.map { it[KEY_CURRENT_GOAL] }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { (it[KEY_REMINDER_ENABLED] ?: 0) == 1 }

    val reminderTime: Flow<Pair<Int, Int>> = context.dataStore.data.map {
        (it[KEY_REMINDER_HOUR] ?: 20) to (it[KEY_REMINDER_MINUTE] ?: 0)
    }

    suspend fun setTheme(mode: Int) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setCurrentGoalId(id: Long?) {
        context.dataStore.edit {
            if (id == null) it.remove(KEY_CURRENT_GOAL) else it[KEY_CURRENT_GOAL] = id
        }
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[KEY_REMINDER_ENABLED] = if (enabled) 1 else 0
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MINUTE] = minute
        }
    }
}
