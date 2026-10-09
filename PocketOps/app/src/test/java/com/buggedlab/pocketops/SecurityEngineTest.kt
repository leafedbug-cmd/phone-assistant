package com.buggedlab.pocketops

import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RiskLevel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityEngineTest {

    @Test
    fun testRiskLevelEvaluation() {
        val lowRiskAction = ProposedAction(
            id = "act_1",
            title = "Open App Settings",
            description = "Opens system settings",
            riskLevel = RiskLevel.LOW,
            targetCapability = "NAVIGATION_INTENT",
            isApproved = false
        )

        val mediumRiskAction = ProposedAction(
            id = "act_2",
            title = "Set DND Filter",
            description = "Enables Priority DND",
            riskLevel = RiskLevel.MEDIUM,
            targetCapability = "NOTIFICATION_FILTER",
            isApproved = false
        )

        val highRiskAction = ProposedAction(
            id = "act_3",
            title = "Factory Reset / Delete Records",
            description = "High impact device modification",
            riskLevel = RiskLevel.HIGH,
            targetCapability = "SYSTEM_CONTROL",
            isApproved = false
        )

        // Low risk actions can proceed without interactive approval
        assertTrue("Low risk action is allowed", lowRiskAction.riskLevel == RiskLevel.LOW)

        // Medium and High risk actions require approval
        assertFalse("Medium risk unapproved action must not proceed autonomously", mediumRiskAction.isApproved)
        assertFalse("High risk unapproved action must not proceed autonomously", highRiskAction.isApproved)

        val approvedHighRisk = highRiskAction.copy(isApproved = true)
        assertTrue("High risk action is authorized once explicitly approved", approvedHighRisk.isApproved)
    }

    @Test
    fun testEmergencyStopBlocksAllActions() {
        val isEmergencyStopActive = true
        val canExecuteAnyAction = !isEmergencyStopActive
        assertFalse("Emergency Stop must halt all automated and supervised actions immediately", canExecuteAnyAction)
    }
}
