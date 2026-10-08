package com.example.automation.services

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.FlowApplication
import com.example.automation.engine.FlowModeController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FlowTileService : TileService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            FlowModeController.cycleNextMode(this@FlowTileService)
            updateTileState()
        }
    }

    private fun updateTileState() {
        val app = applicationContext as? FlowApplication ?: return
        val currentTile = qsTile ?: return

        serviceScope.launch {
            val modes = withContext(Dispatchers.Default) {
                app.repository.allModes.first()
            }
            val activeMode = modes.firstOrNull { it.isActive }

            if (activeMode != null) {
                currentTile.state = Tile.STATE_ACTIVE
                currentTile.label = "Focus: ${activeMode.name}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    currentTile.subtitle = "Active • Tap to cycle"
                }
            } else {
                currentTile.state = Tile.STATE_INACTIVE
                currentTile.label = "Focus Modes"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    currentTile.subtitle = "Off • Tap to start"
                }
            }

            currentTile.updateTile()
        }
    }
}
