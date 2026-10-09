package com.buggedlab.pocketops.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.TimeScheduleTrigger
import com.buggedlab.pocketops.core.model.TriggerType

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val isEnabled: Boolean = true,
    val priority: Int = 1,
    val triggerType: TriggerType,
    val scheduleTrigger: TimeScheduleTrigger? = null,
    val chargingTrigger: Boolean? = null,
    val appTriggerPackage: String? = null,
    val actions: List<AutomationAction>,
    val conflictGroup: String = "DEFAULT",
    val rollbackSupported: Boolean = true,
    val lastExecutedAt: Long? = null,
    val nextRunAt: Long? = null,
    val executionCount: Int = 0,
    val retryLimit: Int = 3
)
