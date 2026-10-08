package com.example.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.automation.services.FlowAccessibilityService
import com.example.automation.services.FlowNotificationListenerService

data class PermissionItem(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconName: String,
    val isGranted: Boolean,
    val isCriticalForBackground: Boolean,
    val actionIntentProvider: (Context) -> Intent
) {
    val title: String get() = name

    fun onRequest(context: Context) {
        try {
            val intent = actionIntentProvider(context).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}

object PermissionManager {

    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = createAppDetailsIntent(context).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun checkExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    fun checkNotificationListenerAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat?.contains(context.packageName) == true
    }

    fun checkAccessibilityAccess(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, FlowAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedComponentName)
    }

    fun checkBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun checkDndAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return notificationManager?.isNotificationPolicyAccessGranted ?: false
    }

    fun checkWriteSettingsPermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun getAllPermissions(context: Context): List<PermissionItem> {
        return listOf(
            PermissionItem(
                id = "notifications",
                name = "Notifications",
                description = "Allows FlowModes to show trigger notifications and alerts",
                category = "Core System",
                iconName = "Notifications",
                isGranted = checkNotificationPermission(context),
                isCriticalForBackground = false,
                actionIntentProvider = { ctx ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                        }
                    } else {
                        createAppDetailsIntent(ctx)
                    }
                }
            ),
            PermissionItem(
                id = "battery_optimization",
                name = "Battery Restrictions",
                description = "Unrestricted battery mode ensures routines trigger reliably on time in the background",
                category = "Reliability",
                iconName = "BatteryChargingFull",
                isGranted = checkBatteryOptimizationIgnored(context),
                isCriticalForBackground = true,
                actionIntentProvider = { ctx ->
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${ctx.packageName}")
                    }
                }
            ),
            PermissionItem(
                id = "exact_alarms",
                name = "Exact Alarms",
                description = "Enables precise time-based routine scheduling down to the exact second",
                category = "Reliability",
                iconName = "Schedule",
                isGranted = checkExactAlarmPermission(context),
                isCriticalForBackground = true,
                actionIntentProvider = { ctx ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:${ctx.packageName}")
                        }
                    } else {
                        createAppDetailsIntent(ctx)
                    }
                }
            ),
            PermissionItem(
                id = "dnd_policy",
                name = "Do Not Disturb Access",
                description = "Required to toggle Silent and Priority modes automatically without user intervention",
                category = "Audio & Alert",
                iconName = "DoNotDisturb",
                isGranted = checkDndAccess(context),
                isCriticalForBackground = false,
                actionIntentProvider = {
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                }
            ),
            PermissionItem(
                id = "accessibility",
                name = "Accessibility Service",
                description = "Used strictly to detect foreground app launches and automate device gestures like Home/Back",
                category = "Advanced Automation",
                iconName = "Accessibility",
                isGranted = checkAccessibilityAccess(context),
                isCriticalForBackground = false,
                actionIntentProvider = {
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                }
            ),
            PermissionItem(
                id = "notification_listener",
                name = "Notification Listener",
                description = "Detects incoming notifications to trigger rules based on content or sender apps",
                category = "Advanced Automation",
                iconName = "NotificationsActive",
                isGranted = checkNotificationListenerAccess(context),
                isCriticalForBackground = false,
                actionIntentProvider = {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                }
            ),
            PermissionItem(
                id = "write_settings",
                name = "Modify System Settings",
                description = "Needed on supported devices to change display brightness and screen timeout directly",
                category = "Display & Hardware",
                iconName = "BrightnessMedium",
                isGranted = checkWriteSettingsPermission(context),
                isCriticalForBackground = false,
                actionIntentProvider = { ctx ->
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${ctx.packageName}")
                    }
                }
            ),
            PermissionItem(
                id = "bluetooth",
                name = "Bluetooth Nearby Devices",
                description = "Allows detecting when headphones, speakers, or car Bluetooth connect",
                category = "Connectivity",
                iconName = "Bluetooth",
                isGranted = checkBluetoothPermission(context),
                isCriticalForBackground = false,
                actionIntentProvider = { ctx -> createAppDetailsIntent(ctx) }
            ),
            PermissionItem(
                id = "location",
                name = "Location Access",
                description = "Required for geofence triggers (entering / leaving home or work)",
                category = "Location",
                iconName = "LocationOn",
                isGranted = checkLocationPermission(context),
                isCriticalForBackground = false,
                actionIntentProvider = { ctx -> createAppDetailsIntent(ctx) }
            )
        )
    }

    private fun createAppDetailsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
}
