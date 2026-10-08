package com.example.automation.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.automation.services.FlowAccessibilityService
import com.example.domain.models.ActionResult
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import org.json.JSONObject

object ActionExecutor {

    private const val CHANNEL_ID = "flowmodes_automations"

    fun executeAction(context: Context, action: AutomationAction): ActionResult {
        return try {
            val config = JSONObject(action.configJson.ifEmpty { "{}" })
            when (action.type) {
                ActionType.SET_MEDIA_VOLUME -> {
                    val streamName = config.optString("stream", "MEDIA").uppercase()
                    val volumePercent = config.optInt("volume", 50)
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        ?: return ActionResult.Failure("Audio service unavailable")

                    val streamType = when (streamName) {
                        "RING" -> AudioManager.STREAM_RING
                        "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
                        "ALARM" -> AudioManager.STREAM_ALARM
                        "CALL" -> AudioManager.STREAM_VOICE_CALL
                        else -> AudioManager.STREAM_MUSIC
                    }

                    val maxVolume = audioManager.getStreamMaxVolume(streamType)
                    val targetVolume = (maxVolume * (volumePercent / 100f)).toInt().coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(streamType, targetVolume, 0)
                    ActionResult.Success("$streamName volume set to $volumePercent% ($targetVolume/$maxVolume)")
                }

                ActionType.SET_RINGER_MODE -> {
                    val targetModeStr = config.optString("mode", "NORMAL").uppercase()
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        ?: return ActionResult.Failure("Audio service unavailable")
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

                    val hasDndAccess = notificationManager?.isNotificationPolicyAccessGranted == true

                    when (targetModeStr) {
                        "VIBRATE" -> {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            ActionResult.Success("Ringer mode set to Vibrate")
                        }
                        "SILENT" -> {
                            if (hasDndAccess) {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                                ActionResult.Success("Ringer mode set to Silent")
                            } else {
                                audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                                ActionResult.Failure(
                                    reason = "Total Silent mode requires Do Not Disturb access. Switched to Vibrate instead.",
                                    canOpenSettings = true,
                                    settingsIntentAction = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                                )
                            }
                        }
                        else -> {
                            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                            ActionResult.Success("Ringer mode set to Normal")
                        }
                    }
                }

                ActionType.SET_BRIGHTNESS -> {
                    val brightnessPercent = config.optInt("brightness", 50)
                    val autoBrightness = config.optBoolean("autoBrightness", false)

                    if (Settings.System.canWrite(context)) {
                        if (autoBrightness) {
                            Settings.System.putInt(
                                context.contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                            )
                            ActionResult.Success("Adaptive Brightness enabled")
                        } else {
                            Settings.System.putInt(
                                context.contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS_MODE,
                                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                            )
                            val brightness255 = (brightnessPercent * 255 / 100).coerceIn(1, 255)
                            Settings.System.putInt(
                                context.contentResolver,
                                Settings.System.SCREEN_BRIGHTNESS,
                                brightness255
                            )
                            ActionResult.Success("Screen brightness set to $brightnessPercent%")
                        }
                    } else {
                        ActionResult.Failure(
                            reason = "Screen brightness adjustment requires 'Modify System Settings' permission.",
                            canOpenSettings = true,
                            settingsIntentAction = Settings.ACTION_MANAGE_WRITE_SETTINGS
                        )
                    }
                }

                ActionType.SHOW_NOTIFICATION -> {
                    val title = config.optString("title", "FlowModes Alert")
                    val message = config.optString("message", "Automation executed successfully")
                    val priorityStr = config.optString("priority", "DEFAULT").uppercase()
                    showNotification(context, title, message, priorityStr)
                    ActionResult.Success("Notification posted: \"$title\"")
                }

                ActionType.LAUNCH_APP -> {
                    val pkg = config.optString("packageName", "")
                    if (pkg.isNotEmpty()) {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                            ActionResult.Success("Launched application: $pkg")
                        } else {
                            ActionResult.Failure("Application $pkg is not installed on this device")
                        }
                    } else {
                        ActionResult.Failure("No package name configured for app launch")
                    }
                }

                ActionType.OPEN_URL -> {
                    val url = config.optString("url", "https://android.com")
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    ActionResult.Success("Opened web link: $url")
                }

                ActionType.TOGGLE_FLASHLIGHT -> {
                    val enableTorch = config.optBoolean("enable", true)
                    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                    val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                    if (cameraManager != null && cameraId != null) {
                        try {
                            cameraManager.setTorchMode(cameraId, enableTorch)
                            ActionResult.Success("Flashlight set to ${if (enableTorch) "ON" else "OFF"}")
                        } catch (e: Exception) {
                            ActionResult.Failure("Camera flash unavailable: ${e.localizedMessage}")
                        }
                    } else {
                        ActionResult.Failure("Device has no available flashlight camera")
                    }
                }

                ActionType.TRIGGER_VIBRATION -> {
                    val pattern = config.optString("pattern", "DOUBLE_PULSE").uppercase()
                    triggerHapticVibration(context, pattern)
                    ActionResult.Success("Haptic vibration pattern triggered ($pattern)")
                }

                ActionType.OPEN_SETTINGS -> {
                    val targetSettings = config.optString("target", "SETTINGS").uppercase()
                    val intent = when (targetSettings) {
                        "WIFI" -> Intent(Settings.ACTION_WIFI_SETTINGS)
                        "BLUETOOTH" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        "BATTERY" -> Intent(Intent.ACTION_POWER_USAGE_SUMMARY)
                        "DISPLAY" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
                        "SOUND" -> Intent(Settings.ACTION_SOUND_SETTINGS)
                        "DND" -> Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        "LOCATION" -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        else -> Intent(Settings.ACTION_SETTINGS)
                    }.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                    ActionResult.Success("Opened $targetSettings settings")
                }

                ActionType.SET_DND_MODE -> {
                    val targetMode = config.optString("mode", "PRIORITY").uppercase()
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    if (notificationManager?.isNotificationPolicyAccessGranted == true) {
                        val filter = when (targetMode) {
                            "TOTAL_SILENCE" -> NotificationManager.INTERRUPTION_FILTER_NONE
                            "ALARMS_ONLY" -> NotificationManager.INTERRUPTION_FILTER_ALARMS
                            "OFF" -> NotificationManager.INTERRUPTION_FILTER_ALL
                            else -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        }
                        notificationManager.setInterruptionFilter(filter)
                        ActionResult.Success("Do Not Disturb set to $targetMode")
                    } else {
                        ActionResult.Failure(
                            reason = "Direct DND control requires Do Not Disturb / Notification Policy Access.",
                            canOpenSettings = true,
                            settingsIntentAction = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        )
                    }
                }

                ActionType.ACCESSIBILITY_NAV -> {
                    val navAction = config.optString("action", "HOME").uppercase()
                    val service = FlowAccessibilityService.instance
                    if (service != null) {
                        val success = when (navAction) {
                            "BACK" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                            "HOME" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                            "LOCK" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                            "QUICK_SETTINGS" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                            "NOTIFICATIONS" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                            else -> false
                        }
                        if (success) {
                            ActionResult.Success("Executed accessibility navigation: $navAction")
                        } else {
                            ActionResult.Failure("Accessibility action $navAction returned false")
                        }
                    } else {
                        ActionResult.Failure(
                            reason = "Requires Accessibility Access to automate device navigation.",
                            canOpenSettings = true,
                            settingsIntentAction = Settings.ACTION_ACCESSIBILITY_SETTINGS
                        )
                    }
                }
            }
        } catch (e: Exception) {
            ActionResult.Failure("Execution error: ${e.localizedMessage ?: "Unknown exception"}")
        }
    }

    private fun showNotification(context: Context, title: String, message: String, priorityStr: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val importance = when (priorityStr) {
            "HIGH" -> NotificationManager.IMPORTANCE_HIGH
            "LOW" -> NotificationManager.IMPORTANCE_LOW
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FlowModes Automation Alerts",
                importance
            ).apply {
                description = "Notifications triggered by FlowModes routines"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val compatPriority = when (priorityStr) {
            "HIGH" -> NotificationCompat.PRIORITY_HIGH
            "LOW" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(compatPriority)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun triggerHapticVibration(context: Context, pattern: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val vibrator = vm?.defaultVibrator ?: return
            when (pattern) {
                "CLICK" -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                "HEAVY" -> vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                "SOS" -> {
                    val timings = longArrayOf(0, 100, 100, 100, 100, 100, 200, 300, 100, 300, 100, 300, 200, 100, 100, 100, 100, 100)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
                else -> { // DOUBLE_PULSE or ALERT
                    val timings = longArrayOf(0, 120, 80, 160)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(150)
        }
    }
}
