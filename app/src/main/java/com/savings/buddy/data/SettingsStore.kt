package com.savings.buddy.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    private val KEY_REMINDER_ENABLED = intPreferencesKey("reminder_enabled")
    private val KEY_REMINDER_HOUR = intPreferencesKey("reminder_hour")
    private val KEY_REMINDER_MINUTE = intPreferencesKey("reminder_minute")

    // 成就系统：登录连击与目标完成计数
    private val KEY_LAST_ACTIVE_DAY = longPreferencesKey("last_active_epoch_day")
    private val KEY_STREAK_DAYS = longPreferencesKey("streak_days")
    private val KEY_ONBOARDED = booleanPreferencesKey("onboarded")
    private val KEY_COMPLETED_COUNT = longPreferencesKey("completed_goals_count")
    private val KEY_COMPLETED_IDS = stringPreferencesKey("completed_goal_ids")

    val theme: Flow<Int> = context.dataStore.data.map { it[KEY_THEME] ?: THEME_SYSTEM }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { (it[KEY_REMINDER_ENABLED] ?: 0) == 1 }

    val reminderTime: Flow<Pair<Int, Int>> = context.dataStore.data.map {
        (it[KEY_REMINDER_HOUR] ?: 20) to (it[KEY_REMINDER_MINUTE] ?: 0)
    }

    val lastActiveEpochDay: Flow<Long?> = context.dataStore.data.map {
        it[KEY_LAST_ACTIVE_DAY]
    }

    val streakDays: Flow<Long> = context.dataStore.data.map { it[KEY_STREAK_DAYS] ?: 0L }

    val onboarded: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDED] ?: false }

    val completedGoalsCount: Flow<Long> = context.dataStore.data.map { it[KEY_COMPLETED_COUNT] ?: 0L }

    val completedGoalIds: Flow<Set<String>> = context.dataStore.data.map {
        it[KEY_COMPLETED_IDS]?.split(",")?.filter { id -> id.isNotBlank() }?.toSet() ?: emptySet()
    }

    suspend fun setTheme(mode: Int) {
        context.dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit {
            it[KEY_REMINDER_ENABLED] = if (enabled) 1 else 0
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun setLoginActivity(todayEpochDay: Long, streak: Long) {
        context.dataStore.edit {
            it[KEY_LAST_ACTIVE_DAY] = todayEpochDay
            it[KEY_STREAK_DAYS] = streak
        }
    }

    suspend fun markOnboarded() {
        context.dataStore.edit { it[KEY_ONBOARDED] = true }
    }

    suspend fun setCompletedGoals(ids: Set<String>, count: Long) {
        context.dataStore.edit {
            it[KEY_COMPLETED_IDS] = ids.joinToString(",")
            it[KEY_COMPLETED_COUNT] = count
        }
    }
}
