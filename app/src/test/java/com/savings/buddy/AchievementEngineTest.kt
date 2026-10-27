package com.savings.buddy

import com.savings.buddy.business.AchievementDefs
import com.savings.buddy.business.AchievementEvaluator
import com.savings.buddy.business.StatsSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementEngineTest {

    private fun stats(
        saved: Long = 0,
        streak: Long = 0,
        goals: Long = 0,
        firstLaunch: Boolean = false,
    ) = StatsSnapshot(saved, streak, goals, firstLaunch)

    // ---------- 连续登录 ----------
    @Test
    fun `首次记录连击为1`() {
        assertEquals(1L, AchievementEvaluator.computeStreak(null, 0, todayEpochDay = 100))
    }

    @Test
    fun `当天重复打开不增加连击`() {
        assertEquals(5L, AchievementEvaluator.computeStreak(100, 5, todayEpochDay = 100))
        assertEquals(1L, AchievementEvaluator.computeStreak(100, 0, todayEpochDay = 100))
    }

    @Test
    fun `昨天打开过则连击加一`() {
        assertEquals(6L, AchievementEvaluator.computeStreak(99, 5, todayEpochDay = 100))
    }

    @Test
    fun `断签后重新计1`() {
        assertEquals(1L, AchievementEvaluator.computeStreak(97, 5, todayEpochDay = 100))
    }

    // ---------- 成就评估 ----------
    @Test
    fun `首次启动解锁首次登陆`() {
        val ids = AchievementEvaluator.evaluate(stats(firstLaunch = true), emptySet())
        assertTrue(ids.contains("first_login"))
        assertEquals(1, ids.size)
    }

    @Test
    fun `金额阈值恰好达成即解锁`() {
        val ids = AchievementEvaluator.evaluate(stats(saved = 1_000), emptySet())
        assertTrue(ids.contains("saved_10"))
        // 999 分差 1 分不解锁
        val below = AchievementEvaluator.evaluate(stats(saved = 999), emptySet())
        assertTrue(!below.contains("saved_10"))
    }

    @Test
    fun `一次达成多个成就是否全部返回`() {
        val ids = AchievementEvaluator.evaluate(stats(saved = 10_000, streak = 3, goals = 1), emptySet())
        assertTrue(ids.contains("saved_10"))
        assertTrue(ids.contains("saved_50"))
        assertTrue(ids.contains("saved_100"))
        assertTrue(ids.contains("streak_3"))
        assertTrue(ids.contains("goals_1"))
        assertEquals(5, ids.size)
    }

    @Test
    fun `已解锁的成就不会重复返回`() {
        val ids = AchievementEvaluator.evaluate(
            stats(saved = 10_000),
            unlockedIds = setOf("saved_10", "saved_50"),
        )
        assertTrue(!ids.contains("saved_10"))
        assertTrue(!ids.contains("saved_50"))
        assertTrue(ids.contains("saved_100"))
    }

    @Test
    fun `成就定义共12个且id唯一`() {
        assertEquals(12, AchievementDefs.ALL.size)
        assertEquals(12, AchievementDefs.ALL.map { it.id }.toSet().size)
    }
}
