package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.permissions.PermissionManager
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowRowItem
import com.example.ui.components.FlowSwitch
import com.example.ui.motion.FlowHaptics
import com.example.ui.motion.FlowMotion
import com.example.ui.motion.flowPress
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.LocalFlowGlassColors

@Composable
fun SettingsScreen(
    themeMode: String,
    oledBlack: Boolean,
    isDeveloperMode: Boolean,
    onThemeModeChange: (String) -> Unit,
    onOledBlackChange: (Boolean) -> Unit,
    onDeveloperModeChange: (Boolean) -> Unit,
    onExportBackup: () -> String,
    onImportBackup: (String) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToLogs: () -> Unit,
    onNavigateToDeveloper: () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var importInputText by remember { mutableStateOf("") }

    val hasBatteryExemption = remember {
        PermissionManager.checkBatteryOptimizationIgnored(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item(key = "header") {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Preferences and system settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glassColors.textSecondary
                )
            }
        }

        // Section: Automation
        item(key = "section_automation") {
            SettingsSection(title = "Automation") {
                FlowRowItem(
                    title = "Execution Logs",
                    subtitle = "History and real-time trace",
                    icon = Icons.Default.ListAlt,
                    onClick = onNavigateToLogs
                )
            }
        }

        // Section: Permissions
        item(key = "section_permissions") {
            SettingsSection(title = "Permissions") {
                FlowRowItem(
                    title = "Permission Center",
                    subtitle = "Manage Android system capabilities",
                    icon = Icons.Default.Security,
                    onClick = onNavigateToPermissions
                )
            }
        }

        // Section: Appearance
        item(key = "section_appearance") {
            SettingsSection(title = "Appearance") {
                // Theme Mode Selector
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = glassColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (key, label) ->
                            val isSelected = themeMode == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .flowPress(level = FlowMotion.Level.SUBTLE)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) FlowCyan else glassColors.glassSurface)
                                    .clickable {
                                        FlowHaptics.selection(hapticFeedback, view)
                                        onThemeModeChange(key)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.Black else glassColors.textSecondary
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.5.dp)
                        .background(glassColors.glassBorder)
                )

                // OLED Pure Black
                FlowRowItem(
                    title = "Pure Black (OLED)",
                    subtitle = "Save battery on OLED displays",
                    icon = Icons.Default.DarkMode,
                    trailing = {
                        FlowSwitch(
                            checked = oledBlack,
                            onCheckedChange = onOledBlackChange
                        )
                    }
                )
            }
        }

        // Section: Battery & Reliability
        item(key = "section_battery") {
            SettingsSection(title = "Battery & Reliability") {
                FlowRowItem(
                    title = "Battery Optimization",
                    subtitle = if (hasBatteryExemption) "Unrestricted (Recommended)" else "Optimized (May delay triggers)",
                    icon = Icons.Default.BatteryAlert,
                    onClick = {
                        PermissionManager.requestIgnoreBatteryOptimizations(context)
                    }
                )
            }
        }

        // Section: Data & Backup
        item(key = "section_data") {
            SettingsSection(title = "Data & Backup") {
                FlowRowItem(
                    title = "Export Backup",
                    subtitle = "Save routines and settings as JSON",
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        exportedJsonText = onExportBackup()
                        showExportDialog = true
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.5.dp)
                        .background(glassColors.glassBorder)
                )
                FlowRowItem(
                    title = "Import Backup",
                    subtitle = "Restore routines from JSON",
                    icon = Icons.Default.CloudUpload,
                    onClick = {
                        importInputText = ""
                        showImportDialog = true
                    }
                )
            }
        }

        // Section: Developer & About
        item(key = "section_about") {
            SettingsSection(title = "About") {
                FlowRowItem(
                    title = "FlowModes Version",
                    subtitle = "2.0.0 (Flagship Minimal)",
                    icon = Icons.Default.Info
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.5.dp)
                        .background(glassColors.glassBorder)
                )
                FlowRowItem(
                    title = "Developer Tools",
                    subtitle = "Diagnostics and simulation tools",
                    icon = Icons.Default.BugReport,
                    trailing = {
                        FlowSwitch(
                            checked = isDeveloperMode,
                            onCheckedChange = onDeveloperModeChange
                        )
                    },
                    onClick = if (isDeveloperMode) onNavigateToDeveloper else null
                )
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup Exported") },
            text = {
                Column {
                    Text("Configuration JSON copied or ready for export:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("FlowModes Backup", exportedJsonText))
                        showExportDialog = false
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup") },
            text = {
                Column {
                    Text("Paste your exported FlowModes JSON configuration:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("Paste JSON here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importInputText.isNotBlank()) {
                            onImportBackup(importInputText.trim())
                        }
                        showImportDialog = false
                    },
                    enabled = importInputText.isNotBlank()
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val glassColors = LocalFlowGlassColors.current

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = glassColors.textSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        FlowGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}
