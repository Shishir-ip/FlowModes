package com.example.ui.screens.developer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.engine.ActionExecutor
import com.example.automation.engine.ConflictManager
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.ConditionType
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.components.IconHelper
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors
import androidx.compose.ui.platform.LocalContext

@Composable
fun DeveloperScreen(
    onTriggerCondition: (ConditionType) -> Unit,
    onNavigateBack: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = glassColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Developer Sandbox",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Simulate hardware sensor states and system triggers without needing physical external events.",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )
            }

            // Simulate Triggers
            item {
                Text(
                    text = "Simulate System Triggers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
            }

            val testTriggers = listOf(
                Pair(ConditionType.CHARGING_STARTED, "Charger Plugged In"),
                Pair(ConditionType.CHARGING_STOPPED, "Charger Unplugged"),
                Pair(ConditionType.BATTERY_LEVEL_BELOW, "Battery Low (Below 20%)"),
                Pair(ConditionType.HEADSET_CONNECTED, "Headphones Connected"),
                Pair(ConditionType.SCREEN_ON, "Screen Turned On"),
                Pair(ConditionType.SCREEN_OFF, "Screen Turned Off"),
                Pair(ConditionType.WIFI_CONNECTED, "Wi-Fi Connected"),
                Pair(ConditionType.DEVICE_BOOT, "Boot / Restart Event")
            )

            testTriggers.forEach { (type, label) ->
                item {
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { onTriggerCondition(type) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(type.iconName),
                                contentDescription = null,
                                tint = FlowCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Fire",
                                tint = FlowCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Direct Action Tests
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Test Action Execution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
            }

            item {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        val testNotification = AutomationAction(
                            id = "test_n",
                            routineId = "dev",
                            type = ActionType.SHOW_NOTIFICATION,
                            title = "Test Notification",
                            summary = "FlowModes Sandbox Test",
                            configJson = "{\"title\":\"Sandbox Alert\",\"message\":\"Developer trigger test succeeded!\"}"
                        )
                        ActionExecutor.executeAction(context, testNotification)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(IconHelper.getIconByName("Notifications"), contentDescription = null, tint = FlowIndigo)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Send Test Notification",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FlowIndigo)
                    }
                }
            }

            item {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        val testVibe = AutomationAction(
                            id = "test_v",
                            routineId = "dev",
                            type = ActionType.TRIGGER_VIBRATION,
                            title = "Vibrate",
                            summary = "Pulse",
                            configJson = "{}"
                        )
                        ActionExecutor.executeAction(context, testVibe)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(IconHelper.getIconByName("Vibration"), contentDescription = null, tint = FlowIndigo)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Trigger Haptic Vibration",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = FlowIndigo)
                    }
                }
            }

            item {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        ConflictManager.resetState()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, tint = FlowCyan)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reset Conflict & Cooldown Cache",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Clears loop protection tracking and routine cooldown timers",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
