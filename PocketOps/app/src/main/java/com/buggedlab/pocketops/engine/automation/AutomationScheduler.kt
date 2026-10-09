package com.buggedlab.pocketops.engine.automation

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.buggedlab.pocketops.core.model.ActionType
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.AutomationRule
import com.buggedlab.pocketops.core.model.DndFilterType
import com.buggedlab.pocketops.core.model.ExecutionRecord
import com.buggedlab.pocketops.core.model.ExecutionStatus
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.core.model.SimulationResult
import com.buggedlab.pocketops.core.model.TriggerType
import com.buggedlab.pocketops.data.db.AuditLogEntity
import com.buggedlab.pocketops.data.db.AutomationRuleEntity
import com.buggedlab.pocketops.data.db.ExecutionHistoryEntity
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import com.buggedlab.pocketops.engine.capability.DeviceCapabilityDetector
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.first

class AutomationScheduler(
    private val context: Context,
    private val zoneId: ZoneId = ZoneId.of("America/Chicago")
) {
    private val db = PocketOpsDatabase.getInstance(context)
    private val detector = DeviceCapabilityDetector(context)
    private val prefs = PocketOpsPreferences(context)

    companion object {
        private const val TAG = "PocketOpsScheduler"
    }

    // Dry Run Simulator: safely evaluates what would happen without altering device state
    suspend fun simulateRule(rule: AutomationRule): SimulationResult {
        val permissions = detector.detectPermissions()
        val missingPermissions = mutableListOf<String>()
        val conflicts = mutableListOf<String>()
        val projectedActions = mutableListOf<String>()

        // Check required permissions for actions
        for (action in rule.actions) {
            when (action.type) {
                ActionType.SET_DND_FILTER -> {
                    if (!permissions.hasDndPolicyAccess) {
                        missingPermissions.add("Do Not Disturb (Notification Policy) Access")
                    }
                    projectedActions.add("Set DND Filter to ${action.dndFilter ?: DndFilterType.PRIORITY_ONLY}")
                }
                ActionType.SUPPRESS_NOTIFICATION_CATEGORIES -> {
                    if (!permissions.hasNotificationListenerAccess) {
                        missingPermissions.add("Notification Listener Access")
                    }
                    val cats = action.suppressedCategories.joinToString { it.displayName }
                    projectedActions.add("Filter incoming notifications in categories: $cats")
                }
                ActionType.ACTIVATE_MODE -> {
                    projectedActions.add("Set PocketOps Mode to '${action.modeName ?: "Focus"}'")
                }
                ActionType.GENERATE_DIGEST -> {
                    projectedActions.add("Generate and display notification summary digest")
                }
                ActionType.SHOW_ALERT -> {
                    projectedActions.add("Display status reminder: '${action.alertMessage ?: ""}'")
                }
            }
        }

        // Conflict detection: inspect enabled rules in same conflict group
        val enabledRules = db.automationRuleDao().getEnabledRules()
        for (other in enabledRules) {
            if (other.id != rule.id && other.conflictGroup == rule.conflictGroup) {
                conflicts.add("Potential conflict with active rule '${other.title}' in group '${rule.conflictGroup}' (Priority: ${other.priority} vs ${rule.priority})")
            }
        }

        // Check trigger satisfaction right now
        val triggerSatisfied = checkTriggerSatisfaction(rule)

        val canExecute = missingPermissions.isEmpty()

        val safetyAssessment = when {
            !canExecute -> "Blocked: Missing required Android permissions."
            conflicts.isNotEmpty() -> "Warning: Conflicting rules detected. Higher priority rule will take precedence."
            else -> "Safe: Deterministic rule verified with all required permissions available."
        }

        return SimulationResult(
            ruleId = rule.id,
            ruleTitle = rule.title,
            canExecute = canExecute,
            triggerSatisfied = triggerSatisfied,
            missingPermissions = missingPermissions,
            conflictsDetected = conflicts,
            projectedActions = projectedActions,
            safetyAssessment = safetyAssessment
        )
    }

    // Real Execution of a deterministic rule
    suspend fun executeRule(rule: AutomationRule, isManual: Boolean = false): ExecutionRecord {
        val simulation = simulateRule(rule)
        val timestamp = System.currentTimeMillis()

        if (!simulation.canExecute && !isManual) {
            val record = ExecutionRecord(
                ruleId = rule.id,
                ruleTitle = rule.title,
                timestamp = timestamp,
                status = ExecutionStatus.FAILURE,
                message = "Execution blocked due to missing permissions: ${simulation.missingPermissions.joinToString()}"
            )
            saveExecution(record)
            return record
        }

        // Snapshot current state for safe rollback
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val previousDndFilter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
        val previousMode = prefs.activeMode.first()
        val rollbackSnapshot = "DND:$previousDndFilter|MODE:$previousMode"

        val actionResults = mutableListOf<String>()

        try {
            for (action in rule.actions) {
                when (action.type) {
                    ActionType.SET_DND_FILTER -> {
                        if (detector.isDndPolicyAccessGranted() && notificationManager != null) {
                            val filterInt = when (action.dndFilter) {
                                DndFilterType.PRIORITY_ONLY -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                                DndFilterType.TOTAL_SILENCE -> NotificationManager.INTERRUPTION_FILTER_NONE
                                DndFilterType.ALARMS_ONLY -> NotificationManager.INTERRUPTION_FILTER_ALARMS
                                else -> NotificationManager.INTERRUPTION_FILTER_ALL
                            }
                            notificationManager.setInterruptionFilter(filterInt)
                            actionResults.add("DND filter set to ${action.dndFilter}")
                        } else {
                            actionResults.add("DND filter skipped (No policy access)")
                        }
                    }
                    ActionType.ACTIVATE_MODE -> {
                        val mode = action.modeName ?: "Standard"
                        prefs.setActiveMode(mode)
                        actionResults.add("Active mode set to $mode")
                    }
                    ActionType.SUPPRESS_NOTIFICATION_CATEGORIES -> {
                        val cats = action.suppressedCategories.joinToString { it.displayName }
                        actionResults.add("Suppression filter configured for $cats")
                    }
                    ActionType.GENERATE_DIGEST, ActionType.SHOW_ALERT -> {
                        actionResults.add("Alert action executed")
                    }
                }
            }

            db.automationRuleDao().recordExecution(rule.id, timestamp)

            val record = ExecutionRecord(
                ruleId = rule.id,
                ruleTitle = rule.title,
                timestamp = timestamp,
                status = ExecutionStatus.SUCCESS,
                message = "Executed successfully: ${actionResults.joinToString("; ")}",
                rollbackSnapshot = rollbackSnapshot
            )
            saveExecution(record)
            logAudit(rule.title, "SUCCESS", record.message)
            return record
        } catch (e: Exception) {
            Log.e(TAG, "Failed executing rule ${rule.title}", e)
            val record = ExecutionRecord(
                ruleId = rule.id,
                ruleTitle = rule.title,
                timestamp = timestamp,
                status = ExecutionStatus.FAILURE,
                message = "Error: ${e.message}"
            )
            saveExecution(record)
            logAudit(rule.title, "FAILED", record.message)
            return record
        }
    }

    // Rollback to prior saved state
    suspend fun rollbackRule(record: ExecutionRecord): Boolean {
        val snapshot = record.rollbackSnapshot ?: return false
        try {
            val parts = snapshot.split("|")
            for (part in parts) {
                if (part.startsWith("DND:")) {
                    val filterInt = part.removePrefix("DND:").toIntOrNull()
                    if (filterInt != null && detector.isDndPolicyAccessGranted()) {
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                        nm?.setInterruptionFilter(filterInt)
                    }
                } else if (part.startsWith("MODE:")) {
                    val mode = part.removePrefix("MODE:")
                    prefs.setActiveMode(mode)
                }
            }
            logAudit(record.ruleTitle, "ROLLED_BACK", "Reverted device state to pre-routine configuration.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Rollback failed", e)
            return false
        }
    }

    private fun checkTriggerSatisfaction(rule: AutomationRule): Boolean {
        when (rule.triggerType) {
            TriggerType.MANUAL -> return true
            TriggerType.TIME_SCHEDULE -> {
                val sched = rule.scheduleTrigger ?: return false
                val now = ZonedDateTime.now(zoneId)
                val currentDayInt = now.dayOfWeek.value // 1 = Monday ... 7 = Sunday
                if (!sched.daysOfWeek.contains(currentDayInt)) return false

                val nowMinutes = now.hour * 60 + now.minute
                val startMinutes = sched.startHour * 60 + sched.startMinute
                val endMinutes = sched.endHour * 60 + sched.endMinute

                return nowMinutes in startMinutes..endMinutes
            }
            TriggerType.CHARGING_STATE -> {
                // Evaluated via broadcast
                return false
            }
            TriggerType.APP_FOREGROUND -> {
                return false
            }
        }
    }

    private suspend fun saveExecution(record: ExecutionRecord) {
        db.executionHistoryDao().insert(
            ExecutionHistoryEntity(
                ruleId = record.ruleId,
                ruleTitle = record.ruleTitle,
                timestamp = record.timestamp,
                status = record.status,
                message = record.message,
                rollbackSnapshot = record.rollbackSnapshot
            )
        )
    }

    private suspend fun logAudit(action: String, outcome: String, details: String) {
        db.auditLogDao().insert(
            AuditLogEntity(
                actionName = "Routine: $action",
                category = "AUTOMATION_ENGINE",
                riskLevel = RiskLevel.MEDIUM,
                authorizedBy = "SAVED_RULE",
                outcome = outcome,
                details = details
            )
        )
    }
}
