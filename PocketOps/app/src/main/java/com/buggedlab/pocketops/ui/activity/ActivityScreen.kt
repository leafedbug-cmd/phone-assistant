package com.buggedlab.pocketops.ui.activity

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buggedlab.pocketops.core.model.AuditLogEntry
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ActivityScreen(
    auditLogs: List<AuditLogEntry>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredLogs = auditLogs.filter { log ->
        val matchesQuery = searchQuery.isBlank() ||
                log.actionName.contains(searchQuery, ignoreCase = true) ||
                log.details.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "AUTOMATION" -> log.category.contains("AUTOMATION", ignoreCase = true)
            "AI_CONTROL" -> log.category.contains("AI", ignoreCase = true)
            "BLOCKED" -> log.outcome == "BLOCKED" || log.outcome == "FAILED"
            else -> true
        }

        matchesQuery && matchesFilter
    }

    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.of("America/Chicago"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = OneUiBlue,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Activity & Security Audit",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Immutable, searchable record of all automated and supervised actions",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search activity logs...", fontSize = 13.sp, color = TextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
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
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All", "AUTOMATION" to "Routines", "AI_CONTROL" to "AI Actions", "BLOCKED" to "Blocked").forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) OneUiBlue else OneUiDarkSurfaceCard,
                    modifier = Modifier.clickable { selectedFilter = key }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) TextPrimary else TextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching activity records found.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    AuditLogRow(log = log, timeFormatter = timeFormatter)
                }
            }
        }
    }
}

@Composable
fun AuditLogRow(log: AuditLogEntry, timeFormatter: DateTimeFormatter) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = OneUiDarkSurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.actionName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (log.outcome) {
                        "SUCCESS" -> RiskLowGreen.copy(alpha = 0.2f)
                        "AUTHORIZED" -> OneUiTeal.copy(alpha = 0.2f)
                        "ROLLED_BACK" -> OneUiBlue.copy(alpha = 0.2f)
                        else -> RiskHighRed.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = log.outcome,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (log.outcome) {
                                "SUCCESS" -> RiskLowGreen
                                "AUTHORIZED" -> OneUiTeal
                                "ROLLED_BACK" -> OneUiBlue
                                else -> RiskHighRed
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${log.category} • Auth: ${log.authorizedBy}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
                Text(
                    text = timeFormatter.format(Instant.ofEpochMilli(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                )
            }
        }
    }
}
