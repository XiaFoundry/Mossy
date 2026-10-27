package com.savings.buddy.business

/**
 * 成就系统：定义 + 纯逻辑评估（无 Android 依赖，可单元测试）。
 * 成就一旦解锁永不回收；目标完成计数只增不减。
 * 标题与描述的文案由 UI 层按当前语言解析（见 ui/i18n）。
 */
data class StatsSnapshot(
    /** 所有流水累计金额（分） */
    val totalSavedCents: Long,
    /** 连续登录天数 */
    val streakDays: Long,
    /** 累计完成目标数 */
    val completedGoalsCount: Long,
    /** 是否首次启动 */
    val isFirstLaunch: Boolean,
)

data class AchievementDef(
    val id: String,
    val check: (StatsSnapshot) -> Boolean,
)

object AchievementDefs {

    val ALL: List<AchievementDef> = listOf(
        AchievementDef("first_login") { it.isFirstLaunch },
        AchievementDef("saved_10") { it.totalSavedCents >= 1_000 },
        AchievementDef("saved_50") { it.totalSavedCents >= 5_000 },
        AchievementDef("saved_100") { it.totalSavedCents >= 10_000 },
        AchievementDef("saved_1000") { it.totalSavedCents >= 100_000 },
        AchievementDef("saved_5000") { it.totalSavedCents >= 500_000 },
        AchievementDef("streak_3") { it.streakDays >= 3 },
        AchievementDef("streak_7") { it.streakDays >= 7 },
        AchievementDef("streak_30") { it.streakDays >= 30 },
        AchievementDef("goals_1") { it.completedGoalsCount >= 1 },
        AchievementDef("goals_3") { it.completedGoalsCount >= 3 },
        AchievementDef("goals_10") { it.completedGoalsCount >= 10 },
    )

    fun byId(id: String): AchievementDef? = ALL.find { it.id == id }
}

object AchievementEvaluator {

    /**
     * 连续登录天数：
     * - 首次记录 -> 1
     * - 今天已记录过 -> 维持不变
     * - 昨天记录过 -> +1
     * - 断签 -> 重新计 1
     */
    fun computeStreak(lastActiveEpochDay: Long?, currentStreak: Long, todayEpochDay: Long): Long {
        return when (lastActiveEpochDay) {
            null -> 1L
            todayEpochDay -> if (currentStreak <= 0) 1L else currentStreak
            todayEpochDay - 1 -> currentStreak + 1
            else -> 1L
        }
    }

    /** 返回本次新达成的成就 id（尚未解锁且条件满足） */
    fun evaluate(stats: StatsSnapshot, unlockedIds: Set<String>): List<String> {
        return AchievementDefs.ALL
            .filter { it.id !in unlockedIds && it.check(stats) }
            .map { it.id }
    }
}
