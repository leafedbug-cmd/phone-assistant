package com.buggedlab.pocketops.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buggedlab.pocketops.core.model.ExecutionStatus

@Entity(tableName = "execution_history")
data class ExecutionHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleId: Long,
    val ruleTitle: String,
    val timestamp: Long,
    val status: ExecutionStatus,
    val message: String,
    val detailsJson: String? = null,
    val rollbackSnapshot: String? = null
)
