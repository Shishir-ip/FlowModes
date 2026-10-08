package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.ExecutionLog
import com.example.domain.models.Mode
import com.example.domain.models.Routine
import com.example.ui.components.FlowEmptyState
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowRowItem
import com.example.ui.components.FlowSwitch
import com.example.ui.components.IconHelper
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.motion.flowPress
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    routines: List<Routine>,
    modes: List<Mode>,
    logs: List<ExecutionLog>,
    onNavigateToCreateRoutine: () -> Unit,
    onNavigateToRoutineDetail: (String) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onToggleRoutine: (Routine) -> Unit,
    onRunRoutine: (Routine) -> Unit,
    onToggleMode: (Mode) -> Unit,
    onNavigateToModeDetail: (String) -> Unit = {},
    onNavigateToTemplates: () -> Unit = {},
    onNavigateToNotificationDebugger: () -> Unit = {}
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val enabledCount = remember(routines) { routines.count { it.isEnabled } }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val recentRoutines = remember(routines) { routines.take(5) }
    val recentLogs = remember(logs) { logs.take(3) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top App Header (Minimal, calm, clear)
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FlowModes",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$enabledCount active ${if (enabledCount == 1) "automation" else "automations"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassColors.textSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Templates Access
                    Box(
                        modifier = Modifier
                            .flowPress(level = FlowMotion.Level.SUBTLE)
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassColors.glassSurface)
                            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                FlowHaptics.tick(hapticFeedback, view)
                                onNavigateToTemplates()
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FlowCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Templates",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = glassColors.textPrimary
                            )
                        }
                    }

                    // Plus Add Routine button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .flowPress(level = FlowMotion.Level.MEDIUM)
                            .clip(CircleShape)
                            .background(FlowCyan)
                            .clickable {
                                FlowHaptics.tick(hapticFeedback, view)
                                onNavigateToCreateRoutine()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Routine",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 2. Active Modes (Simple horizontal list)
        if (modes.isNotEmpty()) {
            item(key = "modes_section") {
                Column {
                    Text(
                        text = "Modes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = glassColors.textPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(modes, key = { it.id }) { mode ->
                            val accentColor = try {
                                Color(android.graphics.Color.parseColor(mode.colorHex))
                            } catch (_: Exception) {
                                FlowCyan
                            }

                            val isSelected = mode.isActive
                            val itemBg = if (isSelected) accentColor.copy(alpha = 0.16f) else glassColors.glassSurface
                            val itemBorder = if (isSelected) accentColor.copy(alpha = 0.6f) else glassColors.glassBorder

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(itemBg)
                                    .border(1.dp, itemBorder, RoundedCornerShape(14.dp))
                                    .clickable {
                                        FlowHaptics.impact(hapticFeedback, view)
                                        onNavigateToModeDetail(mode.id)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(mode.iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) accentColor else glassColors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Text(
                                        text = mode.name,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) glassColors.textPrimary else glassColors.textSecondary
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) StatusSuccess.copy(alpha = 0.25f) else glassColors.glassBorder.copy(alpha = 0.4f))
                                            .clickable {
                                                FlowHaptics.modeToggle(hapticFeedback, view, !isSelected)
                                                onToggleMode(mode)
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) StatusSuccess else glassColors.textMuted)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Routines List (Clean compact rows in a single container)
        item(key = "routines_section") {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Routines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = glassColors.textPrimary
                    )

                    Text(
                        text = "${routines.size} total",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                }

                if (routines.isEmpty()) {
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToCreateRoutine
                    ) {
                        FlowEmptyState(
                            title = "No routines yet",
                            description = "Create your first routine to automate daily tasks",
                            buttonText = "Create Routine",
                            onButtonClick = onNavigateToCreateRoutine
                        )
                    }
                } else {
                    // Single clean card container wrapping list rows
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column {
                            recentRoutines.forEachIndexed { index, routine ->
                                val accentColor = remember(routine.colorHex) {
                                    try {
                                        Color(android.graphics.Color.parseColor(routine.colorHex))
                                    } catch (_: Exception) {
                                        FlowCyan
                                    }
                                }

                                val triggerSummary = remember(routine.conditions) {
                                    routine.conditions.firstOrNull()?.summary
                                        ?: if (routine.conditions.isEmpty()) "Manual" else "${routine.conditions.size} triggers"
                                }

                                FlowRowItem(
                                    title = routine.name,
                                    subtitle = triggerSummary,
                                    icon = IconHelper.getIconByName(routine.iconName),
                                    iconTint = accentColor,
                                    onClick = { onNavigateToRoutineDetail(routine.id) },
                                    trailing = {
                                        FlowSwitch(
                                            checked = routine.isEnabled,
                                            onCheckedChange = { onToggleRoutine(routine) }
                                        )
                                    }
                                )

                                if (index < recentRoutines.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(0.5.dp)
                                            .background(glassColors.glassBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Recent Activity (Only show the latest 3 items)
        if (recentLogs.isNotEmpty()) {
            item(key = "activity_section") {
                Column {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = glassColors.textPrimary,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column {
                            recentLogs.forEachIndexed { index, log ->
                                val timeStr = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (log.isSuccess) StatusSuccess else StatusError,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.routineName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (log.isSuccess) "Completed" else (log.errorMessage ?: "Failed"),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = if (log.isSuccess) glassColors.textSecondary else StatusError,
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = glassColors.textMuted
                                    )
                                }

                                if (index < recentLogs.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .height(0.5.dp)
                                            .background(glassColors.glassBorder)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
