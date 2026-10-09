package com.buggedlab.pocketops.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionHistoryDao {
    @Insert
    suspend fun insert(record: ExecutionHistoryEntity): Long

    @Query("SELECT * FROM execution_history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<ExecutionHistoryEntity>>

    @Query("SELECT * FROM execution_history WHERE ruleId = :ruleId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByRuleId(ruleId: Long, limit: Int = 20): List<ExecutionHistoryEntity>

    @Query("DELETE FROM execution_history WHERE timestamp < :beforeMillis")
    suspend fun deleteOlderThan(beforeMillis: Long): Int

    @Query("DELETE FROM execution_history")
    suspend fun clearAll(): Int
}
