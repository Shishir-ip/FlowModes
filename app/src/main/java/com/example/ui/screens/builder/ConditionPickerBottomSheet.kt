package com.example.ui.screens.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionType
import com.example.ui.components.FlowRowItem
import com.example.ui.components.IconHelper
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionPickerBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onConditionSelected: (AutomationCondition) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    var searchQuery by remember { mutableStateOf("") }

    val allConditions = remember { ConditionType.entries }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allConditions
        else allConditions.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glassColors.glassSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Condition",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = glassColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search triggers (e.g. WiFi, Battery, Time)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = FlowCyan
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlowCyan,
                    unfocusedBorderColor = glassColors.glassBorder,
                    focusedContainerColor = glassColors.glassSurface,
                    unfocusedContainerColor = glassColors.glassSurface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.name }) { item ->
                    FlowRowItem(
                        title = item.displayName,
                        subtitle = item.category,
                        icon = IconHelper.getIconByName(item.iconName),
                        iconTint = FlowCyan,
                        onClick = {
                            val newCondition = createDefaultCondition(item)
                            onConditionSelected(newCondition)
                        }
                    )
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

private fun createDefaultCondition(type: ConditionType): AutomationCondition {
    val id = UUID.randomUUID().toString()
    return when (type) {
        ConditionType.TIME_SPECIFIC -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "08:00 AM",
            configJson = """{"hour":8,"minute":0}"""
        )
        ConditionType.TIME_RANGE -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "22:00 – 07:00",
            configJson = """{"startHour":22,"startMinute":0,"endHour":7,"endMinute":0}"""
        )
        ConditionType.DAYS_OF_WEEK -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Mon, Tue, Wed, Thu, Fri",
            configJson = """{"days":[2,3,4,5,6]}"""
        )
        ConditionType.BATTERY_LEVEL_BELOW -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Battery < 20%",
            configJson = """{"threshold":20}"""
        )
        ConditionType.BATTERY_LEVEL_ABOVE -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Battery > 80%",
            configJson = """{"threshold":80}"""
        )
        ConditionType.CHARGING_STARTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Charging started",
            configJson = """{"isCharging":true}"""
        )
        ConditionType.CHARGING_STOPPED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Charging stopped",
            configJson = """{"isCharging":false}"""
        )
        ConditionType.WIFI_CONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Wi-Fi Connected (Any)",
            configJson = """{"anyNetwork":true}"""
        )
        ConditionType.WIFI_DISCONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Wi-Fi Disconnected",
            configJson = """{}"""
        )
        ConditionType.BLUETOOTH_CONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Bluetooth Connected (Any)",
            configJson = """{"anyDevice":true}"""
        )
        ConditionType.BLUETOOTH_DISCONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Bluetooth Disconnected",
            configJson = """{}"""
        )
        ConditionType.SCREEN_ON -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Screen turned on",
            configJson = """{}"""
        )
        ConditionType.SCREEN_OFF -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Screen turned off",
            configJson = """{}"""
        )
        ConditionType.HEADSET_CONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Headphones connected",
            configJson = """{"isPlugged":true}"""
        )
        ConditionType.HEADSET_DISCONNECTED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Headphones disconnected",
            configJson = """{"isPlugged":false}"""
        )
        ConditionType.DEVICE_BOOT -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Device restarted",
            configJson = """{}"""
        )
        ConditionType.APP_OPENED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "App Opened",
            configJson = """{"packageName":"","appName":""}"""
        )
        ConditionType.NOTIFICATION_RECEIVED -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Notification Received",
            configJson = """{"packageName":"","keyword":""}"""
        )
        ConditionType.LOCATION_GEOFENCE -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Location Geofence",
            configJson = """{"name":"Home","latitude":0.0,"longitude":0.0,"radiusMeters":150.0}"""
        )
        ConditionType.MANUAL_TRIGGER -> AutomationCondition(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Manual trigger only",
            configJson = """{}"""
        )
    }
}
