package com.example.automation.services

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.FlowApplication
import com.example.automation.engine.ConditionEvaluator
import com.example.domain.models.ConditionType

class FlowAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return
            val app = applicationContext as? FlowApplication ?: return
            app.automationEngine.triggerAutomations(
                ConditionEvaluator.TriggerContext(
                    triggerType = ConditionType.APP_OPENED,
                    extraData = mapOf("packageName" to pkgName)
                )
            )
        }
    }

    override fun onInterrupt() {
        // Required callback for accessibility interruptions
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        var instance: FlowAccessibilityService? = null
            private set
    }
}
