package com.buggedlab.pocketops.engine.ai

import android.content.Context
import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.data.db.NotificationEntity
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import com.buggedlab.pocketops.engine.capability.DeviceCapabilityDetector
import kotlinx.coroutines.flow.first

class AiManager(private val context: Context) {
    private val prefs = PocketOpsPreferences(context)
    private val detector = DeviceCapabilityDetector(context)
    private val offlineProvider = OfflineRuleAiProvider()

    suspend fun processRequest(request: String): AiPlan {
        val isOptedIn = prefs.isAiOptedIn.first()
        val hasKey = prefs.hasApiKey()
        val capabilities = detector.detectCapabilities()

        val provider: AiProvider = if (isOptedIn && hasKey) {
            val model = prefs.geminiModel.first()
            GeminiAiProvider(
                apiKeyProvider = { prefs.getApiKey() },
                modelName = model
            )
        } else {
            offlineProvider
        }

        return provider.parseUserRequest(request, capabilities)
    }

    suspend fun generateAiDigestSummary(notifications: List<NotificationEntity>): String {
        return offlineProvider.summarizeDigest(notifications)
    }
}
