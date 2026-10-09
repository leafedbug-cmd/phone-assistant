package com.buggedlab.pocketops.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.data.db.AuditLogEntity
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class UiNodeSnapshot(
    val text: String?,
    val contentDescription: String?,
    val viewId: String?,
    val className: String?,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isPassword: Boolean,
    val bounds: String,
    val children: List<UiNodeSnapshot> = emptyList()
)

class PocketOpsAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        private const val TAG = "PocketOpsA11yService"

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _currentActivePackage = MutableStateFlow<String?>(null)
        val currentActivePackage: StateFlow<String?> = _currentActivePackage.asStateFlow()

        @Volatile
        var instance: PocketOpsAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isConnected.value = true
        Log.i(TAG, "PocketOps Supervised AccessibilityService connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString()
            if (!pkg.isNullOrBlank()) {
                _currentActivePackage.value = pkg
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "PocketOps AccessibilityService interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isConnected.value = false
    }

    // Structured UI Inspection
    fun captureCurrentScreenHierarchy(): UiNodeSnapshot? {
        val root = rootInActiveWindow ?: return null
        return buildNodeSnapshot(root)
    }

    private fun buildNodeSnapshot(node: AccessibilityNodeInfo): UiNodeSnapshot {
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val isPassword = node.isPassword
        val text = if (isPassword) "[REDACTED_PASSWORD]" else node.text?.toString()
        val desc = if (isPassword) null else node.contentDescription?.toString()

        val children = mutableListOf<UiNodeSnapshot>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                children.add(buildNodeSnapshot(child))
            }
        }

        return UiNodeSnapshot(
            text = text,
            contentDescription = desc,
            viewId = node.viewIdResourceName,
            className = node.className?.toString(),
            isClickable = node.isClickable,
            isEditable = node.isEditable,
            isPassword = isPassword,
            bounds = "${rect.left},${rect.top},${rect.right},${rect.bottom}",
            children = children
        )
    }

    // Supervised Action: Click Node
    suspend fun clickNodeByText(targetText: String, userAuthorized: Boolean): Boolean {
        if (!userAuthorized) {
            logAudit("Click '$targetText'", "BLOCKED", "User did not explicitly authorize this action.")
            return false
        }

        val prefs = PocketOpsPreferences(applicationContext)
        if (prefs.isEmergencyStopActive.first()) {
            logAudit("Click '$targetText'", "BLOCKED", "Emergency Stop active.")
            return false
        }

        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            if (node.isClickable) {
                val success = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                logAudit("Click '$targetText'", if (success) "SUCCESS" else "FAILED", "Target clicked via Accessibility.")
                return success
            } else {
                // Try clicking clickable parent
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val success = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        logAudit("Click parent of '$targetText'", if (success) "SUCCESS" else "FAILED", "Target parent clicked.")
                        return success
                    }
                    parent = parent.parent
                }
            }
        }

        logAudit("Click '$targetText'", "FAILED", "Target text not found or not clickable on active screen.")
        return false
    }

    // Supervised Action: Set Text
    suspend fun enterTextInActiveField(text: String, userAuthorized: Boolean): Boolean {
        if (!userAuthorized) {
            logAudit("Enter Text", "BLOCKED", "Text entry requires explicit user authorization.")
            return false
        }

        val prefs = PocketOpsPreferences(applicationContext)
        if (prefs.isEmergencyStopActive.first()) {
            logAudit("Enter Text", "BLOCKED", "Emergency Stop active.")
            return false
        }

        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false

        if (focused.isPassword) {
            logAudit("Enter Text", "BLOCKED", "Cannot enter text into password field.")
            return false
        }

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        val success = focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        logAudit("Enter Text: '$text'", if (success) "SUCCESS" else "FAILED", "Text action performed.")
        return success
    }

    // Global navigation actions
    fun navigateBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun navigateHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun openRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    private fun logAudit(action: String, outcome: String, details: String) {
        serviceScope.launch {
            PocketOpsDatabase.getInstance(applicationContext).auditLogDao().insert(
                AuditLogEntity(
                    actionName = action,
                    category = "AI_SUPERVISED_CONTROL",
                    riskLevel = RiskLevel.MEDIUM,
                    authorizedBy = "USER_SUPERVISED",
                    outcome = outcome,
                    details = details
                )
            )
        }
    }
}
