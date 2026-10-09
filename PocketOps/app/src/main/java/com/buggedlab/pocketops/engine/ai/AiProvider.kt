package com.buggedlab.pocketops.engine.ai

import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.data.db.NotificationEntity

interface AiProvider {
    suspend fun parseUserRequest(userRequest: String, capabilities: DeviceCapabilities): AiPlan
    suspend fun summarizeDigest(notifications: List<NotificationEntity>): String
}
