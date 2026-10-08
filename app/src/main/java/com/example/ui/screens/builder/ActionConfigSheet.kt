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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
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
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.components.FlowSlider
import com.example.ui.components.FlowSwitch
import com.example.ui.components.IconHelper
import com.example.ui.components.pickers.AppPickerSheet
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionConfigSheet(
    action: AutomationAction,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSaveAction: (AutomationAction) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val parsedConfig = remember(action) {
        try { JSONObject(action.configJson.ifEmpty { "{}" }) } catch (_: Exception) { JSONObject() }
    }

    var restoreOnExit by remember { mutableStateOf(action.restoreOnExit) }
    var showAppPicker by remember { mutableStateOf(false) }

    // Volume states
    var volumeStream by remember { mutableStateOf(parsedConfig.optString("stream", "MEDIA")) }
    var volumePercent by remember { mutableFloatStateOf(parsedConfig.optInt("volume", 50).toFloat()) }

    // Ringer mode
    var ringerMode by remember { mutableStateOf(parsedConfig.optString("mode", "NORMAL")) }

    // Brightness
    var brightnessPercent by remember { mutableFloatStateOf(parsedConfig.optInt("brightness", 50).toFloat()) }
    var autoBrightness by remember { mutableStateOf(parsedConfig.optBoolean("autoBrightness", false)) }

    // DND mode
    var dndMode by remember { mutableStateOf(parsedConfig.optString("mode", "PRIORITY")) }

    // Notification
    var notifTitle by remember { mutableStateOf(parsedConfig.optString("title", "FlowModes Alert")) }
    var notifMessage by remember { mutableStateOf(parsedConfig.optString("message", "Routine triggered")) }
    var notifPriority by remember { mutableStateOf(parsedConfig.optString("priority", "DEFAULT")) }

    // App launch
    var appPackage by remember { mutableStateOf(parsedConfig.optString("packageName", "com.google.android.youtube")) }
    var appLabel by remember { mutableStateOf(parsedConfig.optString("appLabel", if (appPackage.isEmpty()) "Select App" else appPackage)) }

    // Web URL
    var webUrl by remember { mutableStateOf(parsedConfig.optString("url", "https://maps.google.com")) }

    // Flashlight
    var flashlightOn by remember { mutableStateOf(parsedConfig.optBoolean("enable", true)) }

    // Vibration pattern
    var vibrationPattern by remember { mutableStateOf(parsedConfig.optString("pattern", "DOUBLE_PULSE")) }

    // Settings target
    var settingsTarget by remember { mutableStateOf(parsedConfig.optString("target", "WIFI")) }

    // Accessibility nav
    var accessibilityAction by remember { mutableStateOf(parsedConfig.optString("action", "HOME")) }

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
                            .background(FlowIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIconByName(action.type.iconName),
                            contentDescription = null,
                            tint = FlowIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = action.type.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = action.type.category,
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

            // Reversible State Restoration Toggle (for volume, brightness, ringer, flashlight)
            val isReversibleType = action.type in listOf(
                ActionType.SET_MEDIA_VOLUME,
                ActionType.SET_RINGER_MODE,
                ActionType.SET_BRIGHTNESS,
                ActionType.SET_DND_MODE,
                ActionType.TOGGLE_FLASHLIGHT
            )

            if (isReversibleType) {
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
                                text = "Restore When Routine Ends",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Saves initial device state and reverts back when leaving mode or condition stops",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = restoreOnExit,
                            onCheckedChange = { restoreOnExit = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = FlowCyan,
                                checkedTrackColor = FlowCyan.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Specific Configurations
            when (action.type) {
                ActionType.SET_MEDIA_VOLUME -> {
                    Text(
                        text = "Audio Stream",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val streams = listOf("MEDIA" to "Media", "RING" to "Ring", "NOTIFICATION" to "Notification", "ALARM" to "Alarm")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(streams) { (key, label) ->
                            FilterChip(
                                selected = volumeStream == key,
                                onClick = { volumeStream = key },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Volume Level",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "${volumePercent.toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FlowCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowSlider(
                        value = volumePercent,
                        onValueChange = { volumePercent = it },
                        valueRange = 0f..100f,
                        steps = 20
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val volumePresets = listOf(0 to "Mute (0%)", 25 to "25%", 50 to "50%", 75 to "75%", 100 to "Max (100%)")
                        items(volumePresets) { (pct, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(glassColors.glassSurface)
                                    .clickable { volumePercent = pct.toFloat() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp, color = glassColors.textPrimary)
                            }
                        }
                    }
                }

                ActionType.SET_RINGER_MODE -> {
                    Text(
                        text = "Ringer Mode",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val modes = listOf("NORMAL" to "Sound (Normal)", "VIBRATE" to "Vibrate Only", "SILENT" to "Silent")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        modes.forEach { (modeKey, label) ->
                            FilterChip(
                                selected = ringerMode == modeKey,
                                onClick = { ringerMode = modeKey },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowIndigo
                                )
                            )
                        }
                    }
                }

                ActionType.SET_BRIGHTNESS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Screen Brightness",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = if (autoBrightness) "Adaptive" else "${brightnessPercent.toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FlowCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowSlider(
                        value = brightnessPercent,
                        onValueChange = { brightnessPercent = it },
                        valueRange = 0f..100f,
                        enabled = !autoBrightness
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Automatic Adaptive Brightness",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glassColors.textPrimary
                        )
                        FlowSwitch(
                            checked = autoBrightness,
                            onCheckedChange = { autoBrightness = it }
                        )
                    }
                }

                ActionType.SET_DND_MODE -> {
                    Text(
                        text = "Do Not Disturb Interruption Filter",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val dndOptions = listOf("PRIORITY" to "Priority Only", "TOTAL_SILENCE" to "Total Silence", "ALARMS_ONLY" to "Alarms Only", "OFF" to "Turn Off DND")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dndOptions) { (key, label) ->
                            FilterChip(
                                selected = dndMode == key,
                                onClick = { dndMode = key },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowIndigo
                                )
                            )
                        }
                    }
                }

                ActionType.SHOW_NOTIFICATION -> {
                    Text(
                        text = "Notification Title",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Notification Message",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Alert Priority",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOW" to "Low", "DEFAULT" to "Default", "HIGH" to "High").forEach { (priKey, priLabel) ->
                            FilterChip(
                                selected = notifPriority == priKey,
                                onClick = { notifPriority = priKey },
                                label = { Text(priLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowCyan
                                )
                            )
                        }
                    }
                }

                ActionType.LAUNCH_APP -> {
                    Text(
                        text = "Application to Launch",
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
                            Icon(Icons.Default.Apps, contentDescription = null, tint = FlowIndigo)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glassColors.textPrimary
                                )
                                Text(
                                    text = appPackage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.textSecondary
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Change", tint = FlowIndigo, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                ActionType.OPEN_URL -> {
                    Text(
                        text = "Web / Deep Link URL",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = webUrl,
                        onValueChange = { webUrl = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FlowCyan,
                            unfocusedBorderColor = glassColors.glassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val urlPresets = listOf(
                            "Google Maps" to "https://maps.google.com",
                            "Spotify" to "https://open.spotify.com",
                            "Google Calendar" to "https://calendar.google.com",
                            "YouTube" to "https://youtube.com"
                        )
                        items(urlPresets) { (label, url) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(glassColors.glassSurface)
                                    .clickable { webUrl = url }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp, color = glassColors.textPrimary)
                            }
                        }
                    }
                }

                ActionType.TOGGLE_FLASHLIGHT -> {
                    Text(
                        text = "Flashlight Mode",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = flashlightOn,
                            onClick = { flashlightOn = true },
                            label = { Text("Turn Torch ON") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                selectedLabelColor = FlowCyan
                            )
                        )
                        FilterChip(
                            selected = !flashlightOn,
                            onClick = { flashlightOn = false },
                            label = { Text("Turn Torch OFF") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                selectedLabelColor = FlowIndigo
                            )
                        )
                    }
                }

                ActionType.TRIGGER_VIBRATION -> {
                    Text(
                        text = "Vibration Pattern",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val patterns = listOf(
                        "CLICK" to "Single Click",
                        "DOUBLE_PULSE" to "Double Pulse",
                        "ALERT" to "Alert Wave",
                        "SOS" to "SOS Emergency"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(patterns) { (patKey, patLabel) ->
                            FilterChip(
                                selected = vibrationPattern == patKey,
                                onClick = { vibrationPattern = patKey },
                                label = { Text(patLabel, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowCyan
                                )
                            )
                        }
                    }
                }

                ActionType.OPEN_SETTINGS -> {
                    Text(
                        text = "System Settings Target",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val targets = listOf("WIFI", "BLUETOOTH", "BATTERY", "DISPLAY", "SOUND", "DND", "LOCATION")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(targets) { tgt ->
                            FilterChip(
                                selected = settingsTarget == tgt,
                                onClick = { settingsTarget = tgt },
                                label = { Text(tgt, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowIndigo.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowIndigo
                                )
                            )
                        }
                    }
                }

                ActionType.ACCESSIBILITY_NAV -> {
                    Text(
                        text = "Accessibility Navigation Action",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val navActions = listOf("HOME" to "Home Screen", "BACK" to "Back Button", "LOCK" to "Lock Device", "QUICK_SETTINGS" to "Quick Settings", "NOTIFICATIONS" to "Notifications Panel")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(navActions) { (key, label) ->
                            FilterChip(
                                selected = accessibilityAction == key,
                                onClick = { accessibilityAction = key },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FlowCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = FlowCyan
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            FlowGradientButton(
                text = "Apply Configuration",
                onClick = {
                    val newConfig = JSONObject()
                    var newSummary = action.summary

                    when (action.type) {
                        ActionType.SET_MEDIA_VOLUME -> {
                            newConfig.put("stream", volumeStream)
                            newConfig.put("volume", volumePercent.toInt())
                            newSummary = "$volumeStream Volume: ${volumePercent.toInt()}%"
                        }
                        ActionType.SET_RINGER_MODE -> {
                            newConfig.put("mode", ringerMode)
                            newSummary = "Switch to $ringerMode"
                        }
                        ActionType.SET_BRIGHTNESS -> {
                            newConfig.put("brightness", brightnessPercent.toInt())
                            newConfig.put("autoBrightness", autoBrightness)
                            newSummary = if (autoBrightness) "Adaptive Brightness" else "Brightness: ${brightnessPercent.toInt()}%"
                        }
                        ActionType.SET_DND_MODE -> {
                            newConfig.put("mode", dndMode)
                            newSummary = "DND: $dndMode"
                        }
                        ActionType.SHOW_NOTIFICATION -> {
                            newConfig.put("title", notifTitle)
                            newConfig.put("message", notifMessage)
                            newConfig.put("priority", notifPriority)
                            newSummary = "\"$notifTitle\""
                        }
                        ActionType.LAUNCH_APP -> {
                            newConfig.put("packageName", appPackage)
                            newConfig.put("appLabel", appLabel)
                            newSummary = "Open $appLabel"
                        }
                        ActionType.OPEN_URL -> {
                            newConfig.put("url", webUrl)
                            newSummary = webUrl
                        }
                        ActionType.TOGGLE_FLASHLIGHT -> {
                            newConfig.put("enable", flashlightOn)
                            newSummary = if (flashlightOn) "Torch ON" else "Torch OFF"
                        }
                        ActionType.TRIGGER_VIBRATION -> {
                            newConfig.put("pattern", vibrationPattern)
                            newSummary = "Pattern: $vibrationPattern"
                        }
                        ActionType.OPEN_SETTINGS -> {
                            newConfig.put("target", settingsTarget)
                            newSummary = "Open $settingsTarget"
                        }
                        ActionType.ACCESSIBILITY_NAV -> {
                            newConfig.put("action", accessibilityAction)
                            newSummary = "Press $accessibilityAction"
                        }
                    }

                    val updated = action.copy(
                        configJson = newConfig.toString(),
                        summary = newSummary,
                        restoreOnExit = restoreOnExit
                    )
                    onSaveAction(updated)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAppPicker) {
        val appSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AppPickerSheet(
            sheetState = appSheetState,
            onDismiss = { showAppPicker = false },
            onAppSelected = { pkg, name ->
                appPackage = pkg
                appLabel = name
                showAppPicker = false
            }
        )
    }
}
