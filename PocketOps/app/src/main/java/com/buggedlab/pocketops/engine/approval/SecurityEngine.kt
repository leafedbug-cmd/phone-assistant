package com.buggedlab.pocketops.engine.approval

import android.content.Context
import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.data.db.AuditLogEntity
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import kotlinx.coroutines.flow.first

sealed class AuthorizationResult {
    data object Authorized : AuthorizationResult()
    data class Denied(val reason: String) : AuthorizationResult()
}

class SecurityEngine(private val context: Context) {
    private val db = PocketOpsDatabase.getInstance(context)
    private val prefs = PocketOpsPreferences(context)

    suspend fun evaluateActionAuthorization(
        action: ProposedAction,
        isUserSupervisedSession: Boolean = false
    ): AuthorizationResult {
        // 1. Check Emergency Stop
        if (prefs.isEmergencyStopActive.first()) {
            val reason = "Emergency Stop is currently active. All automation and phone control actions are halted."
            logAudit(action.title, action.riskLevel, "BLOCKED", reason)
            return AuthorizationResult.Denied(reason)
        }

        // 2. High-Risk / Medium-Risk Authorization Check
        if (action.riskLevel == RiskLevel.HIGH && !action.isApproved) {
            val reason = "High-risk operation requires explicit, interactive user approval before execution."
            logAudit(action.title, action.riskLevel, "BLOCKED", reason)
            return AuthorizationResult.Denied(reason)
        }

        if (action.riskLevel == RiskLevel.MEDIUM && !action.isApproved && !isUserSupervisedSession) {
            val reason = "Medium-risk configuration change requires user confirmation."
            logAudit(action.title, action.riskLevel, "BLOCKED", reason)
            return AuthorizationResult.Denied(reason)
        }

        // 3. Denylist check if target app is specified
        val targetPkg = action.parameters["targetPackage"]
        if (!targetPkg.isNullOrBlank()) {
            val appConfig = db.appConfigDao().getByPackage(targetPkg)
            if (appConfig?.isDenylistedFromAiControl == true) {
                val reason = "Target package '$targetPkg' is on the security denylist."
                logAudit(action.title, action.riskLevel, "BLOCKED", reason)
                return AuthorizationResult.Denied(reason)
            }
        }

        logAudit(action.title, action.riskLevel, "AUTHORIZED", "Action cleared by PocketOps Security Engine.")
        return AuthorizationResult.Authorized
    }

    private suspend fun logAudit(action: String, risk: RiskLevel, outcome: String, details: String) {
        db.auditLogDao().insert(
            AuditLogEntity(
                actionName = action,
                category = "AUTHORIZATION_ENGINE",
                riskLevel = risk,
                authorizedBy = "SECURITY_ENGINE",
                outcome = outcome,
                details = details
            )
        )
    }
}
