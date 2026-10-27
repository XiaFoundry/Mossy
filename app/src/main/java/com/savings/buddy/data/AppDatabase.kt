package com.savings.buddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Goal::class, SavingRecord::class, AchievementEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun recordDao(): RecordDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        /** v2 -> v3：records 新增批次列，历史流水各自成批 */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE records ADD COLUMN batchId TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE records SET batchId = 'legacy-' || id WHERE batchId = ''")
            }
        }
    }
}

/** 手动依赖容器 */
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "saving_assistant.db",
    )
        // v1 -> v2 精简 Goal 字段，MVP 阶段采用破坏性迁移
        .fallbackToDestructiveMigration()
        // v2 -> v3 新增批次列，保留用户数据
        .addMigrations(AppDatabase.MIGRATION_2_3)
        .build()

    val settingsStore: SettingsStore = SettingsStore(context.applicationContext)
    val backupManager: BackupManager = BackupManager(database, settingsStore)
    val repository: SavingRepository = SavingRepository(database, settingsStore)
    val achievementManager: AchievementManager = AchievementManager(database, settingsStore)
}
