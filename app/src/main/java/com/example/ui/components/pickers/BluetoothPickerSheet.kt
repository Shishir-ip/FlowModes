package com.example.ui.components.pickers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
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
import androidx.core.content.ContextCompat
import com.example.ui.components.FlowGlassCard
import com.example.ui.components.FlowGradientButton
import com.example.ui.theme.FlowCyan
import com.example.ui.theme.FlowIndigo
import com.example.ui.theme.LocalFlowGlassColors

data class PairedDeviceItem(
    val name: String,
    val address: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPickerSheet(
    initialDeviceName: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDeviceSelected: (deviceName: String) -> Unit
) {
    val context = LocalContext.current
    val glassColors = LocalFlowGlassColors.current
    var customDeviceName by remember { mutableStateOf(if (initialDeviceName == "Any") "" else initialDeviceName) }

    val pairedDevices = remember {
        getPairedBluetoothDevices(context)
    }

    val presets = listOf("Car Audio / Android Auto", "Galaxy Buds / AirPods", "Smartwatch", "Bluetooth Speaker")

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
                    text = "Select Bluetooth Device",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = glassColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = glassColors.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Any Device Option
            FlowGlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                onClick = { onDeviceSelected("") }
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
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = FlowCyan)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Any Bluetooth Device",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = glassColors.textPrimary
                        )
                        Text(
                            text = "Trigger whenever any device connects or disconnects",
                            style = MaterialTheme.typography.bodySmall,
                            color = glassColors.textSecondary
                        )
                    }
                    if (customDeviceName.isEmpty()) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FlowCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Paired Devices (${pairedDevices.size})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (pairedDevices.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        pairedDevices,
                        key = { it.address.ifEmpty { it.name } },
                        contentType = { "device_item" }
                    ) { device ->
                        FlowGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                customDeviceName = device.name
                                onDeviceSelected(device.name)
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Devices, contentDescription = null, tint = FlowIndigo, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = glassColors.textPrimary
                                    )
                                    Text(text = device.address, fontSize = 10.sp, color = glassColors.textSecondary)
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No paired Bluetooth devices detected on this device/emulator. You can enter device name below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Or enter device name / keyword",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = glassColors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customDeviceName,
                onValueChange = { customDeviceName = it },
                placeholder = { Text("e.g. MyCar, Sony WH-1000XM5, Galaxy Buds") },
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets, key = { it }, contentType = { "preset_chip" }) { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(glassColors.glassSurface)
                            .clickable { customDeviceName = preset }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = preset,
                            fontSize = 12.sp,
                            color = if (customDeviceName == preset) FlowCyan else glassColors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FlowGradientButton(
                text = "Confirm Device",
                onClick = { onDeviceSelected(customDeviceName.trim()) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@SuppressLint("MissingPermission")
private fun getPairedBluetoothDevices(context: Context): List<PairedDeviceItem> {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }
        }
        val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bm?.adapter ?: BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
        val bonded = adapter.bondedDevices ?: return emptyList()
        bonded.map { device ->
            PairedDeviceItem(device.name ?: "Unnamed Device", device.address ?: "")
        }
    } catch (_: Exception) {
        emptyList()
    }
}
