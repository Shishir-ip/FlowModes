package com.example.ui.screens.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionType
import com.example.domain.models.NotificationField
import com.example.domain.models.NotificationMatchOperator
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.components.FlowSlider
import com.example.ui.components.IconHelper
import com.example.ui.components.pickers.AppPickerSheet
import com.example.ui.components.pickers.BluetoothPickerSheet
import com.example.ui.components.pickers.WifiPickerSheet
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusWarning
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConditionConfigSheet(
    condition: AutomationCondition,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveCondition: (AutomationCondition) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val parsedConfig = remember(condition) {
        try { JSONObject(condition.configJson.ifEmpty { "{}" }) } catch (_: Exception) { JSONObject() }
    }

    var isNegated by remember { mutableStateOf(condition.isNegated) }

    // Sub-picker states
    var showAppPicker by remember { mutableStateOf(false) }
    var showWifiPicker by remember { mutableStateOf(false) }
    var showBluetoothPicker by remember { mutableStateOf(false) }

    // Notification fields
    var notifPackage by remember { mutableStateOf(parsedConfig.optString("packageName", "")) }
    var notifAppName by remember { mutableStateOf(parsedConfig.optString("appName", if (notifPackage.isEmpty()) "Any Application" else notifPackage)) }
    var notifField by remember {
        mutableStateOf(try {
            NotificationField.valueOf(parsedConfig.optString("field", "ANY"))
        } catch (_: Exception) { NotificationField.ANY })
    }
    var notifOperator by remember {
        mutableStateOf(try {
            NotificationMatchOperator.valueOf(parsedConfig.optString("operator", "CONTAINS"))
        } catch (_: Exception) { NotificationMatchOperator.CONTAINS })
    }
    var notifQuery by remember { mutableStateOf(parsedConfig.optString("query", parsedConfig.optString("keyword", ""))) }

    // Wi-Fi fields
    var wifiSsid by remember { mutableStateOf(parsedConfig.optString("ssid", "")) }

    // Bluetooth fields
    var bluetoothDeviceName by remember { mutableStateOf(parsedConfig.optString("deviceName", "")) }

    // App opened fields
    var openedPackage by remember { mutableStateOf(parsedConfig.optString("packageName", "")) }
    var openedAppName by remember { mutableStateOf(parsedConfig.optString("appName", if (openedPackage.isEmpty()) "Select Application" else openedPackage)) }

    // Battery fields
    var batteryThreshold by remember {
        mutableFloatStateOf(parsedConfig.optInt("threshold", if (condition.type == ConditionType.BATTERY_LEVEL_BELOW) 20 else 80).toFloat())
    }

    // Time Range fields
    var startHour by remember { mutableIntStateOf(parsedConfig.optInt("startHour", 22)) }
    var startMinute by remember { mutableIntStateOf(parsedConfig.optInt("startMinute", 0)) }
    var endHour by remember { mutableIntStateOf(parsedConfig.optInt("endHour", 7)) }
    var endMinute by remember { mutableIntStateOf(parsedConfig.optInt("endMinute", 0)) }

    // Geofence fields
    var geofenceEvent by remember { mutableStateOf(parsedConfig.optString("event", "ARRIVING")) }
    var geofencePlace by remember { mutableStateOf(parsedConfig.optString("placeName", "Home")) }

    // Calendar Event fields
    var calendarKeyword by remember { mutableStateOf(parsedConfig.optString("keyword", "")) }
    var calendarRequireBusy by remember { mutableStateOf(parsedConfig.optBoolean("requireBusy", true)) }

    // NFC Tag fields
    var nfcTagId by remember { mutableStateOf(parsedConfig.optString("tagId", "")) }
    var nfcTagLabel by remember { mutableStateOf(parsedConfig.optString("label", "")) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glassColors.glassSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FlowCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(condition.type.iconName),
                            contentDescription = null,
                            tint = FlowCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = condition.type.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = condition.type.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glassColors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Negation Card (NOT condition)
            FlowGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invert Condition (NOT)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "Triggers only when this condition is FALSE (e.g. NOT connected to Home Wi-Fi)",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isNegated,
                        onCheckedChange = { isNegated = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FlowCyan,
                            checkedTrackColor = FlowCyan.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Config Form per Condition Type
            when (condition.type) {
                ConditionType.NOTIFICATION_RECEIVED -> {
                    Text(
                        text = "Source Application",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showAppPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = FlowCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notifAppName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = if (notifPackage.isEmpty()) "Any App" else notifPackage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = FlowCyan, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Match Field",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(NotificationField.entries) { field ->
                            FilterChip(
                                selected = notifField == field,
                                onClick = { notifField = field },
                                label = { Text(field.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Matching Operator",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(NotificationMatchOperator.entries) { op ->
                            FilterChip(
                                selected = notifOperator == op,
                                onClick = { notifOperator = op },
                                label = { Text(op.label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowIndigo
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Keyword / Pattern",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notifQuery,
                        onValueChange = { notifQuery = it },
                        placeholder = { Text("e.g. Urgent, OTP, Emergency, Boss") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                ConditionType.WIFI_CONNECTED, ConditionType.WIFI_DISCONNECTED -> {
                    Text(
                        text = "Wi-Fi Network SSID",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showWifiPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = FlowCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (wifiSsid.isEmpty()) "Any Wi-Fi Network" else wifiSsid,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = if (wifiSsid.isEmpty()) "Triggers on all connections" else "Specific network filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = FlowCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                ConditionType.BLUETOOTH_CONNECTED, ConditionType.BLUETOOTH_DISCONNECTED -> {
                    Text(
                        text = "Bluetooth Device",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showBluetoothPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, tint = FlowCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (bluetoothDeviceName.isEmpty()) "Any Bluetooth Device" else bluetoothDeviceName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = if (bluetoothDeviceName.isEmpty()) "Triggers on all Bluetooth connections" else "Specific device filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = FlowCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                ConditionType.BATTERY_LEVEL_BELOW, ConditionType.BATTERY_LEVEL_ABOVE -> {
                    val label = if (condition.type == ConditionType.BATTERY_LEVEL_BELOW) "Below or Equal to" else "Above or Equal to"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery Threshold ($label)",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "${batteryThreshold.toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FlowCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowSlider(
                        value = batteryThreshold,
                        onValueChange = { batteryThreshold = it },
                        valueRange = 5f..100f,
                        steps = 18
                    )
                }

                ConditionType.APP_OPENED -> {
                    Text(
                        text = "Target Application",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        onClick = { showAppPicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null, tint = FlowCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = openedAppName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = if (openedPackage.isEmpty()) "Any App" else openedPackage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = FlowCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                ConditionType.TIME_RANGE -> {
                    Text(
                        text = "Time Window",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FlowGlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Start Time", style = MaterialTheme.typography.bodySmall, color = glassColors.textSecondary)
                                Text("%02d:%02d".format(startHour, startMinute), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FlowCyan)
                            }
                        }
                        FlowGlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("End Time", style = MaterialTheme.typography.bodySmall, color = glassColors.textSecondary)
                                Text("%02d:%02d".format(endHour, endMinute), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FlowIndigo)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Presets:",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val timePresets = listOf(
                            Triple("Sleep (22:00 - 07:00)", Pair(22, 0), Pair(7, 0)),
                            Triple("Work Hours (09:00 - 17:00)", Pair(9, 0), Pair(17, 0)),
                            Triple("Morning (06:00 - 09:00)", Pair(6, 0), Pair(9, 0)),
                            Triple("Evening (18:00 - 22:00)", Pair(18, 0), Pair(22, 0))
                        )
                        items(timePresets) { (label, start, end) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(glassColors.glassSurface)
                                    .clickable {
                                        startHour = start.first
                                        startMinute = start.second
                                        endHour = end.first
                                        endMinute = end.second
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp, color = glassColors.textPrimary)
                            }
                        }
                    }
                }

                ConditionType.LOCATION_GEOFENCE -> {
                    Text(
                        text = "Geofence Event",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = geofenceEvent == "ARRIVING",
                            onClick = { geofenceEvent = "ARRIVING" },
                            label = { Text("Arriving at Place") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                selectedLabelColor = FlowCyan
                            )
                        )
                        FilterChip(
                            selected = geofenceEvent == "LEAVING",
                            onClick = { geofenceEvent = "LEAVING" },
                            label = { Text("Leaving Place") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                selectedLabelColor = FlowIndigo
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Place Name",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = geofencePlace,
                        onValueChange = { geofencePlace = it },
                        placeholder = { Text("e.g. Home, Work, Gym") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                ConditionType.CALENDAR_EVENT -> {
                    Text(
                        text = "Calendar Event Match",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Match ongoing events by title keywords, or trigger on any busy calendar slot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = calendarKeyword,
                        onValueChange = { calendarKeyword = it },
                        placeholder = { Text("Event title keyword (e.g. Meeting, Workout, Focus)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require 'Busy' Status",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Only trigger if calendar event is marked as Busy (ignores Free slots)",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }
                        Switch(
                            checked = calendarRequireBusy,
                            onCheckedChange = { calendarRequireBusy = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FlowCyan)
                        )
                    }
                }

                ConditionType.NFC_TAG_SCANNED -> {
                    Text(
                        text = "Physical NFC Sticker ID / Label",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter NFC Tag identifier or custom name (e.g. desk_sticker, bedside, car_mount).",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nfcTagId,
                        onValueChange = { nfcTagId = it },
                        placeholder = { Text("Tag Identifier (e.g. desk_focus_01)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nfcTagLabel,
                        onValueChange = { nfcTagLabel = it },
                        placeholder = { Text("Optional display label (e.g. Office Desk)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                else -> {
                    Text(
                        text = "This trigger activates automatically based on hardware/system state.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glassColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            FlowGradientButton(
                text = "Apply Configuration",
                onClick = {
                    val newConfig = JSONObject()
                    var newSummary = condition.summary

                    when (condition.type) {
                        ConditionType.NOTIFICATION_RECEIVED -> {
                            newConfig.put("packageName", notifPackage)
                            newConfig.put("appName", notifAppName)
                            newConfig.put("field", notifField.name)
                            newConfig.put("operator", notifOperator.name)
                            newConfig.put("query", notifQuery)
                            newSummary = if (notifQuery.isNotEmpty()) {
                                "${notifAppName}: ${notifOperator.label} \"$notifQuery\""
                            } else {
                                "${notifAppName}: Any notification"
                            }
                        }
                        ConditionType.WIFI_CONNECTED -> {
                            newConfig.put("ssid", wifiSsid)
                            newSummary = if (wifiSsid.isEmpty()) "Any Wi-Fi network" else "Connected to \"$wifiSsid\""
                        }
                        ConditionType.WIFI_DISCONNECTED -> {
                            newConfig.put("ssid", wifiSsid)
                            newSummary = if (wifiSsid.isEmpty()) "Disconnected from Wi-Fi" else "Disconnected from \"$wifiSsid\""
                        }
                        ConditionType.BLUETOOTH_CONNECTED -> {
                            newConfig.put("deviceName", bluetoothDeviceName)
                            newSummary = if (bluetoothDeviceName.isEmpty()) "Any Bluetooth device" else "Connected to \"$bluetoothDeviceName\""
                        }
                        ConditionType.BLUETOOTH_DISCONNECTED -> {
                            newConfig.put("deviceName", bluetoothDeviceName)
                            newSummary = if (bluetoothDeviceName.isEmpty()) "Bluetooth disconnected" else "Disconnected from \"$bluetoothDeviceName\""
                        }
                        ConditionType.BATTERY_LEVEL_BELOW -> {
                            newConfig.put("threshold", batteryThreshold.toInt())
                            newSummary = "Battery \u2264 ${batteryThreshold.toInt()}%"
                        }
                        ConditionType.BATTERY_LEVEL_ABOVE -> {
                            newConfig.put("threshold", batteryThreshold.toInt())
                            newSummary = "Battery \u2265 ${batteryThreshold.toInt()}%"
                        }
                        ConditionType.APP_OPENED -> {
                            newConfig.put("packageName", openedPackage)
                            newConfig.put("appName", openedAppName)
                            newSummary = "Opened $openedAppName"
                        }
                        ConditionType.TIME_RANGE -> {
                            newConfig.put("startHour", startHour)
                            newConfig.put("startMinute", startMinute)
                            newConfig.put("endHour", endHour)
                            newConfig.put("endMinute", endMinute)
                            newSummary = "%02d:%02d – %02d:%02d".format(startHour, startMinute, endHour, endMinute)
                        }
                        ConditionType.LOCATION_GEOFENCE -> {
                            newConfig.put("event", geofenceEvent)
                            newConfig.put("placeName", geofencePlace)
                            newSummary = "$geofenceEvent \"$geofencePlace\""
                        }
                        ConditionType.CALENDAR_EVENT -> {
                            newConfig.put("keyword", calendarKeyword)
                            newConfig.put("requireBusy", calendarRequireBusy)
                            newSummary = if (calendarKeyword.isNotBlank()) {
                                "Calendar event matches \"$calendarKeyword\"" + if (calendarRequireBusy) " (Busy)" else ""
                            } else {
                                if (calendarRequireBusy) "Any Busy Calendar Event" else "Any Active Calendar Event"
                            }
                        }
                        ConditionType.NFC_TAG_SCANNED -> {
                            newConfig.put("tagId", nfcTagId)
                            newConfig.put("label", nfcTagLabel)
                            newSummary = if (nfcTagLabel.isNotBlank()) {
                                "NFC Tag: \"$nfcTagLabel\""
                            } else if (nfcTagId.isNotBlank()) {
                                "NFC Tag: \"$nfcTagId\""
                            } else {
                                "Any FlowModes NFC Tag"
                            }
                        }
                        else -> Unit
                    }

                    val updated = condition.copy(
                        configJson = newConfig.toString(),
                        summary = newSummary,
                        isNegated = isNegated
                    )
                    onSaveCondition(updated)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Sub sheets
    if (showAppPicker) {
        val appSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AppPickerSheet(
            sheetState = appSheetState,
            onDismiss = { showAppPicker = false },
            onAppSelected = { pkg, name ->
                if (condition.type == ConditionType.NOTIFICATION_RECEIVED) {
                    notifPackage = pkg
                    notifAppName = name
                } else if (condition.type == ConditionType.APP_OPENED) {
                    openedPackage = pkg
                    openedAppName = name
                }
                showAppPicker = false
            }
        )
    }

    if (showWifiPicker) {
        val wifiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        WifiPickerSheet(
            initialSsid = wifiSsid,
            sheetState = wifiSheetState,
            onDismiss = { showWifiPicker = false },
            onWifiSelected = { ssid ->
                wifiSsid = ssid
                showWifiPicker = false
            }
        )
    }

    if (showBluetoothPicker) {
        val btSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        BluetoothPickerSheet(
            initialDeviceName = bluetoothDeviceName,
            sheetState = btSheetState,
            onDismiss = { showBluetoothPicker = false },
            onDeviceSelected = { deviceName ->
                bluetoothDeviceName = deviceName
                showBluetoothPicker = false
            }
        )
    }
}
