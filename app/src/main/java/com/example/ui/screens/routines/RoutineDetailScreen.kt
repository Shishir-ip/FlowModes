package com.example.ui.screens.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Routine
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowPrimaryButton
import com.example.ui.components.FlowPriorityBadge
import com.example.ui.components.FlowSwitch
import com.example.ui.components.IconHelper
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowRose
import com.example.ui.theme.LocalFlowGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoutineDetailScreen(
    routine: Routine?,
    onNavigateBack: () -> Unit,
    onEditRoutine: () -> Unit,
    onDeleteRoutine: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onRunNow: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current

    if (routine == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Routine not found", color = glassColors.textSecondary)
        }
        return
    }

    val accentColor = try {
        Color(android.graphics.Color.parseColor(routine.colorHex))
    } catch (_: Exception) {
        FlowCyan
    }

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
                Row {
                    IconButton(onClick = onEditRoutine) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = FlowCyan
                        )
                    }
                    IconButton(onClick = onDeleteRoutine) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = FlowRose
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                FlowPrimaryButton(
                    text = "Test Run Routine",
                    onClick = onRunNow,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item(key = "header_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(routine.iconName),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = glassColors.textPrimary
                            )
                            if (routine.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = routine.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FlowPriorityBadge(priority = routine.priority)
                                Text(
                                    text = "· Cooldown ${routine.cooldownSeconds}s",
                                    fontSize = 11.sp,
                                    color = glassColors.textMuted
                                )
                            }
                        }

                        FlowSwitch(
                            checked = routine.isEnabled,
                            onCheckedChange = onToggleEnabled
                        )
                    }
                }
            }

            // Stats summary row
            item(key = "stats_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Times Triggered",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${routine.triggerCount}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FlowCyan
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Last Executed",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val lastTime = routine.lastTriggeredTime
                            Text(
                                text = if (lastTime != null) SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(lastTime)) else "Never",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glassColors.textPrimary
                            )
                        }
                    }
                }
            }

            // WHEN Conditions in single clean card
            item(key = "when_section") {
                Column {
                    Text(
                        text = "WHEN (${routine.conditionLogic.name})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )

                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (routine.conditions.isEmpty()) {
                                Text(
                                    text = "No conditions (Runs manually or on all events)",
                                    fontSize = 13.sp,
                                    color = glassColors.textMuted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            } else {
                                routine.conditions.forEachIndexed { index, cond ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FlowCyan.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(cond.type.iconName),
                                                contentDescription = null,
                                                tint = FlowCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cond.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = glassColors.textPrimary
                                            )
                                            Text(
                                                text = cond.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = glassColors.textSecondary
                                            )
                                        }
                                    }

                                    if (index < routine.conditions.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
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

            // THEN Actions in single clean card
            item(key = "then_section") {
                Column {
                    Text(
                        text = "THEN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )

                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (routine.actions.isEmpty()) {
                                Text(
                                    text = "No actions configured",
                                    fontSize = 13.sp,
                                    color = glassColors.textMuted,
                                    modifier = Modifier.padding(14.dp)
                                )
                            } else {
                                routine.actions.forEachIndexed { index, act ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FlowCyan.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(act.type.iconName),
                                                contentDescription = null,
                                                tint = FlowCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = act.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = glassColors.textPrimary
                                            )
                                            Text(
                                                text = act.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = glassColors.textSecondary
                                            )
                                        }
                                    }

                                    if (index < routine.actions.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp)
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
}
