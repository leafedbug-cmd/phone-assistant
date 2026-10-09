package com.buggedlab.pocketops.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.buggedlab.pocketops.core.model.TriggerType
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: AutomationRuleEntity): Long

    @Update
    suspend fun update(rule: AutomationRuleEntity)

    @Delete
    suspend fun delete(rule: AutomationRuleEntity)

    @Query("SELECT * FROM automation_rules WHERE id = :id")
    suspend fun getById(id: Long): AutomationRuleEntity?

    @Query("SELECT * FROM automation_rules ORDER BY priority DESC, id ASC")
    fun observeAll(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules ORDER BY priority DESC, id ASC")
    suspend fun getAll(): List<AutomationRuleEntity>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 ORDER BY priority DESC")
    suspend fun getEnabledRules(): List<AutomationRuleEntity>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1 AND triggerType = :type ORDER BY priority DESC")
    suspend fun getEnabledRulesByTriggerType(type: TriggerType): List<AutomationRuleEntity>

    @Query("UPDATE automation_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean): Int

    @Query("UPDATE automation_rules SET lastExecutedAt = :time, executionCount = executionCount + 1 WHERE id = :id")
    suspend fun recordExecution(id: Long, time: Long): Int

    @Query("SELECT COUNT(*) FROM automation_rules")
    suspend fun getCount(): Int
}
