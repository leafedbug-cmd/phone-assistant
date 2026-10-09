package com.buggedlab.pocketops.ui.routines

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buggedlab.pocketops.core.model.ActionType
import com.buggedlab.pocketops.core.model.AutomationAction
import com.buggedlab.pocketops.core.model.AutomationRule
import com.buggedlab.pocketops.core.model.DndFilterType
import com.buggedlab.pocketops.core.model.NotificationCategory
import com.buggedlab.pocketops.core.model.SimulationResult
import com.buggedlab.pocketops.core.model.TimeScheduleTrigger
import com.buggedlab.pocketops.core.model.TriggerType
import com.buggedlab.pocketops.theme.BorderSubtle
import com.buggedlab.pocketops.theme.OneUiBlue
import com.buggedlab.pocketops.theme.OneUiDarkSurfaceCard
import com.buggedlab.pocketops.theme.OneUiTeal
import com.buggedlab.pocketops.theme.RiskHighRed
import com.buggedlab.pocketops.theme.RiskLowGreen
import com.buggedlab.pocketops.theme.RiskMediumOrange
import com.buggedlab.pocketops.theme.TextMuted
import com.buggedlab.pocketops.theme.TextPrimary
import com.buggedlab.pocketops.theme.TextSecondary

@Composable
fun RoutinesScreen(
    rules: List<AutomationRule>,
    simulationResult: SimulationResult?,
    onToggleRule: (AutomationRule, Boolean) -> Unit,
    onSimulateRule: (AutomationRule) -> Unit,
    onExecuteRule: (AutomationRule) -> Unit,
    onCreateRule: (AutomationRule) -> Unit,
    onDismissSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header Row with "+ New Routine" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Autonomous Routines",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Deterministic, user-approved automation rules",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "New", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Simulation Assessment Result Card (Dry-Run Simulator)
        simulationResult?.let { sim ->
            SimulationResultCard(
                result = sim,
                onDismiss = onDismissSimulation
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (showCreateDialog) {
            CreateRoutineDialog(
                onSave = { newRule ->
                    onCreateRule(newRule)
                    showCreateDialog = false
                },
                onDismiss = { showCreateDialog = false }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // List of Active/Configured Routines
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                RoutineCardItem(
                    rule = rule,
                    onToggle = { enabled -> onToggleRule(rule, enabled) },
                    onSimulate = { onSimulateRule(rule) },
                    onExecute = { onExecuteRule(rule) }
                )
            }
        }
    }
}

@Composable
fun RoutineCardItem(
    rule: AutomationRule,
    onToggle: (Boolean) -> Unit,
    onSimulate: () -> Unit,
    onExecute: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rule.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = rule.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OneUiBlue,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Trigger details
            rule.scheduleTrigger?.let { sched ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = OneUiTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Schedule: Mon-Fri, %02d:%02d - %02d:%02d".format(
                                sched.startHour,
                                sched.startMinute,
                                sched.endHour,
                                sched.endMinute
                            ),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.sp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation and Run Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSimulate,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Simulate", fontSize = 12.sp)
                }

                Button(
                    onClick = onExecute,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Run Now", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SimulationResultCard(
    result: SimulationResult,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dry-Run Simulator: ${result.ruleTitle}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (result.canExecute) RiskLowGreen.copy(alpha = 0.2f) else RiskHighRed.copy(alpha = 0.2f)
            ) {
                Text(
                    text = result.safetyAssessment,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (result.canExecute) RiskLowGreen else RiskHighRed
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Projected Actions:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = OneUiTeal)
            )
            result.projectedActions.forEach { action ->
                Text(
                    text = "• $action",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                )
            }

            if (result.missingPermissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Missing Permissions: ${result.missingPermissions.joinToString()}",
                    style = MaterialTheme.typography.labelSmall.copy(color = RiskHighRed)
                )
            }
        }
    }
}

@Composable
fun CreateRoutineDialog(
    onSave: (AutomationRule) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Workday Focus (8 AM - 5 PM)") }
    var description by remember { mutableStateOf("Automatically silence social media and set Priority DND") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Create Automation Routine",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Routine Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Cancel")
                }

                Button(
                    onClick = {
                        val newRule = AutomationRule(
                            id = System.currentTimeMillis(),
                            title = title,
                            description = description,
                            isEnabled = true,
                            priority = 2,
                            triggerType = TriggerType.TIME_SCHEDULE,
                            scheduleTrigger = TimeScheduleTrigger(
                                startHour = 8,
                                startMinute = 0,
                                endHour = 17,
                                endMinute = 0,
                                daysOfWeek = listOf(1, 2, 3, 4, 5)
                            ),
                            actions = listOf(
                                AutomationAction(
                                    type = ActionType.SET_DND_FILTER,
                                    dndFilter = DndFilterType.PRIORITY_ONLY
                                ),
                                AutomationAction(
                                    type = ActionType.SUPPRESS_NOTIFICATION_CATEGORIES,
                                    suppressedCategories = listOf(NotificationCategory.SOCIAL, NotificationCategory.SHOPPING)
                                ),
                                AutomationAction(
                                    type = ActionType.ACTIVATE_MODE,
                                    modeName = "Work"
                                )
                            ),
                            conflictGroup = "WORK_HOURS"
                        )
                        onSave(newRule)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "Save Routine")
                }
            }
        }
    }
}
