package com.example.ui.components.pickers

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiPickerSheet(
    initialSsid: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onWifiSelected: (ssid: String) -> Unit
) {
    val context = LocalContext.current
    val glassColors = LocalFlowGlassColors.current
    var customSsid by remember { mutableStateOf(if (initialSsid == "Any") "" else initialSsid) }

    val currentConnectedSsid = remember {
        detectCurrentSsid(context)
    }

    val presets = listOf("Home", "Office", "Café", "Gym", "School", "Guest")

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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Wi-Fi Network",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glassColors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 1: Any Network
            FlowGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = { onWifiSelected("") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FlowCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = FlowCyan)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Any Wi-Fi Network",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "Trigger whenever Wi-Fi connects or disconnects",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                    if (customSsid.isEmpty()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FlowCyan)
                    }
                }
            }

            // Option 2: Current Connected Network
            if (currentConnectedSsid.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        customSsid = currentConnectedSsid
                        onWifiSelected(currentConnectedSsid)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FlowIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = FlowIndigo)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Network: \"$currentConnectedSsid\"",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = glassColors.textPrimary
                            )
                            Text(
                                text = "Connected right now",
                                style = MaterialTheme.typography.bodySmall,
                                color = FlowIndigo
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Or enter specific SSID",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customSsid,
                onValueChange = { customSsid = it },
                placeholder = { Text("e.g. Home-5G or MyOffice") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlowCyan,
                    unfocusedBorderColor = glassColors.glassBorder,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Quick Presets:",
                style = MaterialTheme.typography.bodySmall,
                color = glassColors.textSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets, key = { it }, contentType = { "preset_chip" }) { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassColors.glassSurface)
                            .clickable { customSsid = preset }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 13.sp,
                            color = if (customSsid == preset) FlowCyan else glassColors.textPrimary,
                            fontWeight = if (customSsid == preset) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            FlowGradientButton(
                text = "Confirm Network SSID",
                onClick = {
                    onWifiSelected(customSsid.trim())
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@SuppressLint("MissingPermission")
private fun detectCurrentSsid(context: Context): String {
    return try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val connectionInfo: WifiInfo? = wm?.connectionInfo
        val ssid = connectionInfo?.ssid?.replace("\"", "") ?: ""
        if (ssid == "<unknown ssid>") "" else ssid
    } catch (_: Exception) {
        ""
    }
}
