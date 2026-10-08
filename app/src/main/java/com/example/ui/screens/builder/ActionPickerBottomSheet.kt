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
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.ui.components.FlowRowItem
import com.example.ui.components.IconHelper
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPickerBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onActionSelected: (AutomationAction) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    var searchQuery by remember { mutableStateOf("") }

    val allActions = remember { ActionType.entries }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allActions
        else allActions.filter {
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
                    text = "Add Action",
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
                placeholder = { Text("Search actions (e.g. Volume, DND, Bluetooth)") },
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
                            val newAction = createDefaultAction(item)
                            onActionSelected(newAction)
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

private fun createDefaultAction(type: ActionType): AutomationAction {
    val id = UUID.randomUUID().toString()
    return when (type) {
        ActionType.SET_MEDIA_VOLUME -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Media 50%",
            configJson = """{"stream":"MEDIA","volume":50}"""
        )
        ActionType.SET_RINGER_MODE -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Vibrate",
            configJson = """{"mode":"VIBRATE"}"""
        )
        ActionType.SET_BRIGHTNESS -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Brightness 50%",
            configJson = """{"brightness":50,"autoBrightness":false}"""
        )
        ActionType.SHOW_NOTIFICATION -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "FlowModes Alert",
            configJson = """{"title":"FlowModes","message":"Routine activated"}"""
        )
        ActionType.LAUNCH_APP -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Select an app",
            configJson = """{"packageName":"","appName":""}"""
        )
        ActionType.OPEN_URL -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "https://google.com",
            configJson = """{"url":"https://google.com"}"""
        )
        ActionType.TOGGLE_FLASHLIGHT -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Turn On",
            configJson = """{"state":true}"""
        )
        ActionType.TRIGGER_VIBRATION -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Short buzz",
            configJson = """{"durationMs":200}"""
        )
        ActionType.OPEN_SETTINGS -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "System Settings",
            configJson = """{"action":"settings"}"""
        )
        ActionType.SET_DND_MODE -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Priority Only",
            configJson = """{"mode":"PRIORITY"}"""
        )
        ActionType.ACCESSIBILITY_NAV -> AutomationAction(
            id = id,
            routineId = "",
            type = type,
            title = type.displayName,
            summary = "Go Home",
            configJson = """{"action":"HOME"}"""
        )
    }
}
