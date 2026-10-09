package com.buggedlab.pocketops.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.buggedlab.pocketops.core.model.ActionType
import com.buggedlab.pocketops.core.model.AiIntent
import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.core.model.AuditLogEntry
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.AutomationRule
import com.buggedlab.pocketops.core.model.ChatMessage
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.core.model.DevicePermissions
import com.buggedlab.pocketops.core.model.DndFilterType
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.NotificationDashboardData
import com.buggedlab.pocketops.core.model.PlanStatus
import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RecommendationItem
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.core.model.SimulationResult
import com.buggedlab.pocketops.core.model.TimeScheduleTrigger
import com.buggedlab.pocketops.core.model.TriggerType
import com.buggedlab.pocketops.data.db.AutomationRuleEntity
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import com.buggedlab.pocketops.engine.ai.AiManager
import com.buggedlab.pocketops.engine.approval.AuthorizationResult
import com.buggedlab.pocketops.engine.approval.SecurityEngine
import com.buggedlab.pocketops.engine.automation.AutomationScheduler
import com.buggedlab.pocketops.engine.capability.DeviceCapabilityDetector
import com.buggedlab.pocketops.engine.notification.DigestGenerator
import com.buggedlab.pocketops.engine.notification.NotificationAnalyticsEngine
import com.buggedlab.pocketops.engine.notification.NotificationDigest
import com.buggedlab.pocketops.engine.samsung.SamsungRoutineAssistant
import com.buggedlab.pocketops.service.accessibility.PocketOpsAccessibilityService
import com.buggedlab.pocketops.service.notification.PocketOpsNotificationListenerService
import com.buggedlab.pocketops.ui.components.NavigationTab
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PocketOpsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = PocketOpsDatabase.getInstance(context)
    private val prefs = PocketOpsPreferences(context)
    private val detector = DeviceCapabilityDetector(context)
    private val analyticsEngine = NotificationAnalyticsEngine(db.notificationDao())
    private val digestGenerator = DigestGenerator(db.notificationDao())
    private val scheduler = AutomationScheduler(context)
    private val aiManager = AiManager(context)
    private val securityEngine = SecurityEngine(context)
    private val samsungAssistant = SamsungRoutineAssistant(context)

    // Navigation State
    private val _selectedTab = MutableStateFlow(NavigationTab.HOME)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    // Device Capabilities & Permissions
    private val _capabilities = MutableStateFlow(detector.detectCapabilities())
    val capabilities: StateFlow<DeviceCapabilities> = _capabilities.asStateFlow()

    // Active Focus Profile
    private val _activeMode = MutableStateFlow("Standard")
    val activeMode: StateFlow<String> = _activeMode.asStateFlow()

    // Dashboard Data
    private val _dashboardData = MutableStateFlow(
        NotificationDashboardData(0, 0, emptyList(), emptyMap(), emptyList(), 0)
    )
    val dashboardData: StateFlow<NotificationDashboardData> = _dashboardData.asStateFlow()

    // Digest
    private val _currentDigest = MutableStateFlow<NotificationDigest?>(null)
    val currentDigest: StateFlow<NotificationDigest?> = _currentDigest.asStateFlow()

    private val _isGeneratingDigest = MutableStateFlow(false)
    val isGeneratingDigest: StateFlow<Boolean> = _isGeneratingDigest.asStateFlow()

    // AI Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Automation Rules
    private val _rules = MutableStateFlow<List<AutomationRule>>(emptyList())
    val rules: StateFlow<List<AutomationRule>> = _rules.asStateFlow()

    // Simulation Result
    private val _simulationResult = MutableStateFlow<SimulationResult?>(null)
    val simulationResult: StateFlow<SimulationResult?> = _simulationResult.asStateFlow()

    // Recommendations
    private val _recommendations = MutableStateFlow<List<RecommendationItem>>(emptyList())
    val recommendations: StateFlow<List<RecommendationItem>> = _recommendations.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Settings States
    private val _isEmergencyStopActive = MutableStateFlow(false)
    val isEmergencyStopActive: StateFlow<Boolean> = _isEmergencyStopActive.asStateFlow()

    private val _isAiOptedIn = MutableStateFlow(false)
    val isAiOptedIn: StateFlow<Boolean> = _isAiOptedIn.asStateFlow()

    private val _geminiModel = MutableStateFlow("gemini-2.0-flash")
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    private val _hasApiKey = MutableStateFlow(prefs.hasApiKey())
    val hasApiKey: StateFlow<Boolean> = _hasApiKey.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(true)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        refreshState()
        observeData()
        seedInitialRulesIfNeeded()
    }

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun refreshState() {
        viewModelScope.launch {
            _capabilities.value = detector.detectCapabilities()
            _activeMode.value = prefs.activeMode.first()
            _isEmergencyStopActive.value = prefs.isEmergencyStopActive.first()
            _isAiOptedIn.value = prefs.isAiOptedIn.first()
            _geminiModel.value = prefs.geminiModel.first()
            _hasApiKey.value = prefs.hasApiKey()
            _isOnboardingCompleted.value = prefs.isOnboardingCompleted.first()

            val stats = analyticsEngine.getDashboardData()
            _dashboardData.value = stats

            refreshRecommendations(stats)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            db.automationRuleDao().observeAll().collect { entities ->
                _rules.value = entities.map { it.toDomain() }
            }
        }

        viewModelScope.launch {
            db.auditLogDao().observeRecent(50).collect { logs ->
                _auditLogs.value = logs.map {
                    AuditLogEntry(
                        id = it.id,
                        timestamp = it.timestamp,
                        actionName = it.actionName,
                        category = it.category,
                        riskLevel = it.riskLevel,
                        authorizedBy = it.authorizedBy,
                        outcome = it.outcome,
                        details = it.details
                    )
                }
            }
        }

        // Live notification listener update
        viewModelScope.launch {
            PocketOpsNotificationListenerService.latestNotification.collect {
                val stats = analyticsEngine.getDashboardData()
                _dashboardData.value = stats
            }
        }
    }

    private fun seedInitialRulesIfNeeded() {
        viewModelScope.launch {
            if (db.automationRuleDao().getCount() == 0) {
                // Pre-populate with approved configurable Work Focus template (disabled by default until user approval)
                val workRule = AutomationRuleEntity(
                    title = "Workday Focus (8 AM - 5 PM)",
                    description = "Silences social media & shopping notifications; activates Priority DND",
                    isEnabled = false,
                    priority = 1,
                    triggerType = TriggerType.TIME_SCHEDULE,
                    scheduleTrigger = TimeScheduleTrigger(
                        startHour = 8,
                        startMinute = 0,
                        endHour = 17,
                        endMinute = 0,
                        daysOfWeek = listOf(1, 2, 3, 4, 5)
                    ),
                    actions = listOf(
                        AutomationAction(
                            type = ActionType.SET_DND_FILTER,
                            dndFilter = DndFilterType.PRIORITY_ONLY
                        ),
                        AutomationAction(
                            type = ActionType.SUPPRESS_NOTIFICATION_CATEGORIES,
                            suppressedCategories = listOf(NotificationCategory.SOCIAL, NotificationCategory.SHOPPING)
                        ),
                        AutomationAction(
                            type = ActionType.ACTIVATE_MODE,
                            modeName = "Work"
                        )
                    ),
                    conflictGroup = "WORK_HOURS"
                )
                db.automationRuleDao().insert(workRule)
            }
        }
    }

    private fun refreshRecommendations(dashboardData: NotificationDashboardData) {
        val list = mutableListOf<RecommendationItem>()

        // Recommendation 1: Work Routine
        list.add(
            RecommendationItem(
                id = "rec_work_focus",
                title = "Automate Workday Focus",
                description = "Activate Priority DND and filter social & shopping alerts weekdays from 8 AM to 5 PM.",
                rationale = "Shields you from non-urgent distraction during business hours while keeping emergency calls and VIP messages accessible.",
                proposedRule = AutomationRule(
                    id = 101,
                    title = "Workday Focus (8 AM - 5 PM)",
                    description = "Silences social media & shopping alerts during working hours",
                    isEnabled = true,
                    priority = 2,
                    triggerType = TriggerType.TIME_SCHEDULE,
                    scheduleTrigger = TimeScheduleTrigger(8, 0, 17, 0, listOf(1, 2, 3, 4, 5)),
                    actions = listOf(
                        AutomationAction(ActionType.SET_DND_FILTER, dndFilter = DndFilterType.PRIORITY_ONLY),
                        AutomationAction(ActionType.SUPPRESS_NOTIFICATION_CATEGORIES, suppressedCategories = listOf(NotificationCategory.SOCIAL, NotificationCategory.SHOPPING)),
                        AutomationAction(ActionType.ACTIVATE_MODE, modeName = "Work")
                    ),
                    conflictGroup = "WORK_HOURS"
                )
            )
        )

        // Recommendation 2: Noisy Apps Alert
        if (dashboardData.noisiestApps.isNotEmpty()) {
            val noisy = dashboardData.noisiestApps.first()
            list.add(
                RecommendationItem(
                    id = "rec_mute_${noisy.packageName}",
                    title = "Silence High-Frequency App: ${noisy.appName}",
                    description = "Detected ${noisy.count} alerts from ${noisy.appName}. Consider muting promotional channels.",
                    rationale = "${noisy.appName} is your highest volume source of interruptions.",
                    proposedRule = AutomationRule(
                        id = 102,
                        title = "Filter ${noisy.appName} Promotions",
                        description = "Filters background promotional alerts from ${noisy.appName}",
                        isEnabled = true,
                        priority = 1,
                        triggerType = TriggerType.MANUAL,
                        actions = listOf(
                            AutomationAction(ActionType.SUPPRESS_NOTIFICATION_CATEGORIES, suppressedCategories = listOf(NotificationCategory.SHOPPING))
                        )
                    )
                )
            )
        }

        _recommendations.value = list
    }

    // AI Request Handler
    fun submitAiPrompt(prompt: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = prompt
        )
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            try {
                val plan = aiManager.processRequest(prompt)
                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = plan.explanation,
                    attachedPlan = plan
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    text = "Encountered an issue formulating plan: ${e.message}"
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Approve & Execute AI Plan
    fun approveAndExecutePlan(plan: AiPlan) {
        viewModelScope.launch {
            for (action in plan.actions) {
                val auth = securityEngine.evaluateActionAuthorization(action.copy(isApproved = true))
                if (auth is AuthorizationResult.Authorized) {
                    when (action.targetCapability) {
                        "NAVIGATION_INTENT" -> {
                            val target = action.parameters["target"]
                            if (target == "NOTIFICATION_LISTENER_SETTINGS") {
                                context.startActivity(detector.getNotificationListenerIntent())
                            } else {
                                context.startActivity(detector.getAppDetailsSettingsIntent())
                            }
                        }
                        "NOTIFICATION_FILTER" -> {
                            val mode = action.parameters["mode"] ?: "Work"
                            prefs.setActiveMode(mode)
                            _activeMode.value = mode
                        }
                        "AUTOMATION_ENGINE" -> {
                            // Create and save rule
                            val newRule = AutomationRule(
                                id = System.currentTimeMillis(),
                                title = action.parameters["title"] ?: "AI Approved Routine",
                                description = action.description,
                                isEnabled = true,
                                priority = 2,
                                triggerType = TriggerType.TIME_SCHEDULE,
                                scheduleTrigger = TimeScheduleTrigger(8, 0, 17, 0, listOf(1, 2, 3, 4, 5)),
                                actions = listOf(
                                    AutomationAction(ActionType.SET_DND_FILTER, dndFilter = DndFilterType.PRIORITY_ONLY),
                                    AutomationAction(ActionType.SUPPRESS_NOTIFICATION_CATEGORIES, suppressedCategories = listOf(NotificationCategory.SOCIAL, NotificationCategory.SHOPPING)),
                                    AutomationAction(ActionType.ACTIVATE_MODE, modeName = "Work")
                                ),
                                conflictGroup = "WORK_HOURS"
                            )
                            db.automationRuleDao().insert(newRule.toEntity())
                        }
                        "DIGEST_GENERATOR" -> {
                            generateDigestNow()
                        }
                        "SAMSUNG_INTEGRATION" -> {
                            samsungAssistant.openSamsungRoutinesApp()?.let {
                                context.startActivity(it)
                            }
                        }
                    }
                }
            }

            // Update plan state
            updatePlanInChat(plan.id, PlanStatus.COMPLETED)
        }
    }

    fun dryRunPlan(plan: AiPlan) {
        viewModelScope.launch {
            val dummyRule = AutomationRule(
                id = 999,
                title = plan.actions.firstOrNull()?.title ?: "Plan Simulation",
                description = plan.explanation,
                isEnabled = true,
                priority = 1,
                triggerType = TriggerType.MANUAL,
                actions = listOf(
                    AutomationAction(ActionType.SET_DND_FILTER, dndFilter = DndFilterType.PRIORITY_ONLY)
                )
            )
            _simulationResult.value = scheduler.simulateRule(dummyRule)
            _selectedTab.value = NavigationTab.ROUTINES
        }
    }

    fun rejectPlan(plan: AiPlan) {
        updatePlanInChat(plan.id, PlanStatus.REJECTED)
    }

    private fun updatePlanInChat(planId: String, newStatus: PlanStatus) {
        _chatMessages.value = _chatMessages.value.map { msg ->
            if (msg.attachedPlan?.id == planId) {
                msg.copy(attachedPlan = msg.attachedPlan.copy(status = newStatus))
            } else {
                msg
            }
        }
    }

    // Digest Generation
    fun generateDigestNow() {
        viewModelScope.launch {
            _isGeneratingDigest.value = true
            try {
                val startOfToday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                val digest = digestGenerator.generateDigest(startOfToday)
                _currentDigest.value = digest
            } finally {
                _isGeneratingDigest.value = false
            }
        }
    }

    // Routines Management
    fun toggleRule(rule: AutomationRule, isEnabled: Boolean) {
        viewModelScope.launch {
            db.automationRuleDao().setEnabled(rule.id, isEnabled)
        }
    }

    fun simulateRule(rule: AutomationRule) {
        viewModelScope.launch {
            _simulationResult.value = scheduler.simulateRule(rule)
        }
    }

    fun executeRule(rule: AutomationRule) {
        viewModelScope.launch {
            scheduler.executeRule(rule, isManual = true)
            refreshState()
        }
    }

    fun createRule(rule: AutomationRule) {
        viewModelScope.launch {
            db.automationRuleDao().insert(rule.toEntity())
        }
    }

    fun dismissSimulation() {
        _simulationResult.value = null
    }

    // Recommendations
    fun acceptRecommendation(item: RecommendationItem) {
        viewModelScope.launch {
            db.automationRuleDao().insert(item.proposedRule.toEntity())
            _recommendations.value = _recommendations.value.filter { it.id != item.id }
        }
    }

    fun dismissRecommendation(item: RecommendationItem) {
        _recommendations.value = _recommendations.value.filter { it.id != item.id }
    }

    // Settings
    fun setMode(mode: String) {
        viewModelScope.launch {
            prefs.setActiveMode(mode)
            _activeMode.value = mode
        }
    }

    fun setEmergencyStop(active: Boolean) {
        viewModelScope.launch {
            prefs.setEmergencyStopActive(active)
            _isEmergencyStopActive.value = active
        }
    }

    fun saveApiKey(key: String) {
        prefs.setApiKey(key)
        _hasApiKey.value = prefs.hasApiKey()
    }

    fun toggleAiOptIn(optIn: Boolean) {
        viewModelScope.launch {
            prefs.setAiOptIn(optIn)
            _isAiOptedIn.value = optIn
        }
    }

    fun selectGeminiModel(model: String) {
        viewModelScope.launch {
            prefs.setGeminiModel(model)
            _geminiModel.value = model
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setOnboardingCompleted(true)
            _isOnboardingCompleted.value = true
        }
    }

    // Intent Openers
    fun openNotificationListenerSettings() = context.startActivity(detector.getNotificationListenerIntent())
    fun openAccessibilitySettings() = context.startActivity(detector.getAccessibilitySettingsIntent())
    fun openDndSettings() = context.startActivity(detector.getDndPolicySettingsIntent())
    fun openBatteryOptimizationSettings() = context.startActivity(detector.getBatteryOptimizationIntent())
    fun openExactAlarmSettings() = context.startActivity(detector.getExactAlarmIntent())
    fun openSamsungRoutines() {
        samsungAssistant.openSamsungRoutinesApp()?.let {
            context.startActivity(it)
        }
    }

    private fun AutomationRule.toEntity(): AutomationRuleEntity {
        return AutomationRuleEntity(
            id = if (this.id > 1000) this.id else 0,
            title = this.title,
            description = this.description,
            isEnabled = this.isEnabled,
            priority = this.priority,
            triggerType = this.triggerType,
            scheduleTrigger = this.scheduleTrigger,
            chargingTrigger = this.chargingTrigger,
            appTriggerPackage = this.appTriggerPackage,
            actions = this.actions,
            conflictGroup = this.conflictGroup
        )
    }

    private fun AutomationRuleEntity.toDomain(): AutomationRule {
        return AutomationRule(
            id = this.id,
            title = this.title,
            description = this.description,
            isEnabled = this.isEnabled,
            priority = this.priority,
            triggerType = this.triggerType,
            scheduleTrigger = this.scheduleTrigger,
            chargingTrigger = this.chargingTrigger,
            appTriggerPackage = this.appTriggerPackage,
            actions = this.actions,
            conflictGroup = this.conflictGroup,
            rollbackSupported = this.rollbackSupported,
            lastExecutedAt = this.lastExecutedAt,
            nextRunAt = this.nextRunAt,
            executionCount = this.executionCount
        )
    }
}
