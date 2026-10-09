package com.buggedlab.pocketops.core.model

data class AuditLogEntry(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val category: String, // AUTOMATION, AI_CONTROL, NOTIFICATION_RULE, PERMISSION
    val riskLevel: RiskLevel,
    val authorizedBy: String, // "USER_MANUAL", "SAVED_RULE", "AUTO_ROUTINE"
    val outcome: String, // SUCCESS, BLOCKED, FAILED, SIMULATED
    val details: String
)

data class AppRuleConfig(
    val packageName: String,
    val appName: String,
    val isProtected: Boolean = false,
    val isMutedInWorkMode: Boolean = false,
    val isDenylistedFromAiControl: Boolean = false
)
