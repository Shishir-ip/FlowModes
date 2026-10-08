package com.example.util

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.Window
import android.view.WindowManager

/**
 * Utility to query and request high-refresh-rate display modes (90Hz, 120Hz, etc.)
 * in full cooperation with Android's display scheduler and adaptive refresh rate.
 *
 * Does not force a fixed rate, does not poll, and does not fight system thermal/battery limits.
 */
object DisplayRefreshRateHelper {

    data class RefreshRateInfo(
        val currentRefreshRate: Float,
        val maxSupportedRefreshRate: Float,
        val isHighRefreshRateSupported: Boolean,
        val supportedRates: List<Float>
    )

    fun getRefreshRateInfo(context: Context): RefreshRateInfo {
        return try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context is Activity) {
                context.display
            } else {
                displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
            }

            if (display == null) {
                return RefreshRateInfo(60f, 60f, false, listOf(60f))
            }

            val currentRate = display.refreshRate
            val supportedModes = display.supportedModes ?: emptyArray()
            val supportedRates = supportedModes.map { it.refreshRate }.distinct().sorted()
            val maxRate = supportedRates.maxOrNull() ?: currentRate

            RefreshRateInfo(
                currentRefreshRate = currentRate,
                maxSupportedRefreshRate = maxRate,
                isHighRefreshRateSupported = maxRate > 65f,
                supportedRates = if (supportedRates.isEmpty()) listOf(currentRate) else supportedRates
            )
        } catch (_: Exception) {
            RefreshRateInfo(60f, 60f, false, listOf(60f))
        }
    }

    /**
     * Configures the window to negotiate the highest practical refresh rate supported by the hardware,
     * while allowing Android's adaptive refresh rate (VRR) to scale freely.
     */
    fun optimizeWindowForHighRefreshRate(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val window = activity.window
                val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    activity.display
                } else {
                    @Suppress("DEPRECATION")
                    window.windowManager.defaultDisplay
                } ?: return

                val supportedModes = display.supportedModes ?: return
                if (supportedModes.isEmpty()) return

                val currentMode = display.mode
                // Find mode that preserves resolution but provides the highest supported refresh rate
                val bestMode = supportedModes
                    .filter { it.physicalWidth == currentMode.physicalWidth && it.physicalHeight == currentMode.physicalHeight }
                    .maxByOrNull { it.refreshRate }

                if (bestMode != null && bestMode.refreshRate > currentMode.refreshRate) {
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = bestMode.modeId
                    window.attributes = layoutParams
                }
            } catch (_: Exception) {
                // Graceful fallback on devices that restrict display mode switching
            }
        }
    }
}
