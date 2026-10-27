package com.savings.buddy.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 攒钱方式：固定金额 / 目标倒推 */
enum class PlanType { FIXED, REVERSE }

/** 用户管理状态；「已完成」由累计金额推导，见业务层 */
enum class GoalStatus { IN_PROGRESS, PAUSED, COMPLETED, ARCHIVED }

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** 目标金额，人民币分，必须大于 0 */
    val targetCents: Long,
    val planType: PlanType = PlanType.FIXED,
    /** 固定金额模式：每日建议金额（分） */
    val dailyFixedCents: Long = 0L,
    /** 目标倒推模式：截止日期（epoch day），可为空 */
    val deadlineEpochDay: Long? = null,
    val note: String = "",
    /** 主题颜色索引（0-5） */
    val colorIndex: Int = 0,
    val status: GoalStatus = GoalStatus.IN_PROGRESS,
    val createdAtEpochDay: Long,
)

@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("goalId")],
)
data class SavingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val goalId: Long,
    /** 本次攒钱金额，人民币分，必须大于 0 */
    val amountCents: Long,
    /** 实际攒钱日期（epoch day），支持补记 */
    val saveEpochDay: Long,
    val note: String = "",
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

/** 流水 + 所属目标名（联表查询结果） */
data class RecordWithGoal(
    @Embedded val record: SavingRecord,
    val goalName: String,
)
