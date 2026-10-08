package com.example.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.engine.ActionExecutor
import com.example.automation.services.NotificationEventHub
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionType
import com.example.domain.models.InterceptedNotification
import com.example.domain.models.NotificationField
import com.example.domain.models.NotificationMatchOperator
import com.example.domain.models.Routine
import com.example.permissions.PermissionManager
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.FlowRose
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDebuggerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onCreateRoutineFromNotification: (Routine) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current
    val hasListenerPermission = PermissionManager.checkNotificationListenerAccess(context)

    val notifications by NotificationEventHub.recentNotifications.collectAsState()

    var testKeyword by remember { mutableStateOf("") }
    var selectedOperator by remember { mutableStateOf(NotificationMatchOperator.CONTAINS) }
    var selectedField by remember { mutableStateOf(NotificationField.ANY) }

    var testNotificationTitle by remember { mutableStateOf("Urgent Security Notice") }
    var testNotificationMessage by remember { mutableStateOf("System server response timeout alert #408") }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notification Debugger",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = glassColors.textPrimary
                    )
                    Text(
                        text = "Live diagnostic hub for automation matching",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                }

                IconButton(onClick = { NotificationEventHub.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear History",
                        tint = glassColors.textSecondary
                    )
                }
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
            // Permission Banner if missing
            if (!hasListenerPermission) {
                item {
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = StatusWarning.copy(alpha = 0.5f),
                        accentGlowColor = StatusWarning,
                        onClick = onNavigateToPermissions
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusWarning)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notification Access Disabled",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = StatusWarning
                                )
                                Text(
                                    text = "Tap to grant Notification Listener permission so FlowModes can intercept real incoming alerts.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Live Match Tester Card
            item {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    borderColor = FlowCyan.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = FlowCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Match Tester",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glassColors.textPrimary
                            )
                        }
                        Text(
                            text = "Test keyword & regex rules against intercepted notifications below",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = testKeyword,
                            onValueChange = { testKeyword = it },
                            placeholder = { Text("Filter / Match rule (e.g. Urgent, OTP, Alert)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowCyan,
                                unfocusedBorderColor = glassColors.glassBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NotificationMatchOperator.entries.take(4).forEach { op ->
                                val isSelected = op == selectedOperator
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) FlowCyan else glassColors.glassSurface)
                                        .clickable { selectedOperator = op }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = op.label.take(8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else glassColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Test Notification Generator
            item {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    borderColor = FlowIndigo.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BugReport, contentDescription = null, tint = FlowIndigo, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emit Test Notification",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glassColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = testNotificationTitle,
                            onValueChange = { testNotificationTitle = it },
                            label = { Text("Notification Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowIndigo,
                                unfocusedBorderColor = glassColors.glassBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = testNotificationMessage,
                            onValueChange = { testNotificationMessage = it },
                            label = { Text("Notification Body Text") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowIndigo,
                                unfocusedBorderColor = glassColors.glassBorder,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowGradientButton(
                            text = "Send Test Notification",
                            onClick = {
                                val action = AutomationAction(
                                    id = UUID.randomUUID().toString(),
                                    routineId = "test",
                                    type = ActionType.SHOW_NOTIFICATION,
                                    title = testNotificationTitle,
                                    summary = testNotificationMessage,
                                    configJson = JSONObject().apply {
                                        put("title", testNotificationTitle)
                                        put("message", testNotificationMessage)
                                    }.toString()
                                )
                                ActionExecutor.executeAction(context, action)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.PlayArrow
                        )
                    }
                }
            }

            // Stream of Intercepted Notifications
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intercepted Stream (${notifications.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glassColors.textPrimary
                    )
                }
            }

            if (notifications.isEmpty()) {
                item {
                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = glassColors.textSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No notifications intercepted yet",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Send a test notification above or receive any system alert to see it live here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(notifications) { item ->
                    val isMatch = remember(testKeyword, selectedOperator, selectedField, item) {
                        if (testKeyword.isBlank()) null
                        else {
                            val textToTest = when (selectedField) {
                                NotificationField.TITLE -> item.title
                                NotificationField.TEXT -> item.text
                                NotificationField.SUBTEXT -> item.subText
                                NotificationField.ANY -> "${item.title} ${item.text} ${item.subText}"
                            }
                            try {
                                when (selectedOperator) {
                                    NotificationMatchOperator.CONTAINS -> textToTest.contains(testKeyword, ignoreCase = true)
                                    NotificationMatchOperator.EQUALS -> textToTest.equals(testKeyword, ignoreCase = true)
                                    NotificationMatchOperator.STARTS_WITH -> textToTest.startsWith(testKeyword, ignoreCase = true)
                                    NotificationMatchOperator.ENDS_WITH -> textToTest.endsWith(testKeyword, ignoreCase = true)
                                    NotificationMatchOperator.DOES_NOT_CONTAIN -> !textToTest.contains(testKeyword, ignoreCase = true)
                                    NotificationMatchOperator.REGEX -> Regex(testKeyword, RegexOption.IGNORE_CASE).containsMatchIn(textToTest)
                                }
                            } catch (_: Exception) {
                                false
                            }
                        }
                    }

                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = when (isMatch) {
                            true -> StatusSuccess.copy(alpha = 0.8f)
                            false -> StatusError.copy(alpha = 0.3f)
                            null -> glassColors.glassBorder
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.appName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FlowCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glassColors.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                if (isMatch != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isMatch) StatusSuccess.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isMatch) "MATCH" else "NO MATCH",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMatch) StatusSuccess else StatusError
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            if (item.text.isNotEmpty()) {
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Text(
                                text = item.packageName,
                                style = MaterialTheme.typography.labelSmall,
                                color = glassColors.textSecondary.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Routine Creator Button from this notification
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(glassColors.glassSurface)
                                        .border(1.dp, FlowCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable {
                                            val routine = buildRoutineFromNotification(item)
                                            onCreateRoutineFromNotification(routine)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = FlowCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Create Routine from this",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FlowCyan
                                        )
                                    }
                                }
                            }
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

private fun buildRoutineFromNotification(item: InterceptedNotification): Routine {
    val routineId = UUID.randomUUID().toString()
    val keyword = if (item.title.isNotBlank()) item.title else item.text.take(20)

    val configJson = JSONObject().apply {
        put("packageName", item.packageName)
        put("appName", item.appName)
        put("keyword", keyword)
        put("operator", "CONTAINS")
        put("field", "ANY")
        put("caseSensitive", false)
    }.toString()

    return Routine(
        id = routineId,
        name = "When ${item.appName}: $keyword",
        description = "Triggered when ${item.appName} posts an alert matching \"$keyword\"",
        iconName = "NotificationsActive",
        colorHex = "#F43F5E",
        isEnabled = true,
        isFavorite = false,
        conditions = listOf(
            AutomationCondition(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                type = ConditionType.NOTIFICATION_RECEIVED,
                title = "Alert from ${item.appName}",
                summary = "Contains \"$keyword\"",
                configJson = configJson
            )
        ),
        actions = listOf(
            AutomationAction(
                id = UUID.randomUUID().toString(),
                routineId = routineId,
                type = ActionType.TRIGGER_VIBRATION,
                title = "Haptic Vibration Pulse",
                summary = "Notify with vibration",
                configJson = "{}"
            )
        ),
        createdAt = System.currentTimeMillis()
    )
}
