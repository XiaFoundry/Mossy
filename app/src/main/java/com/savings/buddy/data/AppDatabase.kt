package com.savings.buddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Goal::class, SavingRecord::class, AchievementEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun recordDao(): RecordDao
    abstract fun achievementDao(): AchievementDao
}

/** 手动依赖容器 */
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "saving_assistant.db",
    )
        // v1 -> v2：Goal 移除攒钱方式字段、新增成就表；MVP 阶段采用破坏性迁移
        .fallbackToDestructiveMigration()
        .build()

    val settingsStore: SettingsStore = SettingsStore(context.applicationContext)
    val backupManager: BackupManager = BackupManager(database, settingsStore)
    val repository: SavingRepository = SavingRepository(database, settingsStore)
    val achievementManager: AchievementManager = AchievementManager(database, settingsStore)
}
