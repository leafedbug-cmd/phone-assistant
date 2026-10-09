package com.buggedlab.pocketops.ui.home

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.core.model.NotificationDashboardData
import com.buggedlab.pocketops.theme.BorderSubtle
import com.buggedlab.pocketops.theme.OneUiBlue
import com.buggedlab.pocketops.theme.OneUiDarkSurfaceCard
import com.buggedlab.pocketops.theme.OneUiTeal
import com.buggedlab.pocketops.theme.RiskHighRed
import com.buggedlab.pocketops.theme.RiskLowGreen
import com.buggedlab.pocketops.theme.TextMuted
import com.buggedlab.pocketops.theme.TextPrimary
import com.buggedlab.pocketops.theme.TextSecondary

@Composable
fun HomeScreen(
    capabilities: DeviceCapabilities,
    activeMode: String,
    dashboardData: NotificationDashboardData,
    onModeChange: (String) -> Unit,
    onSubmitAiPrompt: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var promptText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device Status Card (Samsung One UI & Android 16 detection)
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
                    Column {
                        Text(
                            text = if (capabilities.isSamsung) "Samsung Galaxy S24 Ultra" else "${capabilities.manufacturer} ${capabilities.model}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${capabilities.oneUiVersion ?: "Android"} • OS ${capabilities.osVersion} (API ${capabilities.sdkInt})",
                            style = MaterialTheme.typography.bodySmall.copy(color = OneUiTeal)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (capabilities.permissions.isFullyConfigured) RiskLowGreen.copy(alpha = 0.2f) else RiskHighRed.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onNavigateToSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (capabilities.permissions.isFullyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (capabilities.permissions.isFullyConfigured) RiskLowGreen else RiskHighRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${capabilities.permissions.grantedCount}/${capabilities.permissions.totalRequired} Permissions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (capabilities.permissions.isFullyConfigured) RiskLowGreen else RiskHighRed
                                )
                            )
                        }
                    }
                }
            }
        }

        // AI Command Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = OneUiBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Command",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = {
                        Text(
                            text = "Ask PocketOps (e.g., 'Organize my notifications at work')...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OneUiBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (promptText.isNotBlank()) {
                                    onSubmitAiPrompt(promptText)
                                    promptText = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = OneUiBlue
                            )
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick suggestions:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val samplePrompts = listOf(
                        "Organize notifications",
                        "Show noisy apps",
                        "Open notification settings"
                    )
                    samplePrompts.forEach { sample ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                onSubmitAiPrompt(sample)
                            }
                        ) {
                            Text(
                                text = sample,
                                style = MaterialTheme.typography.labelSmall.copy(color = OneUiTeal),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Focus Mode Selector
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Active Profile",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("Standard", "Work", "Focus", "Sleep")
                    modes.forEach { mode ->
                        val isSelected = mode == activeMode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) OneUiBlue else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onModeChange(mode) }
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification Intelligence Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToNotifications() }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = OneUiBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notification Intelligence",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Details",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${dashboardData.todayTotal}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OneUiBlue
                            )
                        )
                        Text(
                            text = "Received Today",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }

                    Column {
                        Text(
                            text = "${dashboardData.suppressedCount}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = OneUiTeal
                            )
                        )
                        Text(
                            text = "Filtered by Routine",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }

                    Column {
                        Text(
                            text = "${dashboardData.weekTotal}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Last 7 Days",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                if (dashboardData.noisiestApps.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Most active: ${dashboardData.noisiestApps.first().appName} (${dashboardData.noisiestApps.first().count} alerts)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        }

        // Quick Routine Simulator Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToRoutines() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Automation Routines",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Manage deterministic weekday focus & DND schedules",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Routines",
                    tint = OneUiBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
