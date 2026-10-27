package com.savings.buddy.business

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** 统一进度计算结果，驱动圆环填充、颜色、百分比与剩余金额 */
data class ProgressResult(
    val savedCents: Long,
    val targetCents: Long,
    /** 实际累计允许超过目标；本比例已限制在 0..1 */
    val progress: Float,
    /** 完成百分比，四舍五入整数，0..100 */
    val percent: Int,
    val remainingCents: Long,
    /** 累计金额是否已达目标（超额也算完成） */
    val isCompleted: Boolean,
    /** 超出目标的部分（分），未超额为 0 */
    val exceededCents: Long,
)

object ProgressCalculator {

    /**
     * progress = savedAmount / targetAmount，clamp 到 [0, 1]。
     * 百分比按四舍五入取整。
     */
    fun calculate(savedCents: Long, targetCents: Long): ProgressResult {
        if (targetCents <= 0) {
            return ProgressResult(savedCents, targetCents, 0f, 0, 0, savedCents > 0, 0)
        }
        val raw = savedCents.toDouble() / targetCents.toDouble()
        val clamped = raw.coerceIn(0.0, 1.0)
        val percent = (raw * 100.0).roundToLong().toInt().coerceIn(0, 100)
        val remaining = max(0L, targetCents - savedCents)
        val exceeded = max(0L, savedCents - targetCents)
        return ProgressResult(
            savedCents = savedCents,
            targetCents = targetCents,
            progress = clamped.toFloat(),
            percent = percent,
            remainingCents = remaining,
            isCompleted = savedCents >= targetCents,
            exceededCents = exceeded,
        )
    }

    /** 目标剩余天数：正数=还剩 N 天，0=今天截止，负数=已逾期 N 天 */
    fun remainingDays(deadlineEpochDay: Long?, todayEpochDay: Long): Long? {
        if (deadlineEpochDay == null) return null
        return deadlineEpochDay - todayEpochDay
    }

    /** 防御式收敛：确保进度不会为负或超过 1 */
    fun safeProgress(value: Float): Float = min(1f, max(0f, value))
}
