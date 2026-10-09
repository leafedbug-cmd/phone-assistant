package com.buggedlab.pocketops.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buggedlab.pocketops.ui.PocketOpsViewModel
import com.buggedlab.pocketops.ui.activity.ActivityScreen
import com.buggedlab.pocketops.ui.aicontrol.AiControlScreen
import com.buggedlab.pocketops.ui.components.NavigationTab
import com.buggedlab.pocketops.ui.components.PocketOpsBottomNavBar
import com.buggedlab.pocketops.ui.components.PocketOpsTopBar
import com.buggedlab.pocketops.ui.home.HomeScreen
import com.buggedlab.pocketops.ui.notifications.NotificationsScreen
import com.buggedlab.pocketops.ui.onboarding.OnboardingScreen
import com.buggedlab.pocketops.ui.recommendations.RecommendationsScreen
import com.buggedlab.pocketops.ui.routines.RoutinesScreen
import com.buggedlab.pocketops.ui.settings.SettingsScreen

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: PocketOpsViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Automatically refresh permission status and notifications on app resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val capabilities by viewModel.capabilities.collectAsStateWithLifecycle()
    val activeMode by viewModel.activeMode.collectAsStateWithLifecycle()
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    val currentDigest by viewModel.currentDigest.collectAsStateWithLifecycle()
    val isGeneratingDigest by viewModel.isGeneratingDigest.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val simulationResult by viewModel.simulationResult.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val isEmergencyStopActive by viewModel.isEmergencyStopActive.collectAsStateWithLifecycle()
    val isAiOptedIn by viewModel.isAiOptedIn.collectAsStateWithLifecycle()
    val geminiModel by viewModel.geminiModel.collectAsStateWithLifecycle()
    val hasApiKey by viewModel.hasApiKey.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onCompleteOnboarding = { viewModel.completeOnboarding() },
            onOpenNotificationAccess = { viewModel.openNotificationListenerSettings() },
            onOpenAccessibilityAccess = { viewModel.openAccessibilitySettings() }
        )
    } else {
        Scaffold(
            topBar = {
                PocketOpsTopBar(
                    activeMode = activeMode,
                    isEmergencyStopActive = isEmergencyStopActive,
                    onEmergencyStopToggle = { viewModel.setEmergencyStop(!isEmergencyStopActive) }
                )
            },
            bottomBar = {
                PocketOpsBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            capabilities = capabilities,
                            activeMode = activeMode,
                            dashboardData = dashboardData,
                            onModeChange = { viewModel.setMode(it) },
                            onSubmitAiPrompt = { prompt ->
                                viewModel.selectTab(NavigationTab.AI_CONTROL)
                                viewModel.submitAiPrompt(prompt)
                            },
                            onNavigateToNotifications = { viewModel.selectTab(NavigationTab.NOTIFICATIONS) },
                            onNavigateToRoutines = { viewModel.selectTab(NavigationTab.ROUTINES) },
                            onNavigateToSettings = { viewModel.selectTab(NavigationTab.SETTINGS) }
                        )
                    }

                    NavigationTab.AI_CONTROL -> {
                        AiControlScreen(
                            messages = chatMessages,
                            isLoading = isAiLoading,
                            onSendMessage = { viewModel.submitAiPrompt(it) },
                            onApprovePlan = { viewModel.approveAndExecutePlan(it) },
                            onDryRunPlan = { viewModel.dryRunPlan(it) },
                            onRejectPlan = { viewModel.rejectPlan(it) }
                        )
                    }

                    NavigationTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            dashboardData = dashboardData,
                            currentDigest = currentDigest,
                            isGeneratingDigest = isGeneratingDigest,
                            onGenerateDigest = { viewModel.generateDigestNow() }
                        )
                    }

                    NavigationTab.ROUTINES -> {
                        RoutinesScreen(
                            rules = rules,
                            simulationResult = simulationResult,
                            onToggleRule = { rule, enabled -> viewModel.toggleRule(rule, enabled) },
                            onSimulateRule = { viewModel.simulateRule(it) },
                            onExecuteRule = { viewModel.executeRule(it) },
                            onCreateRule = { viewModel.createRule(it) },
                            onDismissSimulation = { viewModel.dismissSimulation() }
                        )
                    }

                    NavigationTab.RECOMMENDATIONS -> {
                        RecommendationsScreen(
                            recommendations = recommendations,
                            onAcceptRecommendation = { viewModel.acceptRecommendation(it) },
                            onSimulateRecommendation = { viewModel.simulateRule(it) },
                            onDismissRecommendation = { viewModel.dismissRecommendation(it) }
                        )
                    }

                    NavigationTab.ACTIVITY -> {
                        ActivityScreen(
                            auditLogs = auditLogs
                        )
                    }

                    NavigationTab.SETTINGS -> {
                        SettingsScreen(
                            capabilities = capabilities,
                            isAiOptedIn = isAiOptedIn,
                            geminiModel = geminiModel,
                            hasApiKey = hasApiKey,
                            isEmergencyStopActive = isEmergencyStopActive,
                            onSaveApiKey = { viewModel.saveApiKey(it) },
                            onToggleAiOptIn = { viewModel.toggleAiOptIn(it) },
                            onSelectModel = { viewModel.selectGeminiModel(it) },
                            onToggleEmergencyStop = { viewModel.setEmergencyStop(it) },
                            onOpenNotificationListenerSettings = { viewModel.openNotificationListenerSettings() },
                            onOpenAccessibilitySettings = { viewModel.openAccessibilitySettings() },
                            onOpenDndSettings = { viewModel.openDndSettings() },
                            onOpenBatteryOptSettings = { viewModel.openBatteryOptimizationSettings() },
                            onOpenExactAlarmSettings = { viewModel.openExactAlarmSettings() },
                            onOpenSamsungRoutines = { viewModel.openSamsungRoutines() }
                        )
                    }
                }
            }
        }
    }
}
