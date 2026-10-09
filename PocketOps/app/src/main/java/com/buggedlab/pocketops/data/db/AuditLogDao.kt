package com.buggedlab.pocketops.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insert(entry: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE actionName LIKE '%' || :query || '%' OR details LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun search(query: String, limit: Int = 50): List<AuditLogEntity>

    @Query("DELETE FROM audit_logs")
    suspend fun clearAll(): Int
}
