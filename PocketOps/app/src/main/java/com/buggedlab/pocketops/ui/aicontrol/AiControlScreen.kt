package com.buggedlab.pocketops.ui.aicontrol

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.core.model.ChatMessage
import com.buggedlab.pocketops.core.model.PlanStatus
import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RiskLevel
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
fun AiControlScreen(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onApprovePlan: (AiPlan) -> Unit,
    onDryRunPlan: (AiPlan) -> Unit,
    onRejectPlan: (AiPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat History List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    onApprovePlan = onApprovePlan,
                    onDryRunPlan = onDryRunPlan,
                    onRejectPlan = onRejectPlan
                )
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = OneUiBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PocketOps AI is formulating plan...",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Field Bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = OneUiDarkSurfaceCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Command PocketOps (text or voice)...",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        // Quick prompt for voice simulation
                        inputText = "Open my notification settings"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = OneUiTeal
                    )
                }

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OneUiBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onApprovePlan: (AiPlan) -> Unit,
    onDryRunPlan: (AiPlan) -> Unit,
    onRejectPlan: (AiPlan) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        if (message.isUser) {
            Surface(
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                color = OneUiBlue,
                modifier = Modifier.padding(start = 48.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = OneUiBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                        color = OneUiDarkSurfaceCard
                    ) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }

                    // Render attached AI Plan Card if present
                    message.attachedPlan?.let { plan ->
                        Spacer(modifier = Modifier.height(8.dp))
                        PlanProposalCard(
                            plan = plan,
                            onApprove = { onApprovePlan(plan) },
                            onDryRun = { onDryRunPlan(plan) },
                            onReject = { onRejectPlan(plan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlanProposalCard(
    plan: AiPlan,
    onApprove: () -> Unit,
    onDryRun: () -> Unit,
    onReject: () -> Unit
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = OneUiTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Proposed Plan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (plan.status) {
                        PlanStatus.COMPLETED -> RiskLowGreen.copy(alpha = 0.2f)
                        PlanStatus.APPROVED, PlanStatus.EXECUTING -> OneUiBlue.copy(alpha = 0.2f)
                        PlanStatus.REJECTED, PlanStatus.CANCELLED, PlanStatus.FAILED -> RiskHighRed.copy(alpha = 0.2f)
                        PlanStatus.PENDING_APPROVAL -> RiskMediumOrange.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = plan.status.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (plan.status) {
                                PlanStatus.COMPLETED -> RiskLowGreen
                                PlanStatus.APPROVED, PlanStatus.EXECUTING -> OneUiBlue
                                PlanStatus.REJECTED, PlanStatus.CANCELLED, PlanStatus.FAILED -> RiskHighRed
                                PlanStatus.PENDING_APPROVAL -> RiskMediumOrange
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = plan.explanation,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Proposed Actions (${plan.actions.size}):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))
            plan.actions.forEach { action ->
                ActionRowItem(action = action)
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (plan.status == PlanStatus.PENDING_APPROVAL) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = OneUiBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Approve", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDryRun,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Simulate", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RiskHighRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reject", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionRowItem(action: ProposedAction) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = OneUiDarkSurfaceCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (action.riskLevel) {
                        RiskLevel.LOW -> RiskLowGreen.copy(alpha = 0.2f)
                        RiskLevel.MEDIUM -> RiskMediumOrange.copy(alpha = 0.2f)
                        RiskLevel.HIGH -> RiskHighRed.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = action.riskLevel.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (action.riskLevel) {
                                RiskLevel.LOW -> RiskLowGreen
                                RiskLevel.MEDIUM -> RiskMediumOrange
                                RiskLevel.HIGH -> RiskHighRed
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )

            if (action.isExecuted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${action.executionResult ?: "Executed"}",
                    style = MaterialTheme.typography.labelSmall.copy(color = OneUiTeal)
                )
            }
        }
    }
}
