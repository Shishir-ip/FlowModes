package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.automation.engine.ConditionEvaluator
import com.example.automation.engine.ConflictManager
import com.example.data.backup.BackupManager
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.Routine
import com.example.domain.models.RoutinePriority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Before
    fun setup() {
        ConflictManager.resetState()
    }

    @Test
    fun `read string from context matches FlowModes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FlowModes", appName)
    }

    @Test
    fun `condition evaluator handles ALL and ANY logic correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val rId = UUID.randomUUID().toString()

        val cond1 = AutomationCondition(
            id = UUID.randomUUID().toString(),
            routineId = rId,
            type = ConditionType.SCREEN_ON,
            title = "Screen On",
            summary = "Screen on",
            configJson = "{}"
        )

        val cond2 = AutomationCondition(
            id = UUID.randomUUID().toString(),
            routineId = rId,
            type = ConditionType.BATTERY_LEVEL_BELOW,
            title = "Battery Low",
            summary = "Battery below 100%",
            configJson = "{\"threshold\":100}"
        )

        // Trigger context with screen_on = true
        val triggerContext = ConditionEvaluator.TriggerContext(
            triggerType = ConditionType.SCREEN_ON,
            extraData = mapOf("screen_on" to true)
        )

        val (allSatisfied, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = listOf(cond1, cond2),
            logic = ConditionLogic.ALL,
            triggerContext = triggerContext
        )
        assertTrue("All conditions should be satisfied", allSatisfied)

        val (anySatisfied, _) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = listOf(cond1, cond2),
            logic = ConditionLogic.ANY,
            triggerContext = triggerContext
        )
        assertTrue("Any condition should be satisfied", anySatisfied)
    }

    @Test
    fun `conflict manager respects cooldown and priorities`() {
        val rId = UUID.randomUUID().toString()
        val routine = Routine(
            id = rId,
            name = "Test Routine",
            description = "Test",
            iconName = "AutoAwesome",
            colorHex = "#38BDF8",
            isEnabled = true,
            isFavorite = false,
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.CRITICAL,
            cooldownSeconds = 10,
            conditions = emptyList(),
            actions = emptyList()
        )

        val (canFirstRun, _) = ConflictManager.canExecuteRoutine(routine, isManualRun = false)
        assertTrue("First run should be allowed", canFirstRun)

        ConflictManager.recordExecution(routine.id)

        val (canSecondRun, reason) = ConflictManager.canExecuteRoutine(routine, isManualRun = false)
        assertFalse("Second run within cooldown should be blocked", canSecondRun)
        assertTrue("Reason should mention cooldown", reason.contains("Cooldown"))

        // Manual override should ignore cooldown
        val (canManualRun, _) = ConflictManager.canExecuteRoutine(routine, isManualRun = true)
        assertTrue("Manual run should override cooldown", canManualRun)
    }

    @Test
    fun `backup manager exports and imports valid json format`() {
        val rId = UUID.randomUUID().toString()
        val routine = Routine(
            id = rId,
            name = "Export Test Routine",
            description = "Test Description",
            iconName = "Bedtime",
            colorHex = "#818CF8",
            isEnabled = true,
            isFavorite = true,
            conditionLogic = ConditionLogic.ALL,
            priority = RoutinePriority.HIGH,
            cooldownSeconds = 60,
            conditions = listOf(
                AutomationCondition(
                    id = UUID.randomUUID().toString(),
                    routineId = rId,
                    type = ConditionType.WIFI_CONNECTED,
                    title = "Wi-Fi Connected",
                    summary = "Connected to Office",
                    configJson = "{\"ssid\":\"Office\"}"
                )
            ),
            actions = listOf(
                AutomationAction(
                    id = UUID.randomUUID().toString(),
                    routineId = rId,
                    type = ActionType.SET_RINGER_MODE,
                    title = "Silent",
                    summary = "Switch to Vibrate",
                    configJson = "{\"mode\":\"VIBRATE\"}"
                )
            )
        )

        val json = BackupManager.exportRoutinesToJson(listOf(routine))
        assertTrue("Exported JSON should contain routine name", json.contains("Export Test Routine"))
        assertTrue("Exported JSON should contain schema app marker", json.contains("FlowModes"))

        val importResult = BackupManager.importRoutinesFromJson(json)
        assertTrue("Import should succeed", importResult is BackupManager.ImportResult.Success)

        val imported = (importResult as BackupManager.ImportResult.Success).routines
        assertEquals(1, imported.size)
        assertEquals("Export Test Routine", imported.first().name)
        assertEquals(1, imported.first().conditions.size)
        assertEquals(1, imported.first().actions.size)
        // Imported routines should be disabled by default for user review safety
        assertFalse(imported.first().isEnabled)
    }
}
