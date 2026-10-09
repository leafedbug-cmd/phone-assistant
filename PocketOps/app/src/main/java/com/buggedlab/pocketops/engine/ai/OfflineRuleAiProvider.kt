package com.buggedlab.pocketops.engine.ai

import com.buggedlab.pocketops.core.model.AiIntent
import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.core.model.PlanStatus
import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.data.db.NotificationEntity
import java.util.UUID

class OfflineRuleAiProvider : AiProvider {

    override suspend fun parseUserRequest(userRequest: String, capabilities: DeviceCapabilities): AiPlan {
        val prompt = userRequest.lowercase().trim()
        val planId = UUID.randomUUID().toString()

        return when {
            // Test C: "Open my notification settings"
            prompt.contains("notification setting") || (prompt.contains("open") && prompt.contains("setting") && prompt.contains("notification")) -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.OPEN_SETTINGS,
                    explanation = "I identified that you want to adjust notification settings on your ${capabilities.model}. I have prepared a direct navigation action to open Android's notification settings.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_open_notif_settings",
                            title = "Open Notification Settings",
                            description = "Navigate to Android Notification Settings using a system intent.",
                            riskLevel = RiskLevel.LOW,
                            targetCapability = "NAVIGATION_INTENT",
                            parameters = mapOf("target" to "NOTIFICATION_LISTENER_SETTINGS")
                        )
                    ),
                    requiresUserApproval = false,
                    status = PlanStatus.APPROVED
                )
            }

            // "Open my phone settings and help me change something"
            prompt.contains("open") && (prompt.contains("setting") || prompt.contains("settings")) -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.OPEN_SETTINGS,
                    explanation = "I can open your device settings. Once open, you can supervise each navigation step.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_open_app_settings",
                            title = "Open Phone Settings",
                            description = "Launch Android System Settings.",
                            riskLevel = RiskLevel.LOW,
                            targetCapability = "NAVIGATION_INTENT",
                            parameters = mapOf("target" to "SYSTEM_SETTINGS")
                        )
                    ),
                    requiresUserApproval = false,
                    status = PlanStatus.APPROVED
                )
            }

            // "Organize my notifications so I don't get distracted at work"
            prompt.contains("distract") || (prompt.contains("organize") && prompt.contains("notification")) -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.ORGANIZE_NOTIFICATIONS,
                    explanation = "To prevent distractions at work, I recommend applying the 'Work Focus' profile. This profile filters non-urgent Social and Shopping alerts while allowing Critical security alerts, calls, and work messages.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_activate_work_mode",
                            title = "Activate Work Mode",
                            description = "Switches active PocketOps profile to 'Work' and enables background filtering for social and promotional notifications.",
                            riskLevel = RiskLevel.MEDIUM,
                            targetCapability = "NOTIFICATION_FILTER",
                            parameters = mapOf("mode" to "Work", "suppress" to "SHOPPING,SOCIAL")
                        )
                    ),
                    requiresUserApproval = true,
                    status = PlanStatus.PENDING_APPROVAL
                )
            }

            // "Show me which apps send the most useless notifications" / "noisy apps"
            prompt.contains("useless") || prompt.contains("noisy") || prompt.contains("bother") || prompt.contains("most notifications") -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.FIND_NOISY_APPS,
                    explanation = "I analyzed notification frequency and identified that shopping and promotional apps generate the highest volume of non-essential alerts.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_view_analytics",
                            title = "View Notification Analytics",
                            description = "Inspect the live analytics dashboard for noisy apps and category breakdowns.",
                            riskLevel = RiskLevel.LOW,
                            targetCapability = "ANALYTICS_VIEW"
                        )
                    ),
                    requiresUserApproval = false,
                    status = PlanStatus.APPROVED
                )
            }

            // "Suggest some useful Samsung modes and routines"
            prompt.contains("samsung") || prompt.contains("one ui") || prompt.contains("modes and routines") -> {
                val oneUiText = capabilities.oneUiVersion ?: "Samsung One UI"
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.SAMSUNG_ROUTINE_HELP,
                    explanation = "On $oneUiText, Samsung Modes & Routines offers deep OS-level integration. I recommend setting up: 1) 'Deep Work Mode' (turns on Grayscale and limits notifications), 2) 'Night Calm' (DND + Eye Comfort Shield), and 3) 'Battery Safeguard' (turns on Power Saving below 20%).",
                    actions = listOf(
                        ProposedAction(
                            id = "action_open_samsung_routines",
                            title = "Open Samsung Modes & Routines",
                            description = "Launch Samsung's native Modes & Routines settings interface.",
                            riskLevel = RiskLevel.LOW,
                            targetCapability = "SAMSUNG_INTEGRATION"
                        )
                    ),
                    requiresUserApproval = false,
                    status = PlanStatus.APPROVED
                )
            }

            // "Make a routine that silences social media while I'm working" / "turn my approved work mode on automatically every weekday"
            prompt.contains("routine") || prompt.contains("weekday") || prompt.contains("silence") -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.CREATE_ROUTINE,
                    explanation = "I have drafted a deterministic weekday routine: 'Workday Focus (8 AM - 5 PM)'. It automatically applies Priority DND and filters social media notifications Monday through Friday.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_create_work_routine",
                            title = "Create Weekday Work Routine",
                            description = "Schedule deterministic automation: Mon-Fri, 8:00 AM - 5:00 PM. Suppresses Social and Shopping notifications; enables Priority DND.",
                            riskLevel = RiskLevel.MEDIUM,
                            targetCapability = "AUTOMATION_ENGINE",
                            parameters = mapOf(
                                "title" to "Workday Focus (8 AM - 5 PM)",
                                "startHour" to "8",
                                "startMinute" to "0",
                                "endHour" to "17",
                                "endMinute" to "0",
                                "days" to "1,2,3,4,5",
                                "dnd" to "PRIORITY_ONLY"
                            )
                        )
                    ),
                    requiresUserApproval = true,
                    status = PlanStatus.PENDING_APPROVAL
                )
            }

            // "Every evening, give me a summary of notifications I missed" / "summary" / "digest"
            prompt.contains("evening") || prompt.contains("summary") || prompt.contains("digest") || prompt.contains("missed") -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.GENERATE_DIGEST,
                    explanation = "I can generate an on-demand summary of missed notifications and schedule a daily evening digest at 6:00 PM.",
                    actions = listOf(
                        ProposedAction(
                            id = "action_generate_digest",
                            title = "Generate Notification Digest",
                            description = "Compile all categorized notifications received today into a structured summary.",
                            riskLevel = RiskLevel.LOW,
                            targetCapability = "DIGEST_GENERATOR"
                        )
                    ),
                    requiresUserApproval = false,
                    status = PlanStatus.APPROVED
                )
            }

            // Default general assistant
            else -> {
                AiPlan(
                    id = planId,
                    userPrompt = userRequest,
                    intent = AiIntent.GENERAL_QUERY,
                    explanation = "I understood your request: '$userRequest'. You can manage notifications, create automation routines, or inspect your device capabilities from the tabs below.",
                    actions = emptyList(),
                    requiresUserApproval = false,
                    status = PlanStatus.COMPLETED
                )
            }
        }
    }

    override suspend fun summarizeDigest(notifications: List<NotificationEntity>): String {
        if (notifications.isEmpty()) return "No notifications recorded today."
        val topApps = notifications.groupBy { it.appName }
            .entries.sortedByDescending { it.value.size }
            .take(3)
            .joinToString { "${it.key} (${it.value.size})" }
        return "You received ${notifications.size} total notifications today. Most frequent sources: $topApps."
    }
}
