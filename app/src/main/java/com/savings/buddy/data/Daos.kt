package com.savings.buddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query(
        "SELECT * FROM goals ORDER BY " +
            "CASE status WHEN 'IN_PROGRESS' THEN 0 WHEN 'PAUSED' THEN 1 WHEN 'COMPLETED' THEN 2 ELSE 3 END, id ASC"
    )
    fun observeAll(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY id ASC")
    suspend fun getAllOnce(): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getByIdOnce(id: Long): Goal?

    @Insert
    suspend fun insert(goal: Goal): Long

    @Update
    suspend fun update(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()
}

@Dao
interface RecordDao {

    @Query("SELECT * FROM records WHERE goalId = :goalId ORDER BY saveEpochDay DESC, createdAtMillis DESC")
    fun observeForGoal(goalId: Long): Flow<List<SavingRecord>>

    @Query("SELECT * FROM records ORDER BY saveEpochDay DESC, createdAtMillis DESC")
    fun observeAll(): Flow<List<SavingRecord>>

    @Query(
        "SELECT r.*, g.name AS goalName FROM records r " +
            "INNER JOIN goals g ON r.goalId = g.id " +
            "ORDER BY r.saveEpochDay DESC, r.createdAtMillis DESC"
    )
    fun observeAllWithGoal(): Flow<List<RecordWithGoal>>

    @Query(
        "SELECT r.*, g.name AS goalName FROM records r " +
            "INNER JOIN goals g ON r.goalId = g.id WHERE r.id = :id"
    )
    fun observeByIdWithGoal(id: Long): Flow<RecordWithGoal?>

    @Query("SELECT * FROM records")
    suspend fun getAllOnce(): List<SavingRecord>

    @Insert
    suspend fun insert(record: SavingRecord): Long

    @Update
    suspend fun update(record: SavingRecord)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM records")
    suspend fun deleteAll()
}

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY unlockedAtMillis ASC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllOnce(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<AchievementEntity>)
}
