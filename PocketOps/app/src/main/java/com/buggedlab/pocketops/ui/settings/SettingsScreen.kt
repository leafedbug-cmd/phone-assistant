package com.buggedlab.pocketops.ui.settings

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buggedlab.pocketops.core.model.DeviceCapabilities
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
fun SettingsScreen(
    capabilities: DeviceCapabilities,
    isAiOptedIn: Boolean,
    geminiModel: String,
    hasApiKey: Boolean,
    isEmergencyStopActive: Boolean,
    onSaveApiKey: (String) -> Unit,
    onToggleAiOptIn: (Boolean) -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleEmergencyStop: (Boolean) -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenDndSettings: () -> Unit,
    onOpenBatteryOptSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenSamsungRoutines: () -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKeySavedBanner by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = OneUiBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "System & Permissions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Configure device permissions, security boundaries, and AI integrations",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Permissions Dashboard
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Permission Dashboard",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Android platform permissions required for real on-device management.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                PermissionRow(
                    title = "Notification Listener Access",
                    description = "Required to capture alerts, track frequency, and generate digests",
                    isGranted = capabilities.permissions.hasNotificationListenerAccess,
                    onOpenSettings = onOpenNotificationListenerSettings
                )

                Spacer(modifier = Modifier.height(10.dp))

                PermissionRow(
                    title = "Accessibility Service",
                    description = "Required for supervised UI inspection and interactive screen assistance",
                    isGranted = capabilities.permissions.hasAccessibilityAccess,
                    onOpenSettings = onOpenAccessibilitySettings
                )

                Spacer(modifier = Modifier.height(10.dp))

                PermissionRow(
                    title = "Do Not Disturb (Zen Policy)",
                    description = "Required to adjust priority interruption filters in focus routines",
                    isGranted = capabilities.permissions.hasDndPolicyAccess,
                    onOpenSettings = onOpenDndSettings
                )

                Spacer(modifier = Modifier.height(10.dp))

                PermissionRow(
                    title = "Battery Optimization Exemption",
                    description = "Ensures background routines execute reliably without OS suppression",
                    isGranted = capabilities.permissions.isBatteryOptimizationIgnored,
                    onOpenSettings = onOpenBatteryOptSettings
                )

                Spacer(modifier = Modifier.height(10.dp))

                PermissionRow(
                    title = "Exact Alarm Scheduling",
                    description = "Enables precise time-triggered routine activation",
                    isGranted = capabilities.permissions.canScheduleExactAlarms,
                    onOpenSettings = onOpenExactAlarmSettings
                )
            }
        }

        // AI Provider Configuration (Keystore-backed)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OneUiTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Provider & Privacy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cloud AI is 100% optional. PocketOps includes a deterministic offline NLP engine that runs completely on-device without an API key.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Cloud Opt-in Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Cloud AI (Google Gemini)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Sends structured task prompts to Gemini API with strict consent",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Switch(
                        checked = isAiOptedIn,
                        onCheckedChange = onToggleAiOptIn,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OneUiBlue
                        )
                    )
                }

                if (isAiOptedIn) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Gemini API Key (Stored in Android Keystore)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = {
                            Text(
                                text = if (hasApiKey) "•••••••••••••••• (Key saved in Keystore)" else "Enter Gemini API Key...",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OneUiBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                onSaveApiKey(apiKeyInput)
                                apiKeyInput = ""
                                showApiKeySavedBanner = true
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Save Key to Keystore", fontWeight = FontWeight.Bold)
                    }

                    if (showApiKeySavedBanner) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✓ Key securely saved into hardware-backed Android Keystore.",
                            style = MaterialTheme.typography.labelSmall.copy(color = RiskLowGreen)
                        )
                    }
                }
            }
        }

        // Samsung One UI Integration Diagnostics
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = OneUiBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Samsung One UI Integration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Device: ${capabilities.manufacturer} ${capabilities.model} • ${capabilities.oneUiVersion ?: "One UI Supported"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = OneUiTeal)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenSamsungRoutines,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Launch Samsung Modes & Routines", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Emergency Stop Master Controls
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
                            imageVector = Icons.Default.Emergency,
                            contentDescription = null,
                            tint = RiskHighRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Emergency Stop",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RiskHighRed
                                )
                            )
                            Text(
                                text = "Instantly aborts all background routines and supervised navigation",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Switch(
                        checked = isEmergencyStopActive,
                        onCheckedChange = onToggleEmergencyStop,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = RiskHighRed
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) RiskLowGreen else RiskHighRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp),
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        if (!isGranted) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OneUiBlue,
                modifier = Modifier.clickable { onOpenSettings() }
            ) {
                Text(
                    text = "Grant",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RiskLowGreen.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Granted",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = RiskLowGreen
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
