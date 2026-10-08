package com.example.automation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast
import com.example.automation.engine.FlowModeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlowNotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_END_MODE = "com.example.action.NOTIFICATION_END_MODE"
        const val ACTION_MUTE_30M = "com.example.action.NOTIFICATION_MUTE_30M"
        const val EXTRA_MODE_ID = "extra_mode_id"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (action) {
                    ACTION_END_MODE -> {
                        val modeId = intent.getStringExtra(EXTRA_MODE_ID)
                        if (modeId != null) {
                            FlowModeController.deactivateMode(context, modeId)
                        } else {
                            FlowModeController.deactivateAllModes(context)
                        }
                    }
                    ACTION_MUTE_30M -> {
                        // Apply mute/vibrate
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        audioManager?.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
