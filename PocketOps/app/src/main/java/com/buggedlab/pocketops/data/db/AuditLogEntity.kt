package com.buggedlab.pocketops.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buggedlab.pocketops.core.model.RiskLevel

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val category: String,
    val riskLevel: RiskLevel,
    val authorizedBy: String,
    val outcome: String,
    val details: String
)
