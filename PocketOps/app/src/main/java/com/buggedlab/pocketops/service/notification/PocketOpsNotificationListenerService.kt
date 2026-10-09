package com.buggedlab.pocketops.service.notification

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.data.db.AppConfigEntity
import com.buggedlab.pocketops.data.db.NotificationEntity
import com.buggedlab.pocketops.data.db.PocketOpsDatabase
import com.buggedlab.pocketops.data.preferences.PocketOpsPreferences
import com.buggedlab.pocketops.engine.notification.NotificationCategorizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PocketOpsNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val categorizer = NotificationCategorizer()

    companion object {
        private const val TAG = "PocketOpsNotifService"

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _latestNotification = MutableStateFlow<NotificationEntity?>(null)
        val latestNotification: StateFlow<NotificationEntity?> = _latestNotification.asStateFlow()

        @Volatile
        var instance: PocketOpsNotificationListenerService? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isConnected.value = true
        Log.i(TAG, "PocketOps NotificationListenerService connected successfully.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isConnected.value = false
        Log.w(TAG, "PocketOps NotificationListenerService disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        serviceScope.launch {
            try {
                handleNotificationPosted(sbn)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification from ${sbn.packageName}", e)
            }
        }
    }

    private suspend fun handleNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        // Skip our own app's notifications to prevent recursive listening loops
        if (packageName == applicationContext.packageName) return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()
        val systemCategory = sbn.notification.category
        val channelId = sbn.notification.channelId
        val postTime = sbn.postTime.takeIf { it > 0 } ?: System.currentTimeMillis()

        // Resolve user-friendly application name
        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }

        val db = PocketOpsDatabase.getInstance(applicationContext)
        val appConfig = db.appConfigDao().getByPackage(packageName)

        // Determine category: respect explicit user override if configured, otherwise heuristic
        val category = appConfig?.categoryOverride ?: categorizer.categorize(
            packageName = packageName,
            title = title,
            text = text,
            subText = subText,
            systemCategory = systemCategory,
            channelId = channelId
        )

        val isProtected = appConfig?.isProtected == true || categorizer.isProtectedNotification(
            packageName = packageName,
            category = category,
            title = title,
            text = text
        )

        // Check if an active routine suppresses this category
        val prefs = PocketOpsPreferences(applicationContext)
        val activeMode = prefs.activeMode.first()
        var isSuppressed = false

        if (!isProtected && activeMode == "Work") {
            // In Work mode, suppress Shopping and Social
            if (category == NotificationCategory.SHOPPING || category == NotificationCategory.SOCIAL) {
                isSuppressed = true
            }
        }

        val entity = NotificationEntity(
            notificationKey = sbn.key,
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            category = category,
            postTimeMillis = postTime,
            isOngoing = sbn.isOngoing,
            isClearable = sbn.isClearable,
            channelId = channelId,
            isProtected = isProtected,
            isSuppressedByRoutine = isSuppressed
        )

        db.notificationDao().insert(entity)
        _latestNotification.value = entity

        // Auto-register app in AppConfig if not already present
        if (appConfig == null) {
            db.appConfigDao().upsert(
                AppConfigEntity(
                    packageName = packageName,
                    appName = appName,
                    isProtected = isProtected,
                    isMutedInWorkMode = false,
                    isDenylistedFromAiControl = false,
                    categoryOverride = null
                )
            )
        }
    }
}
