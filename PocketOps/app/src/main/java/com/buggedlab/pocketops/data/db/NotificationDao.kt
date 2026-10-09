package com.buggedlab.pocketops.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class AppCountQueryResult(
    val packageName: String,
    val appName: String,
    val count: Int
)

data class CategoryCountQueryResult(
    val category: String,
    val count: Int
)

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY postTimeMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE postTimeMillis >= :sinceMillis ORDER BY postTimeMillis DESC")
    suspend fun getNotificationsSince(sinceMillis: Long): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM notifications WHERE postTimeMillis >= :sinceMillis")
    suspend fun getCountSince(sinceMillis: Long): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE postTimeMillis >= :sinceMillis AND isSuppressedByRoutine = 1")
    suspend fun getSuppressedCountSince(sinceMillis: Long): Int

    @Query("SELECT packageName, appName, COUNT(*) as count FROM notifications WHERE postTimeMillis >= :sinceMillis GROUP BY packageName ORDER BY count DESC LIMIT :limit")
    suspend fun getTopAppsSince(sinceMillis: Long, limit: Int = 10): List<AppCountQueryResult>

    @Query("SELECT category, COUNT(*) as count FROM notifications WHERE postTimeMillis >= :sinceMillis GROUP BY category")
    suspend fun getCategoryCountsSince(sinceMillis: Long): List<CategoryCountQueryResult>

    @Query("DELETE FROM notifications WHERE postTimeMillis < :beforeMillis")
    suspend fun deleteOlderThan(beforeMillis: Long): Int

    @Query("DELETE FROM notifications")
    suspend fun clearAll(): Int
}
