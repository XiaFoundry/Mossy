package com.savings.buddy.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

/** 统一数据入口：集中维护时间戳与联动逻辑 */
class SavingRepository(
    private val db: AppDatabase,
    private val settings: SettingsStore,
) {

    fun observeGoals(): Flow<List<Goal>> = db.goalDao().observeAll()
    fun observeRecords(): Flow<List<SavingRecord>> = db.recordDao().observeAll()
    fun observeRecordsWithGoal(): Flow<List<RecordWithGoal>> = db.recordDao().observeAllWithGoal()

    suspend fun addRecord(goalId: Long, amountCents: Long, note: String, saveEpochDay: Long): Long {
        val now = System.currentTimeMillis()
        return db.recordDao().insert(
            SavingRecord(
                goalId = goalId,
                amountCents = amountCents,
                saveEpochDay = saveEpochDay,
                note = note.trim(),
                createdAtMillis = now,
                updatedAtMillis = now,
            )
        )
    }

    /** 一笔攒钱分配到多个目标：单事务写入多条流水（仅写入金额大于 0 的分配） */
    suspend fun addAllocatedRecords(
        allocations: List<Pair<Long, Long>>,
        note: String,
        saveEpochDay: Long,
    ) {
        val now = System.currentTimeMillis()
        db.withTransaction {
            allocations.forEach { (goalId, cents) ->
                if (cents > 0) {
                    db.recordDao().insert(
                        SavingRecord(
                            goalId = goalId,
                            amountCents = cents,
                            saveEpochDay = saveEpochDay,
                            note = note.trim(),
                            createdAtMillis = now,
                            updatedAtMillis = now,
                        )
                    )
                }
            }
        }
    }

    suspend fun updateRecord(record: SavingRecord) {
        db.recordDao().update(record.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun deleteRecord(id: Long) = db.recordDao().deleteById(id)

    /** 新建返回新 id；编辑返回原 id */
    suspend fun saveGoal(goal: Goal): Long {
        return if (goal.id == 0L) {
            db.goalDao().insert(goal)
        } else {
            db.goalDao().update(goal)
            goal.id
        }
    }

    suspend fun deleteGoal(id: Long) {
        db.goalDao().deleteById(id) // 流水按外键级联删除
    }

    suspend fun setGoalStatus(goal: Goal, status: GoalStatus) {
        db.goalDao().update(goal.copy(status = status))
    }
}
