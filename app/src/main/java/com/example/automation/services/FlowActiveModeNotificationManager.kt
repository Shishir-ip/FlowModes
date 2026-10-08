package com.example.automation.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.automation.receivers.FlowNotificationActionReceiver
import com.example.domain.models.Mode

object FlowActiveModeNotificationManager {

    private const val CHANNEL_ID = "flowmodes_active_mode"
    private const val NOTIFICATION_ID = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Active Focus Mode"
            val descriptionText = "Persistent lock screen status and quick actions for active Focus Modes"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                enableVibration(false)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showActiveModeNotification(context: Context, mode: Mode) {
        createNotificationChannel(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            100,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: End Mode Early
        val endModeIntent = Intent(context, FlowNotificationActionReceiver::class.java).apply {
            action = FlowNotificationActionReceiver.ACTION_END_MODE
            putExtra(FlowNotificationActionReceiver.EXTRA_MODE_ID, mode.id)
        }
        val endModePendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            endModeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: Mute for 30m / DND
        val muteIntent = Intent(context, FlowNotificationActionReceiver::class.java).apply {
            action = FlowNotificationActionReceiver.ACTION_MUTE_30M
        }
        val mutePendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val accentColor = try {
            Color.parseColor(mode.colorHex)
        } catch (_: Exception) {
            Color.parseColor("#38BDF8")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("Active Focus: ${mode.name}")
            .setContentText(if (mode.description.isNotBlank()) mode.description else "Distractions muted • Tap to configure")
            .setSubText("FlowModes Ongoing")
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(accentColor)
            .setColorized(true)
            .setShowWhen(true)
            .setWhen(mode.activatedAt ?: System.currentTimeMillis())
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "End Early", endModePendingIntent)
            .addAction(android.R.drawable.stat_notify_chat, "Mute 30m", mutePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }
}
