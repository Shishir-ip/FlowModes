package com.example.ui.screens.logs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.ExecutionLog
import com.example.ui.components.FlowEmptyState
import com.example.ui.components.FlowGlassCard
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowRose
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationLogsScreen(
    logs: List<ExecutionLog>,
    onClearLogs: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glassColors.textPrimary
                    )
                }
                Text(
                    text = "History & Logs",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
                if (logs.isNotEmpty()) {
                    IconButton(onClick = onClearLogs) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs",
                            tint = FlowRose
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }
    ) { innerPadding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                FlowEmptyState(
                    title = "No history yet",
                    description = "When routines trigger, execution outcomes will appear here.",
                    buttonText = "Back",
                    onButtonClick = onNavigateBack
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "header") {
                    Text(
                        text = "${logs.size} executions recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(
                    logs,
                    key = { if (it.id != 0L) it.id else "${it.timestamp}_${it.routineId}_${it.hashCode()}" },
                    contentType = { "log_item" }
                ) { log ->
                    LogTimelineItem(log = log, timeFormat = timeFormat, dateFormat = dateFormat)
                }
            }
        }
    }
}

@Composable
private fun LogTimelineItem(
    log: ExecutionLog,
    timeFormat: SimpleDateFormat,
    dateFormat: SimpleDateFormat
) {
    val glassColors = LocalFlowGlassColors.current
    var isExpanded by remember { mutableStateOf(false) }

    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }
    val timeStr = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    FlowGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            // Compact timeline row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Text(
                    text = timeStr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = glassColors.textMuted,
                    modifier = Modifier.width(44.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status Icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (log.isSuccess) StatusSuccess.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (log.isSuccess) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (log.isSuccess) StatusSuccess else StatusError,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Status
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.routineName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = glassColors.textPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = if (log.isSuccess) "✓ Completed" else "✕ ${log.errorMessage ?: "Failed"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = if (log.isSuccess) StatusSuccess else StatusError,
                        maxLines = 1
                    )
                }

                Text(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = glassColors.textMuted
                )
            }

            // Expanded details on tap only
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 52.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(glassColors.glassBorder)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trigger: ${log.triggerName}",
                        fontSize = 12.sp,
                        color = glassColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Conditions: ${log.conditionsSummary}",
                        fontSize = 12.sp,
                        color = glassColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Actions: ${log.actionsSummary}",
                        fontSize = 12.sp,
                        color = glassColors.textSecondary
                    )

                    if (!log.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Error: ${log.errorMessage}",
                            fontSize = 12.sp,
                            color = StatusError
                        )
                    }
                }
            }
        }
    }
}
