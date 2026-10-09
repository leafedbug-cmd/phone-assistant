package com.buggedlab.pocketops.engine.capability

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.core.model.DevicePermissions

class DeviceCapabilityDetector(private val context: Context) {

    fun detectCapabilities(): DeviceCapabilities {
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Unknown"
        val osVersion = Build.VERSION.RELEASE ?: "Unknown"
        val sdkInt = Build.VERSION.SDK_INT
        val isSamsung = manufacturer.contains("samsung", ignoreCase = true)

        val oneUiVersion = detectOneUiVersion()
        val isOneUi = isSamsung || !oneUiVersion.isNullOrBlank()

        return DeviceCapabilities(
            manufacturer = manufacturer,
            model = model,
            osVersion = osVersion,
            sdkInt = sdkInt,
            isSamsung = isSamsung,
            oneUiVersion = oneUiVersion,
            isOneUiDetected = isOneUi,
            permissions = detectPermissions()
        )
    }

    fun detectPermissions(): DevicePermissions {
        val hasNotificationListener = isNotificationListenerEnabled()
        val hasAccessibility = isAccessibilityServiceEnabled()
        val hasDnd = isDndPolicyAccessGranted()
        val hasPostNotifications = isPostNotificationsGranted()
        val isBatteryOptIgnored = isBatteryOptimizationIgnored()
        val canExactAlarms = canScheduleExactAlarms()

        return DevicePermissions(
            hasNotificationListenerAccess = hasNotificationListener,
            hasAccessibilityAccess = hasAccessibility,
            hasDndPolicyAccess = hasDnd,
            hasPostNotificationsPermission = hasPostNotifications,
            isBatteryOptimizationIgnored = isBatteryOptIgnored,
            canScheduleExactAlarms = canExactAlarms
        )
    }

    fun isNotificationListenerEnabled(): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                if (name.contains(pkgName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(pkgName)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceName = "${context.packageName}/com.buggedlab.pocketops.service.accessibility.PocketOpsAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains(context.packageName)
    }

    fun isDndPolicyAccessGranted(): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.isNotificationPolicyAccessGranted == true
    }

    fun isPostNotificationsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return pm?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            am?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }

    private fun detectOneUiVersion(): String? {
        // Try reading One UI version from Samsung system properties
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java)

            // One UI version code property (e.g. 60100 for One UI 6.1, 70000 for 7.0)
            val oneUiCode = getMethod.invoke(null, "ro.build.version.oneui") as? String
            if (!oneUiCode.isNullOrBlank()) {
                val code = oneUiCode.toIntOrNull()
                if (code != null && code > 0) {
                    val major = code / 10000
                    val minor = (code % 10000) / 100
                    return "One UI $major.$minor"
                }
                return "One UI $oneUiCode"
            }

            // Fallback to SEP (Samsung Experience Platform) version
            val sepVersion = getMethod.invoke(null, "ro.build.version.sep") as? String
            if (!sepVersion.isNullOrBlank()) {
                return "SEP $sepVersion"
            }
        } catch (_: Exception) {
            // Reflection fallback if restricted
        }

        if (Build.MANUFACTURER.contains("samsung", ignoreCase = true)) {
            return "One UI (Supported)"
        }
        return null
    }

    // Settings Navigation Intents
    fun getNotificationListenerIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getDndPolicySettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getBatteryOptimizationIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getExactAlarmIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }

    fun getAppDetailsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getSamsungModesAndRoutinesIntent(): Intent? {
        val intent = Intent().apply {
            setClassName(
                "com.samsung.android.app.routines",
                "com.samsung.android.app.routines.ui.RoutineActivity"
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return if (context.packageManager.resolveActivity(intent, 0) != null) {
            intent
        } else {
            // Fallback to standard Zen Mode settings
            Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
    }
}
