package com.example.automation.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.FlowApplication
import com.example.automation.engine.ConditionEvaluator
import com.example.domain.models.ConditionType

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val app = context.applicationContext as? FlowApplication ?: return
        val routineId = intent?.getStringExtra("routine_id")

        app.automationEngine.triggerAutomations(
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.TIME_RANGE,
                extraData = if (routineId != null) mapOf("routine_id" to routineId) else emptyMap()
            )
        )

        app.automationEngine.triggerAutomations(
            ConditionEvaluator.TriggerContext(
                triggerType = ConditionType.CALENDAR_EVENT
            )
        )

        // Reschedule next check
        app.flowScheduler.scheduleNextEvaluation()
    }
}
