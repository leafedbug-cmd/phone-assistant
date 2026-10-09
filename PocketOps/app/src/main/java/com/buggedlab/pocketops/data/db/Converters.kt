package com.buggedlab.pocketops.data.db

import androidx.room.TypeConverter
import com.buggedlab.pocketops.core.model.ActionType
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.DndFilterType
import com.buggedlab.pocketops.core.model.ExecutionStatus
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.core.model.TimeScheduleTrigger
import com.buggedlab.pocketops.core.model.TriggerType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromNotificationCategory(category: NotificationCategory): String = category.name

    @TypeConverter
    fun toNotificationCategory(value: String): NotificationCategory =
        runCatching { NotificationCategory.valueOf(value) }.getOrDefault(NotificationCategory.UNKNOWN)

    @TypeConverter
    fun fromTriggerType(type: TriggerType): String = type.name

    @TypeConverter
    fun toTriggerType(value: String): TriggerType =
        runCatching { TriggerType.valueOf(value) }.getOrDefault(TriggerType.MANUAL)

    @TypeConverter
    fun fromTimeScheduleTrigger(trigger: TimeScheduleTrigger?): String? =
        trigger?.let { json.encodeToString(it) }

    @TypeConverter
    fun toTimeScheduleTrigger(value: String?): TimeScheduleTrigger? =
        value?.let { runCatching { json.decodeFromString<TimeScheduleTrigger>(it) }.getOrNull() }

    @TypeConverter
    fun fromAutomationActions(actions: List<AutomationAction>): String =
        json.encodeToString(actions)

    @TypeConverter
    fun toAutomationActions(value: String): List<AutomationAction> =
        runCatching { json.decodeFromString<List<AutomationAction>>(value) }.getOrDefault(emptyList())

    @TypeConverter
    fun fromExecutionStatus(status: ExecutionStatus): String = status.name

    @TypeConverter
    fun toExecutionStatus(value: String): ExecutionStatus =
        runCatching { ExecutionStatus.valueOf(value) }.getOrDefault(ExecutionStatus.FAILURE)

    @TypeConverter
    fun fromRiskLevel(risk: RiskLevel): String = risk.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel =
        runCatching { RiskLevel.valueOf(value) }.getOrDefault(RiskLevel.LOW)
}
