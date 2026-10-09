package com.buggedlab.pocketops.core.model

import java.time.DayOfWeek

enum class NotificationCategory(val displayName: String) {
    CRITICAL("Critical / Security"),
    PERSONAL("Personal"),
    WORK("Work"),
    MESSAGES("Messages"),
    SHOPPING("Shopping & Offers"),
    SOCIAL("Social Media"),
    ENTERTAINMENT("Entertainment"),
    SYSTEM("System & OS"),
    UNKNOWN("Other")
}

data class NotificationRecord(
    val id: Long = 0,
    val notificationKey: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val category: NotificationCategory,
    val postTimeMillis: Long,
    val isOngoing: Boolean = false,
    val isClearable: Boolean = true,
    val channelId: String? = null,
    val isProtected: Boolean = false,
    val isSuppressedByRoutine: Boolean = false
)

data class NotificationSummary(
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val category: NotificationCategory,
    val postTimeMillis: Long
)

data class AppNotificationStat(
    val packageName: String,
    val appName: String,
    val count: Int,
    val dominantCategory: NotificationCategory,
    val isProtected: Boolean = false
)

data class HourlyNotificationStat(
    val hourOfDay: Int,
    val count: Int
)

data class NotificationDashboardData(
    val todayTotal: Int,
    val weekTotal: Int,
    val noisiestApps: List<AppNotificationStat>,
    val categoryCounts: Map<NotificationCategory, Int>,
    val hourlyCounts: List<HourlyNotificationStat>,
    val suppressedCount: Int
)
