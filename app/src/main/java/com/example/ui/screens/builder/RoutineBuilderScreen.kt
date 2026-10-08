package com.example.ui.screens.builder

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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.Routine
import com.example.domain.models.RoutinePriority
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowLogicToggle
import com.example.ui.components.FlowOutlinedActionButton
import com.example.ui.components.FlowPrimaryButton
import com.example.ui.components.IconHelper
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.motion.flowPress
import com.example.ui.motion.flowShake
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderScreen(
    initialRoutine: Routine?,
    onSave: (Routine) -> Unit,
    onNavigateBack: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    var routineName by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var routineDesc by remember { mutableStateOf(initialRoutine?.description ?: "") }
    var selectedColor by remember { mutableStateOf(initialRoutine?.colorHex ?: "#38BDF8") }
    var selectedIcon by remember { mutableStateOf(initialRoutine?.iconName ?: "AutoAwesome") }
    var conditionLogic by remember { mutableStateOf(initialRoutine?.conditionLogic ?: ConditionLogic.ALL) }
    var priority by remember { mutableStateOf(initialRoutine?.priority ?: RoutinePriority.NORMAL) }

    var shakeNameTrigger by remember { mutableIntStateOf(0) }
    var shakeActionsTrigger by remember { mutableIntStateOf(0) }

    val conditions = remember {
        mutableStateListOf<AutomationCondition>().apply {
            if (initialRoutine != null) addAll(initialRoutine.conditions)
        }
    }

    val actions = remember {
        mutableStateListOf<AutomationAction>().apply {
            if (initialRoutine != null) addAll(initialRoutine.actions)
        }
    }

    var showConditionPicker by remember { mutableStateOf(false) }
    var showActionPicker by remember { mutableStateOf(false) }
    val conditionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val actionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var editingCondition by remember { mutableStateOf<AutomationCondition?>(null) }
    var editingAction by remember { mutableStateOf<AutomationAction?>(null) }
    val conditionConfigSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val actionConfigSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val colorOptions = listOf("#38BDF8", "#818CF8", "#A78BFA", "#F43F5E", "#10B981", "#F59E0B")
    val iconOptions = listOf("AutoAwesome", "Bedtime", "Work", "School", "DirectionsCar", "Flight", "Headphones", "Wifi", "BatteryAlert")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    text = if (initialRoutine == null) "New Routine" else "Edit Routine",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
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
                    text = "Save Routine",
                    onClick = {
                        if (routineName.isBlank()) {
                            shakeNameTrigger++
                            FlowHaptics.error(hapticFeedback, view)
                            return@FlowPrimaryButton
                        }
                        if (actions.isEmpty()) {
                            shakeActionsTrigger++
                            FlowHaptics.error(hapticFeedback, view)
                            return@FlowPrimaryButton
                        }

                        FlowHaptics.success(hapticFeedback, view)
                        val routineId = initialRoutine?.id ?: UUID.randomUUID().toString()
                        val updatedConditions = conditions.map { it.copy(routineId = routineId) }
                        val updatedActions = actions.map { it.copy(routineId = routineId) }

                        val routine = Routine(
                            id = routineId,
                            name = routineName.trim(),
                            description = routineDesc.trim(),
                            iconName = selectedIcon,
                            colorHex = selectedColor,
                            isEnabled = initialRoutine?.isEnabled ?: true,
                            isFavorite = initialRoutine?.isFavorite ?: false,
                            conditionLogic = conditionLogic,
                            priority = priority,
                            conditions = updatedConditions,
                            actions = updatedActions,
                            createdAt = initialRoutine?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(routine)
                    },
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
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Basic Info Section (Name, Icon & Color)
            item(key = "basic_info") {
                FlowGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .flowShake(shakeNameTrigger)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = routineName,
                            onValueChange = { routineName = it },
                            label = { Text("Routine Name") },
                            placeholder = { Text("e.g. Night Sleep, Leaving Home") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FlowCyan,
                                unfocusedBorderColor = glassColors.glassBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Color options
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(colorOptions) { hex ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (_: Exception) {
                                    FlowCyan
                                }
                                val isSelected = hex == selectedColor
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            FlowHaptics.selection(hapticFeedback, view)
                                            selectedColor = hex
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Icon options
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(iconOptions) { iconName ->
                                val isSelected = iconName == selectedIcon
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) FlowCyan.copy(alpha = 0.2f) else glassColors.glassSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) FlowCyan else glassColors.glassBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            FlowHaptics.selection(hapticFeedback, view)
                                            selectedIcon = iconName
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getIconByName(iconName),
                                        contentDescription = null,
                                        tint = if (isSelected) FlowCyan else glassColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

        // 2. WHEN (Conditions) Section
        item(key = "when_section") {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WHEN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = glassColors.textPrimary
                    )

                    if (conditions.size > 1) {
                        FlowLogicToggle(
                            currentLogic = conditionLogic,
                            onLogicChanged = { conditionLogic = it }
                        )
                    }
                }

                // Single container for all conditions
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        if (conditions.isEmpty()) {
                            Text(
                                text = "No conditions (Runs manually or on all events)",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            conditions.forEachIndexed { index, condition ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            FlowHaptics.tick(hapticFeedback, view)
                                            editingCondition = condition
                                        }
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
                                            imageVector = IconHelper.getIconByName(condition.type.iconName),
                                            contentDescription = null,
                                            tint = FlowCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = condition.type.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = condition.summary,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = glassColors.textSecondary,
                                            maxLines = 1
                                        )
                                    }

                                    IconButton(
                                        onClick = { conditions.remove(condition) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = glassColors.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < conditions.size - 1) {
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

                Spacer(modifier = Modifier.height(8.dp))

                FlowOutlinedActionButton(
                    text = "+ Add Condition",
                    onClick = { showConditionPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. THEN (Actions) Section
        item(key = "then_section") {
            Column {
                Text(
                    text = "THEN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = glassColors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Single container for all actions
                FlowGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .flowShake(shakeActionsTrigger)
                ) {
                    Column {
                        if (actions.isEmpty()) {
                            Text(
                                text = "No actions configured yet. At least 1 is required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.textMuted,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            actions.forEachIndexed { index, action ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            FlowHaptics.tick(hapticFeedback, view)
                                            editingAction = action
                                        }
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
                                            imageVector = IconHelper.getIconByName(action.type.iconName),
                                            contentDescription = null,
                                            tint = FlowCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = action.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = glassColors.textPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = action.summary,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = glassColors.textSecondary,
                                            maxLines = 1
                                        )
                                    }

                                    IconButton(
                                        onClick = { actions.remove(action) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = glassColors.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < actions.size - 1) {
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

                Spacer(modifier = Modifier.height(8.dp))

                FlowOutlinedActionButton(
                    text = "+ Add Action",
                    onClick = { showActionPicker = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Bottom Sheets for Condition/Action Picking & Configuration
    if (showConditionPicker) {
        ConditionPickerBottomSheet(
            sheetState = conditionSheetState,
            onDismiss = { showConditionPicker = false },
            onConditionSelected = { cond ->
                conditions.add(cond)
                showConditionPicker = false
                editingCondition = cond
            }
        )
    }

    if (showActionPicker) {
        ActionPickerBottomSheet(
            sheetState = actionSheetState,
            onDismiss = { showActionPicker = false },
            onActionSelected = { act ->
                actions.add(act)
                showActionPicker = false
                editingAction = act
            }
        )
    }

    editingCondition?.let { cond ->
        ConditionConfigSheet(
            condition = cond,
            sheetState = conditionConfigSheetState,
            onDismiss = { editingCondition = null },
            onSaveCondition = { updated ->
                val idx = conditions.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    conditions[idx] = updated
                }
                editingCondition = null
            }
        )
    }

    editingAction?.let { act ->
        ActionConfigSheet(
            action = act,
            sheetState = actionConfigSheetState,
            onDismiss = { editingAction = null },
            onSaveAction = { updated ->
                val idx = actions.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    actions[idx] = updated
                }
                editingAction = null
            }
        )
    }
}
}
