package com.buggedlab.pocketops.engine.notification

import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.data.db.NotificationDao
import com.buggedlab.pocketops.data.db.NotificationEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class DigestSection(
    val title: String,
    val items: List<String>
)

data class NotificationDigest(
    val headline: String,
    val timestampFormatted: String,
    val totalCount: Int,
    val sections: List<DigestSection>,
    val fullSummaryText: String
)

class DigestGenerator(
    private val notificationDao: NotificationDao,
    private val zoneId: ZoneId = ZoneId.of("America/Chicago")
) {
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(zoneId)

    suspend fun generateDigest(sinceMillis: Long): NotificationDigest {
        val notifications = notificationDao.getNotificationsSince(sinceMillis)
        val now = Instant.now().atZone(zoneId)
        val timestampFormatted = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a").format(now)

        if (notifications.isEmpty()) {
            return NotificationDigest(
                headline = "Notification Digest: All Quiet",
                timestampFormatted = timestampFormatted,
                totalCount = 0,
                sections = emptyList(),
                fullSummaryText = "No notifications were received during this period. Your phone remained peaceful."
            )
        }

        // Group by category
        val critical = notifications.filter { it.category == NotificationCategory.CRITICAL }
        val messages = notifications.filter { it.category == NotificationCategory.MESSAGES }
        val work = notifications.filter { it.category == NotificationCategory.WORK }
        val promos = notifications.filter { it.category == NotificationCategory.SHOPPING }
        val social = notifications.filter { it.category == NotificationCategory.SOCIAL }
        val others = notifications.filter {
            it.category !in setOf(
                NotificationCategory.CRITICAL,
                NotificationCategory.MESSAGES,
                NotificationCategory.WORK,
                NotificationCategory.SHOPPING,
                NotificationCategory.SOCIAL
            )
        }

        val sections = mutableListOf<DigestSection>()

        if (critical.isNotEmpty()) {
            sections.add(
                DigestSection(
                    title = "🚨 Important & Security (${critical.size})",
                    items = critical.take(5).map { formatItem(it) }
                )
            )
        }

        if (messages.isNotEmpty()) {
            // Group messages by app or sender
            val appGrouped = messages.groupBy { it.appName }
            val formatted = appGrouped.map { (app, msgs) ->
                val senders = msgs.map { it.title }.distinct().take(3).joinToString(", ")
                "$app: ${msgs.size} messages from $senders"
            }
            sections.add(
                DigestSection(
                    title = "💬 Messages (${messages.size})",
                    items = formatted
                )
            )
        }

        if (work.isNotEmpty()) {
            sections.add(
                DigestSection(
                    title = "💼 Work & Productivity (${work.size})",
                    items = work.take(5).map { formatItem(it) }
                )
            )
        }

        if (promos.isNotEmpty()) {
            val appCounts = promos.groupBy { it.appName }.map { "${it.key} (${it.value.size})" }.joinToString(", ")
            sections.add(
                DigestSection(
                    title = "🏷️ Shopping & Promotions (${promos.size})",
                    items = listOf("Received offers from: $appCounts")
                )
            )
        }

        if (social.isNotEmpty()) {
            val appCounts = social.groupBy { it.appName }.map { "${it.key} (${it.value.size})" }.joinToString(", ")
            sections.add(
                DigestSection(
                    title = "📱 Social Updates (${social.size})",
                    items = listOf("Activity from: $appCounts")
                )
            )
        }

        if (others.isNotEmpty()) {
            sections.add(
                DigestSection(
                    title = "⚙️ System & Other (${others.size})",
                    items = listOf("${others.size} background system notifications recorded.")
                )
            )
        }

        val sb = StringBuilder()
        sb.appendLine("Notification Digest (${notifications.size} total)")
        sb.appendLine("Generated at $timestampFormatted")
        sb.appendLine()
        for (section in sections) {
            sb.appendLine(section.title)
            for (item in section.items) {
                sb.appendLine("  • $item")
            }
            sb.appendLine()
        }

        return NotificationDigest(
            headline = "Notification Digest: ${notifications.size} updates summarized",
            timestampFormatted = timestampFormatted,
            totalCount = notifications.size,
            sections = sections,
            fullSummaryText = sb.toString().trim()
        )
    }

    private fun formatItem(notif: NotificationEntity): String {
        val time = timeFormatter.format(Instant.ofEpochMilli(notif.postTimeMillis))
        val bodySnippet = if (notif.text.isNotBlank()) " - ${notif.text.take(60)}" else ""
        return "[${notif.appName}] ${notif.title}$bodySnippet ($time)"
    }
}
