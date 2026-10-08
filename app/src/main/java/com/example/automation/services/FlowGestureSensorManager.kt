package com.example.automation.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.FlowApplication
import com.example.automation.engine.ConditionEvaluator
import com.example.automation.engine.FlowModeController
import com.example.domain.models.ConditionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class PhoneOrientation(val label: String) {
    FACE_UP("Facing Up"),
    FACE_DOWN("Face-Down (Shhh Active)"),
    PORTRAIT("Upright"),
    TILTED("Tilted / Moving")
}

class FlowGestureSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentOrientation = MutableStateFlow(PhoneOrientation.FACE_UP)
    val currentOrientation: StateFlow<PhoneOrientation> = _currentOrientation.asStateFlow()

    private val _lastShakeTime = MutableStateFlow(0L)
    val lastShakeTime: StateFlow<Long> = _lastShakeTime.asStateFlow()

    private var isFaceDown = false
    private var faceDownStartTime = 0L
    private var hasTriggeredFlipForCurrentHold = false

    private var lastProximityNear = false
    private var lastShakeTimestamp = 0L

    var isFlipToShhhEnabled = false
    var isShakeEnabled = false
    var flipTargetModeId = "mode_work"

    fun start() {
        if (sensorManager == null) return
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values.firstOrNull() ?: 10f
                val maxRange = proximitySensor?.maximumRange ?: 5f
                lastProximityNear = distance < maxRange
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                handleOrientation(x, y, z)
                handleShake(x, y, z)
            }
        }
    }

    private fun handleOrientation(x: Float, y: Float, z: Float) {
        val now = System.currentTimeMillis()

        // Z is strongly negative when face down on a flat surface (approx -9.8 m/s²)
        val facingDown = z < -7.0f && Math.abs(x) < 4.5f && Math.abs(y) < 4.5f
        val facingUp = z > 7.0f && Math.abs(x) < 4.5f && Math.abs(y) < 4.5f

        if (facingDown) {
            _currentOrientation.value = PhoneOrientation.FACE_DOWN
            if (!isFaceDown) {
                isFaceDown = true
                faceDownStartTime = now
                hasTriggeredFlipForCurrentHold = false
            } else {
                // Must be held face down steadily for 700ms to prevent accidental triggers
                if (!hasTriggeredFlipForCurrentHold && (now - faceDownStartTime >= 700L)) {
                    hasTriggeredFlipForCurrentHold = true
                    onFlipToShhhTriggered()
                }
            }
        } else {
            isFaceDown = false
            hasTriggeredFlipForCurrentHold = false
            if (facingUp) {
                _currentOrientation.value = PhoneOrientation.FACE_UP
            } else if (Math.abs(y) > 7.0f) {
                _currentOrientation.value = PhoneOrientation.PORTRAIT
            } else {
                _currentOrientation.value = PhoneOrientation.TILTED
            }
        }
    }

    private fun onFlipToShhhTriggered() {
        if (!isFlipToShhhEnabled) return

        scope.launch {
            val app = context.applicationContext as? FlowApplication ?: return@launch
            // 1. Trigger automations for ConditionType.FLIP_TO_SHHH
            app.automationEngine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.FLIP_TO_SHHH,
                    extraData = mapOf("face_down" to true)
                )
            )

            // 2. Activate configured target mode (default: Work)
            FlowModeController.activateMode(context, flipTargetModeId)
        }
    }

    private fun handleShake(x: Float, y: Float, z: Float) {
        val now = System.currentTimeMillis()
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val delta = Math.abs(magnitude - SensorManager.GRAVITY_EARTH)

        // Shake threshold
        if (delta > 14.0f && (now - lastShakeTimestamp > 2500L)) {
            lastShakeTimestamp = now
            _lastShakeTime.value = now
            onShakeTriggered()
        }
    }

    private fun onShakeTriggered() {
        if (!isShakeEnabled) return

        scope.launch {
            val app = context.applicationContext as? FlowApplication ?: return@launch
            app.automationEngine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.SHAKE_GESTURE,
                    extraData = mapOf("shake_detected" to true)
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
