package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.ConditionLogic
import com.example.domain.models.RoutinePriority
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.motion.flowPress
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning

/**
 * Lightweight, high-performance card surface with tactile spring press feedback.
 * Minimal border, solid surface, no expensive blur/radial gradient loops.
 */
@Composable
fun FlowGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    accentGlowColor: Color? = null,
    motionLevel: FlowMotion.Level = FlowMotion.Level.MEDIUM,
    enablePressAnimation: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val surfaceColor = backgroundColor ?: glassColors.glassSurface
    val borderCol = borderColor ?: (if (accentGlowColor != null) accentGlowColor.copy(alpha = 0.5f) else glassColors.glassBorder)
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val baseModifier = modifier
        .clip(shape)
        .background(surfaceColor)
        .border(width = 1.dp, color = borderCol, shape = shape)

    val finalModifier = if (onClick != null) {
        if (enablePressAnimation) {
            val interactionSource = remember { MutableInteractionSource() }
            baseModifier
                .flowPress(level = motionLevel, interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = {
                        FlowHaptics.tick(hapticFeedback, view)
                        onClick()
                    }
                )
        } else {
            baseModifier.clickable(
                role = Role.Button,
                onClick = {
                    FlowHaptics.tick(hapticFeedback, view)
                    onClick()
                }
            )
        }
    } else {
        baseModifier
    }

    Box(modifier = finalModifier) {
        content()
    }
}

/**
 * Clean, compact list row item adhering to flagship minimal guidelines.
 * Displays: Icon (in subtle container) | Title + Subtitle | Trailing action/switch/chevron.
 * Optimized for zero animation overhead and instant touch response in LazyLists.
 */
@Composable
fun FlowRowItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = FlowCyan,
    iconBackgroundColor: Color = iconTint.copy(alpha = 0.12f),
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val rowModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(
                        role = Role.Button,
                        onClick = {
                            FlowHaptics.tick(hapticFeedback, view)
                            onClick()
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            } else {
                Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            }
        )

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = glassColors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        } else if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = glassColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Primary action button: Clean solid filled button with tactile press spring feedback.
 */
@Composable
fun FlowPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = {
            FlowHaptics.impact(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(48.dp)
            .flowPress(level = FlowMotion.Level.MEDIUM, enabled = enabled, interactionSource = interactionSource),
        shape = RoundedCornerShape(14.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = FlowCyan,
            contentColor = Color.Black,
            disabledContainerColor = FlowCyan.copy(alpha = 0.3f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Backwards-compatible alias for existing screens. Uses the clean primary button styling.
 */
@Composable
fun FlowGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(FlowCyan, FlowIndigo),
    enabled: Boolean = true
) {
    FlowPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled
    )
}

@Composable
fun FlowOutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedButton(
        onClick = {
            FlowHaptics.tick(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(44.dp)
            .flowPress(level = FlowMotion.Level.SUBTLE, interactionSource = interactionSource),
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = glassColors.textPrimary
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassColors.glassBorder)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FlowCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FlowLogicToggle(
    currentLogic: ConditionLogic,
    onLogicChanged: (ConditionLogic) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(glassColors.glassSurface)
            .border(1.dp, glassColors.glassBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isAll = currentLogic == ConditionLogic.ALL
        val isAny = currentLogic == ConditionLogic.ANY

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (isAll) FlowCyan else Color.Transparent)
                .clickable {
                    if (!isAll) {
                        FlowHaptics.tick(hapticFeedback, view)
                        onLogicChanged(ConditionLogic.ALL)
                    }
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ALL (AND)",
                fontSize = 11.sp,
                fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                color = if (isAll) Color.Black else glassColors.textSecondary
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (isAny) FlowCyan else Color.Transparent)
                .clickable {
                    if (!isAny) {
                        FlowHaptics.tick(hapticFeedback, view)
                        onLogicChanged(ConditionLogic.ANY)
                    }
                }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ANY (OR)",
                fontSize = 11.sp,
                fontWeight = if (isAny) FontWeight.Bold else FontWeight.Medium,
                color = if (isAny) Color.Black else glassColors.textSecondary
            )
        }
    }
}

@Composable
fun FlowPriorityBadge(
    priority: RoutinePriority,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority) {
        RoutinePriority.CRITICAL -> Pair(StatusError.copy(alpha = 0.15f), StatusError)
        RoutinePriority.HIGH -> Pair(StatusWarning.copy(alpha = 0.15f), StatusWarning)
        RoutinePriority.NORMAL -> Pair(FlowCyan.copy(alpha = 0.12f), FlowCyan)
        RoutinePriority.LOW -> Pair(Color.Gray.copy(alpha = 0.12f), Color.LightGray)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

/**
 * Handcrafted tactile switch with real spring physics, controlled overshoot,
 * slight squash/stretch, track color transitions, and zero input latency.
 */
@Composable
fun FlowSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    val trackColor = if (checked) FlowCyan else Color(0xFF2A303C)
    val thumbColor = if (checked) Color.Black else Color(0xFFE2E8F0)

    // Travel distance: width(50.dp) - 2 * padding(3.dp) - thumbSize(22.dp) = 22.dp
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        animationSpec = FlowMotion.ToggleDp,
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                indication = null,
                interactionSource = null,
                onValueChange = { newValue ->
                    FlowHaptics.tick(hapticFeedback, view)
                    onCheckedChange(newValue)
                }
            )
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

/**
 * Tactile Slider with zero-latency finger tracking and clean rendering.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FlowSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            FlowHaptics.tick(hapticFeedback, view)
        }
    }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = {
            Box(
                modifier = Modifier
                    .size(if (isPressed) 22.dp else 20.dp)
                    .clip(CircleShape)
                    .background(FlowCyan)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape)
            )
        },
        colors = SliderDefaults.colors(
            thumbColor = FlowCyan,
            activeTrackColor = FlowCyan,
            inactiveTrackColor = glassColors.glassBorder
        )
    )
}

/**
 * Execution Button with loading spinner, checkmark pop, and tactile spring feedback.
 */
@Composable
fun FlowRunButton(
    text: String = "Test Run Routine",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    isSuccess: Boolean = false
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }

    val buttonBg by animateColorAsState(
        targetValue = if (isSuccess) Color(0xFF10B981) else FlowCyan,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "runButtonBg"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (isSuccess) 1.0f else 0.6f,
        animationSpec = FlowMotion.Success,
        label = "checkScale"
    )

    Button(
        onClick = {
            FlowHaptics.impact(hapticFeedback, view)
            onClick()
        },
        modifier = modifier
            .height(48.dp)
            .flowPress(level = FlowMotion.Level.MEDIUM, interactionSource = interactionSource),
        shape = RoundedCornerShape(14.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBg,
            contentColor = Color.Black
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Running...",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            } else if (isSuccess) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Executed Successfully",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun FlowEmptyState(
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    icon: ImageVector = Icons.Default.AutoAwesome,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalFlowGlassColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(FlowCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FlowCyan,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = glassColors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = glassColors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        FlowPrimaryButton(
            text = buttonText,
            onClick = onButtonClick,
            icon = Icons.Default.Add,
            modifier = Modifier.width(200.dp)
        )
    }
}
