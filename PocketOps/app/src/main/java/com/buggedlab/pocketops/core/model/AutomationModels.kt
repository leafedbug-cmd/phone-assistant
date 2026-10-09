package com.buggedlab.pocketops.core.model

import java.time.DayOfWeek
import java.time.LocalTime
import kotlinx.serialization.Serializable

@Serializable
enum class TriggerType {
    TIME_SCHEDULE,
    CHARGING_STATE,
    APP_FOREGROUND,
    MANUAL
}

@Serializable
data class TimeScheduleTrigger(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: List<Int> // 1 = Monday ... 7 = Sunday
)

@Serializable
enum class ActionType {
    SET_DND_FILTER,
    SUPPRESS_NOTIFICATION_CATEGORIES,
    ACTIVATE_MODE,
    GENERATE_DIGEST,
    SHOW_ALERT
}

@Serializable
enum class DndFilterType {
    ALL_INTERRUPTIONS,      // Normal
    PRIORITY_ONLY,          // Priority DND
    TOTAL_SILENCE,          // None
    ALARMS_ONLY             // Alarms only
}

@Serializable
data class AutomationAction(
    val type: ActionType,
    val dndFilter: DndFilterType? = null,
    val suppressedCategories: List<NotificationCategory> = emptyList(),
    val modeName: String? = null,
    val alertMessage: String? = null
)

@Serializable
data class AutomationRule(
    val id: Long = 0,
    val title: String,
    val description: String,
    val isEnabled: Boolean = true,
    val priority: Int = 1, // Higher number = higher priority
    val triggerType: TriggerType,
    val scheduleTrigger: TimeScheduleTrigger? = null,
    val chargingTrigger: Boolean? = null, // true = when plugged in, false = when unplugged
    val appTriggerPackage: String? = null,
    val actions: List<AutomationAction>,
    val conflictGroup: String = "DEFAULT",
    val rollbackSupported: Boolean = true,
    val lastExecutedAt: Long? = null,
    val nextRunAt: Long? = null,
    val executionCount: Int = 0,
    val retryLimit: Int = 3
)

enum class ExecutionStatus {
    SUCCESS,
    FAILURE,
    SIMULATED,
    ROLLED_BACK,
    CANCELLED
}

data class ExecutionRecord(
    val id: Long = 0,
    val ruleId: Long,
    val ruleTitle: String,
    val timestamp: Long,
    val status: ExecutionStatus,
    val message: String,
    val detailsJson: String? = null,
    val rollbackSnapshot: String? = null
)

data class SimulationResult(
    val ruleId: Long,
    val ruleTitle: String,
    val canExecute: Boolean,
    val triggerSatisfied: Boolean,
    val missingPermissions: List<String>,
    val conflictsDetected: List<String>,
    val projectedActions: List<String>,
    val safetyAssessment: String
)
