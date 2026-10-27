package com.savings.buddy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Goal::class, SavingRecord::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun recordDao(): RecordDao
}

/** 手动依赖容器 */
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "saving_assistant.db",
    ).build()

    val settingsStore: SettingsStore = SettingsStore(context.applicationContext)
    val backupManager: BackupManager = BackupManager(database, settingsStore)
    val repository: SavingRepository = SavingRepository(database, settingsStore)
}
