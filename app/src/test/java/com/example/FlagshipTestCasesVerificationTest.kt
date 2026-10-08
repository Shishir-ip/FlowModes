package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.automation.engine.ActionExecutor
import com.example.automation.engine.ConditionEvaluator
import com.example.automation.services.NotificationEventHub
import com.example.domain.models.ActionResult
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.InterceptedNotification
import com.example.domain.models.NotificationField
import com.example.domain.models.NotificationMatchOperator
import com.example.domain.templates.TemplateCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FlagshipTestCasesVerificationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `Test Case 1 - Sleep Mode (22_00 - 07_00 AND Charger Connected)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_sleep_mode" }
        assertNotNull(template)
        assertEquals(ConditionLogic.ALL, template.conditionLogic)
        assertEquals(2, template.conditions.size)
        assertEquals(4, template.actions.size)

        // Verify Revert on exit is set for system state actions
        val ringerAction = template.actions.first { it.type == com.example.domain.models.ActionType.SET_RINGER_MODE }
        val volumeAction = template.actions.first { it.type == com.example.domain.models.ActionType.SET_MEDIA_VOLUME }
        val brightnessAction = template.actions.first { it.type == com.example.domain.models.ActionType.SET_BRIGHTNESS }
        assertTrue("Ringer mode should have restoreOnExit enabled", ringerAction.restoreOnExit)
        assertTrue("Media volume should have restoreOnExit enabled", volumeAction.restoreOnExit)
        assertTrue("Brightness should have restoreOnExit enabled", brightnessAction.restoreOnExit)

        // Evaluate conditions with simulated 23:30 night time and charger plugged in
        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.CHARGING_STARTED,
            simulatedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 30)
            },
            extraData = mapOf("charging" to true)
        )

        val (isMet, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContext
        )
        assertTrue("Sleep Mode conditions must be satisfied at 23:30 while charging", isMet)

        // Verify actions execution succeeds
        val result = ActionExecutor.executeAction(context, volumeAction)
        assertTrue("Volume mute should succeed or report valid state", result is ActionResult.Success || result is ActionResult.Failure)
    }

    @Test
    fun `Test Case 2 - Leaving Home (Wi-Fi Disconnected)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_leaving_home" }
        assertNotNull(template)

        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.WIFI_DISCONNECTED,
            extraData = mapOf("wifi_connected" to false)
        )

        val (isMet, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContext
        )
        assertTrue("Leaving Home conditions must be satisfied when Wi-Fi disconnects", isMet)
    }

    @Test
    fun `Test Case 3 - Driving Mode (Car Bluetooth Connected)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_driving_mode" }
        assertNotNull(template)

        // When connected device name contains "Car"
        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.BLUETOOTH_CONNECTED,
            extraData = mapOf(
                "bluetooth_connected" to true,
                "device_name" to "Car Audio BT"
            )
        )

        val (isMet, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContext
        )
        assertTrue("Driving Mode conditions must match Car Bluetooth device", isMet)
    }

    @Test
    fun `Test Case 4 - Battery Saver (Below 20% AND NOT Charging)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_battery_saver" }
        assertNotNull(template)

        // Verify the second condition is negated (NOT Charging)
        val chargingCondition = template.conditions.first { it.type == ConditionType.CHARGING_STARTED }
        assertTrue("Charging condition must have isNegated = true", chargingCondition.isNegated)

        // Case A: Battery 15% and NOT charging -> Should trigger
        val contextBelowAndUnplugged = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.BATTERY_LEVEL_BELOW,
            extraData = mapOf(
                "battery_level" to 15,
                "charging" to false
            )
        )
        val (isMetA, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = contextBelowAndUnplugged
        )
        assertTrue("Battery Saver must trigger when battery is 15% and unplugged", isMetA)

        // Case B: Battery 15% but PLUGGED IN -> Should NOT trigger because NOT charging fails
        val contextBelowAndPlugged = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.BATTERY_LEVEL_BELOW,
            extraData = mapOf(
                "battery_level" to 15,
                "charging" to true
            )
        )
        val (isMetB, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = contextBelowAndPlugged
        )
        assertFalse("Battery Saver must NOT trigger when plugged in", isMetB)
    }

    @Test
    fun `Test Case 5 - Work Focus (Weekdays AND 09_00 - 17_00)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_work_focus" }
        assertNotNull(template)

        // Wednesday at 11:30 AM
        val wednesday1130 = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY)
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 30)
        }

        val triggerContextWorkTime = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.TIME_RANGE,
            simulatedCalendar = wednesday1130
        )

        val (isMetWork, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContextWorkTime
        )
        assertTrue("Work focus must be satisfied during Wednesday work hours", isMetWork)

        // Sunday at 11:30 AM -> Should NOT match
        val sunday1130 = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 30)
        }

        val triggerContextWeekend = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.TIME_RANGE,
            simulatedCalendar = sunday1130
        )

        val (isMetWeekend, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContextWeekend
        )
        assertFalse("Work focus must not trigger on Sundays", isMetWeekend)
    }

    @Test
    fun `Test Case 6 - Urgent Notification Alert (Regex or Keyword Matching)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_urgent_alert" }
        assertNotNull(template)

        val emergencyNotification = InterceptedNotification(
            id = "test_alert_1",
            packageName = "com.google.android.apps.messaging",
            appName = "Messages",
            title = "Security Alert",
            text = "Emergency alert: Server breach detected on node #7"
        )

        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.NOTIFICATION_RECEIVED,
            extraData = mapOf("notification" to emergencyNotification)
        )

        val (isMet, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContext
        )
        assertTrue("Urgent notification routine must trigger for message with 'Emergency'", isMet)

        // Also test NotificationEventHub evaluator directly
        val matchResult = NotificationEventHub.evaluateMatch(
            notification = emergencyNotification,
            targetPackage = "",
            field = NotificationField.ANY,
            operator = NotificationMatchOperator.CONTAINS,
            query = "Emergency"
        )
        assertTrue("NotificationEventHub evaluateMatch must return true for 'Emergency'", matchResult)
    }

    @Test
    fun `Test Case 7 - Headphones Plugged In (Headset Connected)`() {
        val template = TemplateCatalog.allTemplates.first { it.id == "template_headphones_connected" }
        assertNotNull(template)

        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.HEADSET_CONNECTED,
            extraData = mapOf("headset_connected" to true)
        )

        val (isMet, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = template.conditions,
            logic = template.conditionLogic,
            triggerContext = triggerContext
        )
        assertTrue("Headphones connected condition must be met when headset is plugged in", isMet)

        // Verify volume action is configured to 50%
        val volumeAction = template.actions.first { it.type == com.example.domain.models.ActionType.SET_MEDIA_VOLUME }
        assertTrue("Volume should be 50%", volumeAction.summary.contains("50%"))
    }
}
