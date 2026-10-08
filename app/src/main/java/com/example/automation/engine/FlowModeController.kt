package com.example.automation.engine

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import com.example.FlowApplication
import com.example.automation.receivers.FlowWidgetProvider
import com.example.automation.services.FlowActiveModeNotificationManager
import com.example.automation.services.FlowTileService
import com.example.domain.models.ExecutionLog
import com.example.domain.models.Mode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FlowModeController {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun toggleMode(context: Context, modeId: String) {
        val app = context.applicationContext as? FlowApplication ?: return
        scope.launch {
            val modes = app.repository.allModes.first()
            val targetMode = modes.firstOrNull { it.id == modeId } ?: return@launch
            if (targetMode.isActive) {
                deactivateMode(context, modeId)
            } else {
                activateMode(context, modeId)
            }
        }
    }

    suspend fun activateMode(context: Context, modeId: String) = withContext(Dispatchers.Default) {
        val app = context.applicationContext as? FlowApplication ?: return@withContext
        val modes = app.repository.allModes.first()
        val targetMode = modes.firstOrNull { it.id == modeId } ?: return@withContext

        // Mark in database
        app.repository.setModeActive(modeId, true)

        // Execute assigned mode actions
        for (action in targetMode.actions) {
            ActionExecutor.executeAction(context, action)
        }

        // Log execution
        app.repository.recordExecutionLog(
            ExecutionLog(
                routineId = targetMode.id,
                routineName = "${targetMode.name} Mode",
                triggerName = "Mode Activation",
                timestamp = System.currentTimeMillis(),
                isSuccess = true,
                conditionsSummary = "Mode enabled via system controller",
                actionsSummary = targetMode.actions.joinToString("\n") { "✓ ${it.title}" },
                resultSummary = "Active profile applied"
            )
        )

        val updatedMode = targetMode.copy(isActive = true, activatedAt = System.currentTimeMillis())
        syncSystemIntegrations(context, updatedMode)
    }

    suspend fun deactivateMode(context: Context, modeId: String) = withContext(Dispatchers.Default) {
        val app = context.applicationContext as? FlowApplication ?: return@withContext
        app.repository.setModeActive(modeId, false)
        syncSystemIntegrations(context, null)
    }

    suspend fun deactivateAllModes(context: Context) = withContext(Dispatchers.Default) {
        val app = context.applicationContext as? FlowApplication ?: return@withContext
        val modes = app.repository.allModes.first()
        for (m in modes) {
            if (m.isActive) {
                app.repository.setModeActive(m.id, false)
            }
        }
        syncSystemIntegrations(context, null)
    }

    suspend fun cycleNextMode(context: Context) = withContext(Dispatchers.Default) {
        val app = context.applicationContext as? FlowApplication ?: return@withContext
        val modes = app.repository.allModes.first()
        if (modes.isEmpty()) return@withContext

        val currentIndex = modes.indexOfFirst { it.isActive }
        if (currentIndex == -1) {
            // None active -> Activate first mode (e.g. Work)
            activateMode(context, modes.first().id)
        } else if (currentIndex == modes.lastIndex) {
            // Last mode active -> Turn off all
            deactivateAllModes(context)
        } else {
            // Switch to next mode
            activateMode(context, modes[currentIndex + 1].id)
        }
    }

    fun syncSystemIntegrations(context: Context, activeMode: Mode?) {
        val appContext = context.applicationContext

        // 1. Notification
        if (activeMode != null) {
            FlowActiveModeNotificationManager.showActiveModeNotification(appContext, activeMode)
        } else {
            FlowActiveModeNotificationManager.cancelNotification(appContext)
        }

        // 2. Quick Settings Tile
        try {
            TileService.requestListeningState(
                appContext,
                ComponentName(appContext, FlowTileService::class.java)
            )
        } catch (_: Exception) {}

        // 3. App Widget
        FlowWidgetProvider.updateAllWidgets(appContext)
    }
}
