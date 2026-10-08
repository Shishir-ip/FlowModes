package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneLocked
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "Bedtime" -> Icons.Default.Bedtime
            "Work" -> Icons.Default.Work
            "School" -> Icons.Default.School
            "SportsEsports" -> Icons.Default.SportsEsports
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Flight" -> Icons.Default.Flight
            "Schedule" -> Icons.Default.Schedule
            "AccessTime" -> Icons.Default.AccessTime
            "DateRange" -> Icons.Default.DateRange
            "BatteryAlert" -> Icons.Default.BatteryAlert
            "BatteryChargingFull" -> Icons.Default.BatteryChargingFull
            "Power" -> Icons.Default.Power
            "PowerOff" -> Icons.Default.PowerOff
            "Wifi" -> Icons.Default.Wifi
            "WifiOff" -> Icons.Default.WifiOff
            "Bluetooth" -> Icons.Default.Bluetooth
            "BluetoothDisabled" -> Icons.Default.BluetoothDisabled
            "Smartphone" -> Icons.Default.Smartphone
            "PhonelinkErase" -> Icons.Default.PhonelinkErase
            "Headphones" -> Icons.Default.Headphones
            "HeadsetOff" -> Icons.Default.HeadsetOff
            "RestartAlt" -> Icons.Default.RestartAlt
            "Apps" -> Icons.Default.Apps
            "NotificationsActive" -> Icons.Default.NotificationsActive
            "LocationOn" -> Icons.Default.LocationOn
            "TouchApp" -> Icons.Default.TouchApp
            "VolumeUp" -> Icons.Default.VolumeUp
            "BrightnessMedium" -> Icons.Default.BrightnessMedium
            "NotificationImportant" -> Icons.Default.NotificationImportant
            "Launch" -> Icons.Default.Launch
            "Language" -> Icons.Default.Language
            "FlashlightOn" -> Icons.Default.FlashlightOn
            "Vibration" -> Icons.Default.Vibration
            "Settings" -> Icons.Default.Settings
            "DoNotDisturb" -> Icons.Default.DoNotDisturb
            "Accessibility" -> Icons.Default.Accessibility
            "Notifications" -> Icons.Default.Notifications
            "Nfc" -> Icons.Default.Nfc
            "Event" -> Icons.Default.CalendarMonth
            "PhoneLocked" -> Icons.Default.PhoneLocked
            "ScreenRotation" -> Icons.Default.ScreenRotation
            else -> Icons.Default.AutoAwesome
        }
    }
}
