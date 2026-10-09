package com.buggedlab.pocketops.engine.notification

import com.buggedlab.pocketops.core.model.AppNotificationStat
import com.buggedlab.pocketops.core.model.HourlyNotificationStat
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.NotificationDashboardData
import com.buggedlab.pocketops.data.db.NotificationDao
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class NotificationAnalyticsEngine(
    private val notificationDao: NotificationDao,
    private val zoneId: ZoneId = ZoneId.of("America/Chicago")
) {

    suspend fun getDashboardData(): NotificationDashboardData {
        val now = ZonedDateTime.now(zoneId)
        val startOfToday = now.truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli()
        val sevenDaysAgo = now.minusDays(7).toInstant().toEpochMilli()

        val todayCount = notificationDao.getCountSince(startOfToday)
        val weekCount = notificationDao.getCountSince(sevenDaysAgo)
        val suppressedCount = notificationDao.getSuppressedCountSince(startOfToday)

        val topAppsQuery = notificationDao.getTopAppsSince(sevenDaysAgo, limit = 8)
        val categoryQuery = notificationDao.getCategoryCountsSince(sevenDaysAgo)

        val categoryCounts = NotificationCategory.values().associateWith { 0 }.toMutableMap()
        for (item in categoryQuery) {
            val cat = runCatching { NotificationCategory.valueOf(item.category) }.getOrDefault(NotificationCategory.UNKNOWN)
            categoryCounts[cat] = item.count
        }

        val noisiestApps = topAppsQuery.map { queryResult ->
            AppNotificationStat(
                packageName = queryResult.packageName,
                appName = queryResult.appName,
                count = queryResult.count,
                dominantCategory = NotificationCategory.UNKNOWN
            )
        }

        // Calculate 24-hour distribution for today
        val todayNotifications = notificationDao.getNotificationsSince(startOfToday)
        val hourlyMap = (0..23).associateWith { 0 }.toMutableMap()

        for (notif in todayNotifications) {
            val notifTime = Instant.ofEpochMilli(notif.postTimeMillis).atZone(zoneId)
            val hour = notifTime.hour
            hourlyMap[hour] = (hourlyMap[hour] ?: 0) + 1
        }

        val hourlyList = (0..23).map { hour ->
            HourlyNotificationStat(
                hourOfDay = hour,
                count = hourlyMap[hour] ?: 0
            )
        }

        return NotificationDashboardData(
            todayTotal = todayCount,
            weekTotal = weekCount,
            noisiestApps = noisiestApps,
            categoryCounts = categoryCounts,
            hourlyCounts = hourlyList,
            suppressedCount = suppressedCount
        )
    }
}
