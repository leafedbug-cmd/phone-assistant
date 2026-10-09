package com.buggedlab.pocketops.service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.AutomationRule
import com.buggedlab.pocketops.core.model.TriggerType
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.engine.automation.AutomationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PocketOpsReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        Log.i(TAG, "Received broadcast intent: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = PocketOpsDatabase.getInstance(context)
                val scheduler = AutomationScheduler(context)

                when (action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        Log.i(TAG, "Device reboot completed. Recovering and validating enabled routines.")
                        // Check if any scheduled routines should be evaluated now
                        val enabledRules = db.automationRuleDao().getEnabledRules()
                        for (ruleEntity in enabledRules) {
                            if (ruleEntity.triggerType == TriggerType.TIME_SCHEDULE) {
                                val rule = AutomationRule(
                                    id = ruleEntity.id,
                                    title = ruleEntity.title,
                                    description = ruleEntity.description,
                                    isEnabled = ruleEntity.isEnabled,
                                    priority = ruleEntity.priority,
                                    triggerType = ruleEntity.triggerType,
                                    scheduleTrigger = ruleEntity.scheduleTrigger,
                                    actions = ruleEntity.actions,
                                    conflictGroup = ruleEntity.conflictGroup
                                )
                                val sim = scheduler.simulateRule(rule)
                                if (sim.triggerSatisfied && sim.canExecute) {
                                    scheduler.executeRule(rule)
                                }
                            }
                        }
                    }

                    Intent.ACTION_POWER_CONNECTED, Intent.ACTION_POWER_DISCONNECTED -> {
                        val isConnected = action == Intent.ACTION_POWER_CONNECTED
                        val chargingRules = db.automationRuleDao().getEnabledRulesByTriggerType(TriggerType.CHARGING_STATE)
                        for (ruleEntity in chargingRules) {
                            if (ruleEntity.chargingTrigger == isConnected) {
                                val rule = AutomationRule(
                                    id = ruleEntity.id,
                                    title = ruleEntity.title,
                                    description = ruleEntity.description,
                                    isEnabled = ruleEntity.isEnabled,
                                    priority = ruleEntity.priority,
                                    triggerType = ruleEntity.triggerType,
                                    chargingTrigger = ruleEntity.chargingTrigger,
                                    actions = ruleEntity.actions,
                                    conflictGroup = ruleEntity.conflictGroup
                                )
                                scheduler.executeRule(rule)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling broadcast $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
