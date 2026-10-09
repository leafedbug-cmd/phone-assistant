package com.buggedlab.pocketops.core.model

data class DeviceCapabilities(
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val sdkInt: Int,
    val isSamsung: Boolean,
    val oneUiVersion: String?,
    val isOneUiDetected: Boolean,
    val permissions: DevicePermissions
)

data class DevicePermissions(
    val hasNotificationListenerAccess: Boolean,
    val hasAccessibilityAccess: Boolean,
    val hasDndPolicyAccess: Boolean,
    val hasPostNotificationsPermission: Boolean,
    val isBatteryOptimizationIgnored: Boolean,
    val canScheduleExactAlarms: Boolean
) {
    val totalRequired: Int = 4 // Core permissions: Notification Listener, Accessibility, DND, Post Notifications
    val grantedCount: Int
        get() {
            var count = 0
            if (hasNotificationListenerAccess) count++
            if (hasAccessibilityAccess) count++
            if (hasDndPolicyAccess) count++
            if (hasPostNotificationsPermission) count++
            return count
        }

    val isFullyConfigured: Boolean
        get() = hasNotificationListenerAccess && hasAccessibilityAccess && hasDndPolicyAccess
}
