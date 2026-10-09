package com.buggedlab.pocketops.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class RiskLevel(val label: String) {
    LOW("Low Risk"),
    MEDIUM("Medium Risk"),
    HIGH("High Risk")
}

@Serializable
enum class AiIntent {
    ORGANIZE_NOTIFICATIONS,
    FIND_NOISY_APPS,
    CREATE_ROUTINE,
    OPEN_SETTINGS,
    GENERATE_DIGEST,
    SAMSUNG_ROUTINE_HELP,
    INSPECT_SCREEN,
    EXECUTE_STEP,
    GENERAL_QUERY
}

@Serializable
enum class PlanStatus {
    PENDING_APPROVAL,
    APPROVED,
    EXECUTING,
    COMPLETED,
    REJECTED,
    CANCELLED,
    FAILED
}

@Serializable
data class ProposedAction(
    val id: String,
    val title: String,
    val description: String,
    val riskLevel: RiskLevel,
    val targetCapability: String,
    val requiredPermission: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val isApproved: Boolean = false,
    val isExecuted: Boolean = false,
    val executionResult: String? = null
)

@Serializable
data class AiPlan(
    val id: String,
    val userPrompt: String,
    val intent: AiIntent,
    val explanation: String,
    val actions: List<ProposedAction>,
    val requiresUserApproval: Boolean = true,
    val status: PlanStatus = PlanStatus.PENDING_APPROVAL,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedPlan: AiPlan? = null
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val rationale: String,
    val proposedRule: AutomationRule,
    val isDismissed: Boolean = false
)
