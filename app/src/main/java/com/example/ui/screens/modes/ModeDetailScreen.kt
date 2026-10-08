package com.example.ui.screens.modes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.Mode
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowPrimaryButton
import com.example.ui.components.FlowSwitch
import com.example.ui.components.IconHelper
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ModeDetailScreen(
    mode: Mode?,
    onNavigateBack: () -> Unit,
    onToggleMode: (Mode) -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    if (mode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Mode not found", color = glassColors.textSecondary)
        }
        return
    }

    val accentColor = remember(mode.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(mode.colorHex))
        } catch (_: Exception) {
            FlowCyan
        }
    }

    val isSelected = mode.isActive

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

                Text(
                    text = "${mode.name} Mode",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )

                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                FlowPrimaryButton(
                    text = if (isSelected) "Turn Off Mode" else "Activate Mode Now",
                    icon = Icons.Default.PowerSettingsNew,
                    onClick = {
                        FlowHaptics.modeToggle(hapticFeedback, view, !isSelected)
                        onToggleMode(mode)
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
            // Mode Header Hero Card
            item(key = "mode_hero_card") {
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isSelected) accentColor.copy(alpha = 0.5f) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentColor.copy(alpha = 0.18f))
                                .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getIconByName(mode.iconName),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = mode.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    ),
                                    color = glassColors.textPrimary
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(StatusSuccess.copy(alpha = 0.18f))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSuccess
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glassColors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        FlowSwitch(
                            checked = isSelected,
                            onCheckedChange = {
                                FlowHaptics.modeToggle(hapticFeedback, view, !isSelected)
                                onToggleMode(mode)
                            }
                        )
                    }
                }
            }

            // Status Details Card
            item(key = "status_info_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current State",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSelected) "Active & Running" else "Inactive / Standby",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) StatusSuccess else glassColors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Actions Executed",
                                fontSize = 12.sp,
                                color = glassColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${mode.actions.size} profile settings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            // Actions Header
            item(key = "actions_header") {
                Column {
                    Text(
                        text = "WHAT THIS MODE DOES",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.8.sp
                        ),
                        color = glassColors.textSecondary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    Text(
                        text = "When activated, FlowModes applies these exact device settings to eliminate distractions and adapt your environment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.textMuted,
                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                    )

                    // Actions list card
                    FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            if (mode.actions.isEmpty()) {
                                Text(
                                    text = "No custom actions configured for this mode.",
                                    fontSize = 13.sp,
                                    color = glassColors.textMuted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                mode.actions.forEachIndexed { index, act ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(accentColor.copy(alpha = 0.14f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconHelper.getIconByName(act.type.iconName),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = act.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                color = glassColors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = act.summary,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                color = glassColors.textSecondary
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Applied",
                                                tint = StatusSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (index < mode.actions.size - 1) {
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

            // How to trigger this mode info card
            item(key = "trigger_info_card") {
                FlowGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = FlowCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ways to trigger ${mode.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = glassColors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val triggers = listOf(
                            "Tap the mode tile on the Home or Modes screen",
                            "Quick Settings Tile in Android notification shade",
                            "Home Screen App Widget 1x1 or 2x2",
                            "Physical NFC sticker or tag tap",
                            "Flip phone face-down gesture (if configured in Settings)",
                            "Automated Routine with schedule or geofence"
                        )

                        triggers.forEach { triggerText ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 12.sp,
                                    color = FlowCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = triggerText,
                                    fontSize = 12.sp,
                                    color = glassColors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
