package com.example.automation.engine

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class RoutineStateSnapshot(
    val routineId: String,
    val capturedAt: Long = System.currentTimeMillis(),
    val originalMediaVolume: Int? = null,
    val originalRingerMode: Int? = null,
    val originalBrightness: Int? = null,
    val originalFlashlight: Boolean? = null
)

object StateRestorationManager {
    private val activeSnapshots = ConcurrentHashMap<String, RoutineStateSnapshot>()

    fun captureStateBeforeExecution(
        context: Context,
        routineId: String,
        actions: List<AutomationAction>
    ) {
        val reversibleActions = actions.filter { it.restoreOnExit }
        if (reversibleActions.isEmpty()) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        var mediaVolume: Int? = null
        var ringerMode: Int? = null
        var brightness: Int? = null
        var flashlight: Boolean? = null

        for (action in reversibleActions) {
            when (action.type) {
                ActionType.SET_MEDIA_VOLUME -> {
                    if (mediaVolume == null && audioManager != null) {
                        mediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    }
                }
                ActionType.SET_RINGER_MODE, ActionType.SET_DND_MODE -> {
                    if (ringerMode == null && audioManager != null) {
                        ringerMode = audioManager.ringerMode
                    }
                }
                ActionType.SET_BRIGHTNESS -> {
                    if (brightness == null) {
                        brightness = try {
                            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
                ActionType.TOGGLE_FLASHLIGHT -> {
                    flashlight = false // Default assumption prior to turning on
                }
                else -> Unit
            }
        }

        // Only store if we captured at least one restorable parameter and don't already have one
        if (!activeSnapshots.containsKey(routineId)) {
            activeSnapshots[routineId] = RoutineStateSnapshot(
                routineId = routineId,
                originalMediaVolume = mediaVolume,
                originalRingerMode = ringerMode,
                originalBrightness = brightness,
                originalFlashlight = flashlight
            )
        }
    }

    fun hasSnapshot(routineId: String): Boolean = activeSnapshots.containsKey(routineId)

    fun getSnapshot(routineId: String): RoutineStateSnapshot? = activeSnapshots[routineId]

    fun restoreState(context: Context, routineId: String): Boolean {
        val snapshot = activeSnapshots.remove(routineId) ?: return false
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        // 1. Restore Media Volume
        snapshot.originalMediaVolume?.let { volume ->
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }

        // 2. Restore Ringer Mode
        snapshot.originalRingerMode?.let { mode ->
            audioManager?.let { am ->
                try {
                    am.ringerMode = mode
                } catch (_: Exception) {
                    // Ignored if DND policy blocks changing
                }
            }
        }

        // 3. Restore Brightness
        snapshot.originalBrightness?.let { b ->
            if (Settings.System.canWrite(context)) {
                try {
                    Settings.System.putInt(
                        context.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS,
                        b
                    )
                } catch (_: Exception) {}
            }
        }

        // 4. Restore Torch
        snapshot.originalFlashlight?.let { state ->
            try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                val cameraId = cameraManager?.cameraIdList?.firstOrNull()
                if (cameraId != null) {
                    cameraManager.setTorchMode(cameraId, state)
                }
            } catch (_: Exception) {}
        }

        return true
    }

    fun clearAll() {
        activeSnapshots.clear()
    }
}
