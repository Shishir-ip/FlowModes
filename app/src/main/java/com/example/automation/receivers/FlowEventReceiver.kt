package com.example.automation.receivers

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.example.FlowApplication
import com.example.automation.engine.ConditionEvaluator
import com.example.domain.models.ConditionType

class FlowEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val app = context.applicationContext as? FlowApplication ?: return
        val engine = app.automationEngine

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.CHARGING_STARTED
                    )
                )
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.CHARGING_STOPPED
                    )
                )
            }
            Intent.ACTION_BATTERY_LOW -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.BATTERY_LEVEL_BELOW
                    )
                )
            }
            Intent.ACTION_HEADSET_PLUG -> {
                val state = intent.getIntExtra("state", -1)
                val isPlugged = state == 1
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = if (isPlugged) ConditionType.HEADSET_CONNECTED else ConditionType.HEADSET_DISCONNECTED,
                        extraData = mapOf("headset_connected" to isPlugged)
                    )
                )
            }
            Intent.ACTION_SCREEN_ON -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.SCREEN_ON,
                        extraData = mapOf("screen_on" to true)
                    )
                )
            }
            Intent.ACTION_SCREEN_OFF -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.SCREEN_OFF,
                        extraData = mapOf("screen_on" to false)
                    )
                )
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.DEVICE_BOOT
                    )
                )
                app.flowScheduler.scheduleNextEvaluation()
            }
            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                engine.triggerAutomations(
                    ConditionEvaluator.TriggerContext(
                        triggerType = ConditionType.WIFI_CONNECTED
                    )
                )
            }
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    engine.triggerAutomations(
                        ConditionEvaluator.TriggerContext(
                            triggerType = ConditionType.BLUETOOTH_CONNECTED
                        )
                    )
                }
            }
        }
    }
}
