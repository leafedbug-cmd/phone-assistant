package com.buggedlab.pocketops.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buggedlab.pocketops.core.model.AppNotificationStat
import com.buggedlab.pocketops.core.model.HourlyNotificationStat
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.NotificationDashboardData
import com.buggedlab.pocketops.engine.notification.NotificationDigest
import com.buggedlab.pocketops.theme.OneUiBlue
import com.buggedlab.pocketops.theme.OneUiDarkSurfaceCard
import com.buggedlab.pocketops.theme.OneUiTeal
import com.buggedlab.pocketops.theme.RiskHighRed
import com.buggedlab.pocketops.theme.RiskLowGreen
import com.buggedlab.pocketops.theme.TextMuted
import com.buggedlab.pocketops.theme.TextPrimary
import com.buggedlab.pocketops.theme.TextSecondary

@Composable
fun NotificationsScreen(
    dashboardData: NotificationDashboardData,
    currentDigest: NotificationDigest?,
    isGeneratingDigest: Boolean,
    onGenerateDigest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDigestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Today",
                count = "${dashboardData.todayTotal}",
                color = OneUiBlue,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "7-Day Total",
                count = "${dashboardData.weekTotal}",
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Suppressed",
                count = "${dashboardData.suppressedCount}",
                color = OneUiTeal,
                modifier = Modifier.weight(1f)
            )
        }

        // Notification Digest Banner & Actions
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ListAlt,
                            contentDescription = null,
                            tint = OneUiTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Notification Digest",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Generates a structured overview of what happened while you were focused, grouping urgent alerts, messages, and routine updates.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onGenerateDigest()
                        showDigestDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGeneratingDigest) "Compiling Digest..." else "Generate Digest Now",
                        fontWeight = FontWeight.Bold
                    )
                }

                // If digest is loaded, show preview card
                currentDigest?.let { digest ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = digest.headline,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = digest.timestampFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            digest.sections.forEach { section ->
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OneUiTeal
                                    )
                                )
                                section.items.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
        }

        // 24-Hour Hourly Activity Bar Chart
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = OneUiBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Today's Hourly Distribution",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HourlyBarChart(hourlyStats = dashboardData.hourlyCounts)
            }
        }

        // Noisiest Applications
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Most Active Applications (7 Days)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (dashboardData.noisiestApps.isEmpty()) {
                    Text(
                        text = "No notification traffic recorded yet. Connect to Notification Listener in Settings.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                } else {
                    dashboardData.noisiestApps.forEach { appStat ->
                        AppStatRow(stat = appStat)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Category Breakdown
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Breakdown by Category",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                dashboardData.categoryCounts.forEach { (cat, count) ->
                    CategoryRow(category = cat, count = count)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
fun HourlyBarChart(hourlyStats: List<HourlyNotificationStat>) {
    val maxCount = hourlyStats.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            hourlyStats.forEach { stat ->
                val fraction = stat.count.toFloat() / maxCount.toFloat()
                val barHeight = (fraction * 70).coerceAtLeast(4f).dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (stat.count > 0) OneUiBlue else MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "12 AM", fontSize = 9.sp, color = TextMuted)
            Text(text = "6 AM", fontSize = 9.sp, color = TextMuted)
            Text(text = "12 PM", fontSize = 9.sp, color = TextMuted)
            Text(text = "6 PM", fontSize = 9.sp, color = TextMuted)
            Text(text = "11 PM", fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
fun AppStatRow(stat: AppNotificationStat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (stat.isProtected) RiskLowGreen else OneUiBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = stat.appName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = stat.packageName,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = "${stat.count} alerts",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CategoryRow(category: NotificationCategory, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (count > 0) OneUiBlue else TextMuted
            )
        )
    }
}
