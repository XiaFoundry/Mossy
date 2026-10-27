package com.savings.buddy.data

import com.savings.buddy.business.AchievementDef
import com.savings.buddy.business.AchievementDefs
import com.savings.buddy.business.AchievementEvaluator
import com.savings.buddy.business.StatsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** 成就展示状态 */
data class AchievementState(
    val def: AchievementDef,
    val unlockedAtMillis: Long?,
) {
    val unlocked: Boolean get() = unlockedAtMillis != null
}

/**
 * 成就管理器：
 * - 应用启动：更新登录连击 / 首次启动标记并评估
 * - 流水变化：重算累计金额与目标完成数并评估
 * - 新解锁通过 [unlockedEvents] 推送给 UI 弹窗（成就只解锁一次，不回退）
 */
class AchievementManager(
    private val db: AppDatabase,
    private val settings: SettingsStore,
) {

    private val _unlockedEvents = MutableSharedFlow<List<AchievementDef>>(extraBufferCapacity = 16)
    val unlockedEvents: SharedFlow<List<AchievementDef>> = _unlockedEvents

    fun observe(): Flow<List<AchievementState>> =
        db.achievementDao().observeAll().map { list ->
            val unlockedMap = list.associate { it.id to it.unlockedAtMillis }
            AchievementDefs.ALL.map { AchievementState(it, unlockedMap[it.id]) }
        }

    /** 应用启动时调用 */
    suspend fun onAppOpen() {
        val today = LocalDate.now().toEpochDay()
        val last = settings.lastActiveEpochDay.first()
        val prevStreak = settings.streakDays.first()
        val streak = AchievementEvaluator.computeStreak(last, prevStreak, today)
        val firstLaunch = !settings.onboarded.first()
        settings.setLoginActivity(today, streak)
        if (firstLaunch) settings.markOnboarded()
        evaluate(isFirstLaunch = firstLaunch)
    }

    /** 流水新增/修改/删除后调用 */
    suspend fun onRecordsChanged() {
        evaluate(isFirstLaunch = false)
    }

    private suspend fun evaluate(isFirstLaunch: Boolean) {
        val goals = db.goalDao().getAllOnce()
        val records = db.recordDao().getAllOnce()

        val totalSaved = records.sumOf { it.amountCents }

        // 目标完成检测：达到目标金额即计入，计数只增不减（幂等）
        val completedNow = goals.filter { g ->
            records.filter { it.goalId == g.id }.sumOf { it.amountCents } >= g.targetCents
        }.map { it.id.toString() }.toSet()
        val known = settings.completedGoalIds.first()
        var completedCount = settings.completedGoalsCount.first()
        val newCompletions = completedNow - known
        if (newCompletions.isNotEmpty()) {
            completedCount += newCompletions.size
            settings.setCompletedGoals(completedNow, completedCount)
        }

        val stats = StatsSnapshot(
            totalSavedCents = totalSaved,
            streakDays = settings.streakDays.first(),
            completedGoalsCount = completedCount,
            isFirstLaunch = isFirstLaunch,
        )
        val unlocked = db.achievementDao().getAllOnce().map { it.id }.toSet()
        val newIds = AchievementEvaluator.evaluate(stats, unlocked)
        if (newIds.isNotEmpty()) {
            val now = System.currentTimeMillis()
            db.achievementDao().insertAll(newIds.map { AchievementEntity(it, now) })
            val defs = newIds.mapNotNull { AchievementDefs.byId(it) }
            _unlockedEvents.emit(defs)
        }
    }
}
