package com.buggedlab.pocketops.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NotificationEntity::class,
        AutomationRuleEntity::class,
        ExecutionHistoryEntity::class,
        AuditLogEntity::class,
        AppConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PocketOpsDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun executionHistoryDao(): ExecutionHistoryDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: PocketOpsDatabase? = null

        fun getInstance(context: Context): PocketOpsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PocketOpsDatabase::class.java,
                    "pocketops_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
