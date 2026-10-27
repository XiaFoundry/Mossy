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

/** 主题模式（亮暗） */
const val THEME_SYSTEM = 0
const val THEME_LIGHT = 1
const val THEME_DARK = 2

/** 主题色板：默认跟随系统（Android 12+ 动态取色），其余为固定预设 */
const val PALETTE_SYSTEM = 0
const val PALETTE_TEAL = 1
const val PALETTE_PINK = 2
const val PALETTE_MINT = 3
const val PALETTE_WATER = 4
const val PALETTE_LAVENDER = 5
const val PALETTE_MONO = 6

/** 应用语言：默认跟随系统 UI 语言 */
const val LANG_SYSTEM = 0
const val LANG_EN = 1
const val LANG_ZH_CN = 2
const val LANG_ZH_TW = 3
const val LANG_JA = 4
const val LANG_KO = 5

class SettingsStore(private val context: Context) {

    private val KEY_THEME = intPreferencesKey("theme")
    private val KEY_PALETTE = intPreferencesKey("palette")
    private val KEY_LANGUAGE = intPreferencesKey("language")
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

    val palette: Flow<Int> = context.dataStore.data.map { it[KEY_PALETTE] ?: PALETTE_SYSTEM }

    val language: Flow<Int> = context.dataStore.data.map { it[KEY_LANGUAGE] ?: LANG_SYSTEM }

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

    suspend fun setPalette(palette: Int) {
        context.dataStore.edit { it[KEY_PALETTE] = palette }
    }

    suspend fun setLanguage(language: Int) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language }
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
