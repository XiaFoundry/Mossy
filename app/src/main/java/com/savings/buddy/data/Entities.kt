package com.savings.buddy.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 用户管理状态；「已完成」由累计金额推导，见业务层 */
enum class GoalStatus { IN_PROGRESS, PAUSED, COMPLETED, ARCHIVED }

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    /** 目标金额，人民币分，必须大于 0 */
    val targetCents: Long,
    /** 截止日期（epoch day），可选，仅作信息展示 */
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
    /** 批次 ID：一次分配保存产生的多条流水共享同一批次；单条记录也拥有独立批次 */
    val batchId: String = "",
)

/** 流水 + 所属目标名（联表查询结果） */
data class RecordWithGoal(
    @Embedded val record: SavingRecord,
    val goalName: String,
)

/** 已解锁成就 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val unlockedAtMillis: Long,
)
