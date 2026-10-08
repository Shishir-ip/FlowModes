package com.example.automation.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.FlowApplication
import com.example.MainActivity
import com.example.R
import com.example.automation.engine.FlowModeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_MODE = "com.example.action.WIDGET_TOGGLE_MODE"
        const val ACTION_DEACTIVATE_ALL = "com.example.action.WIDGET_DEACTIVATE_ALL"
        const val EXTRA_MODE_ID = "extra_mode_id"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, FlowWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, FlowWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as? FlowApplication ?: return
        CoroutineScope(Dispatchers.Default).launch {
            val modes = app.repository.allModes.first()
            val activeMode = modes.firstOrNull { it.isActive }

            withContext(Dispatchers.Main) {
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.flow_widget_layout)

                    // Click header to open App
                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val appPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_header, appPendingIntent)

                    // Bind Status Text & Subtext
                    if (activeMode != null) {
                        views.setTextViewText(R.id.widget_status_badge, "● ${activeMode.name} Active")
                        views.setTextViewText(R.id.widget_subtext, "Focus mode engaged • Tap below to switch")
                    } else {
                        views.setTextViewText(R.id.widget_status_badge, "○ Inactive")
                        views.setTextViewText(R.id.widget_subtext, "Tap any mode below to start focusing")
                    }

                    // Button 1: Work Mode
                    views.setOnClickPendingIntent(
                        R.id.widget_btn_work,
                        createModeToggleIntent(context, "mode_work", 1)
                    )

                    // Button 2: Study Mode
                    views.setOnClickPendingIntent(
                        R.id.widget_btn_study,
                        createModeToggleIntent(context, "mode_study", 2)
                    )

                    // Button 3: Sleep Mode
                    views.setOnClickPendingIntent(
                        R.id.widget_btn_sleep,
                        createModeToggleIntent(context, "mode_sleep", 3)
                    )

                    // Button 4: Turn Off
                    val turnOffIntent = Intent(context, FlowWidgetProvider::class.java).apply {
                        action = ACTION_DEACTIVATE_ALL
                    }
                    val turnOffPendingIntent = PendingIntent.getBroadcast(
                        context,
                        4,
                        turnOffIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_btn_off, turnOffPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun createModeToggleIntent(context: Context, modeId: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, FlowWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_MODE
            putExtra(EXTRA_MODE_ID, modeId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent == null) return

        when (intent.action) {
            ACTION_TOGGLE_MODE -> {
                val modeId = intent.getStringExtra(EXTRA_MODE_ID) ?: return
                FlowModeController.toggleMode(context, modeId)
            }
            ACTION_DEACTIVATE_ALL -> {
                CoroutineScope(Dispatchers.Default).launch {
                    FlowModeController.deactivateAllModes(context)
                }
            }
        }
    }
}
