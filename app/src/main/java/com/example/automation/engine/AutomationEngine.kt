package com.example.automation.engine

import android.content.Context
import com.example.data.repository.AutomationRepository
import com.example.domain.models.ActionResult
import com.example.domain.models.ConditionType
import com.example.domain.models.ExecutionLog
import com.example.domain.models.Routine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class AutomationEngine(
    private val context: Context,
    private val repository: AutomationRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Event debouncing cache (prevents duplicate triggers within 300ms)
    private val lastEventTimestamps = ConcurrentHashMap<String, Long>()
    private val eventDebounceWindowMs = 300L

    fun triggerAutomations(triggerContext: ConditionEvaluator.TriggerContext) {
        val eventKey = "${triggerContext.triggerType}_${triggerContext.packageName}_${triggerContext.wifiSsid}"
        val now = System.currentTimeMillis()
        val lastTime = lastEventTimestamps[eventKey] ?: 0L
        if (now - lastTime < eventDebounceWindowMs && !triggerContext.isManualRun) {
            // Debounce rapid duplicate events
            return
        }
        lastEventTimestamps[eventKey] = now

        scope.launch(Dispatchers.Default) {
            val activeRoutines = repository.getActiveRoutines()
            // 18. ROUTINE INDEXING: Pre-filter routines relevant to this event trigger
            val relevantRoutines = filterRelevantRoutines(activeRoutines, triggerContext)
            val sortedRoutines = ConflictManager.sortRoutinesByPriority(relevantRoutines)

            for (routine in sortedRoutines) {
                evaluateAndExecuteRoutine(routine, triggerContext)
            }
        }
    }

    /**
     * Efficient trigger indexing:
     * Only evaluates routines that could match the incoming trigger.
     * Prevents evaluating battery/time routines on notification events, etc.
     */
    private fun filterRelevantRoutines(
        routines: List<Routine>,
        triggerContext: ConditionEvaluator.TriggerContext
    ): List<Routine> {
        if (triggerContext.isManualRun || triggerContext.triggerType == null) {
            return routines
        }

        val eventType = triggerContext.triggerType

        return routines.filter { routine ->
            // If routine has no conditions, it can trigger
            if (routine.conditions.isEmpty()) return@filter true

            // Check if routine has any condition matching this trigger type
            val matchingCondition = routine.conditions.firstOrNull { it.type == eventType }
            if (matchingCondition == null) {
                return@filter false
            }

            // Sub-index by package name for notifications and app opens where configured
            if (eventType == ConditionType.NOTIFICATION_RECEIVED && !triggerContext.packageName.isNullOrBlank()) {
                try {
                    val config = JSONObject(matchingCondition.configJson)
                    val expectedPkg = config.optString("packageName", "")
                    if (expectedPkg.isNotBlank() && !expectedPkg.equals(triggerContext.packageName, ignoreCase = true)) {
                        return@filter false
                    }
                } catch (_: Exception) {}
            }

            true
        }
    }

    suspend fun executeRoutineDirectly(routine: Routine): ExecutionLog {
        return withContext(Dispatchers.Default) {
            val triggerContext = ConditionEvaluator.TriggerContext(
                isManualRun = true
            )
            evaluateAndExecuteRoutine(routine, triggerContext)
        }
    }

    private suspend fun evaluateAndExecuteRoutine(
        routine: Routine,
        triggerContext: ConditionEvaluator.TriggerContext
    ): ExecutionLog {
        val triggerName = if (triggerContext.isManualRun) {
            "Manual Trigger"
        } else {
            triggerContext.triggerType?.displayName ?: "System Event"
        }

        // 1. Conflict & Cooldown Check
        val (canExecute, conflictReason) = ConflictManager.canExecuteRoutine(
            routine,
            triggerContext.isManualRun
        )
        if (!canExecute) {
            val log = ExecutionLog(
                routineId = routine.id,
                routineName = routine.name,
                triggerName = triggerName,
                timestamp = System.currentTimeMillis(),
                isSuccess = false,
                conditionsSummary = "Skipped by conflict manager",
                actionsSummary = "None attempted",
                resultSummary = "Blocked: $conflictReason",
                errorMessage = conflictReason
            )
            repository.recordExecutionLog(log)
            return log
        }

        // 2. Condition Evaluation
        val (isSatisfied, conditionSummary) = ConditionEvaluator.evaluateRoutineConditions(
            context = context,
            conditions = routine.conditions,
            logic = routine.conditionLogic,
            triggerContext = triggerContext
        )

        if (!isSatisfied) {
            return ExecutionLog(
                routineId = routine.id,
                routineName = routine.name,
                triggerName = triggerName,
                timestamp = System.currentTimeMillis(),
                isSuccess = false,
                conditionsSummary = conditionSummary,
                actionsSummary = "Not evaluated",
                resultSummary = "Conditions unsatisfied",
                errorMessage = null
            )
        }

        // 3. Conflict Record & State Snapshot
        ConflictManager.recordExecution(routine.id)
        StateRestorationManager.captureStateBeforeExecution(context, routine.id, routine.actions)

        // 4. Action Execution
        val actionResults = mutableListOf<ActionResult>()
        val actionSummaries = mutableListOf<String>()

        for (action in routine.actions) {
            val result = ActionExecutor.executeAction(context, action)
            actionResults.add(result)
            when (result) {
                is ActionResult.Success -> actionSummaries.add("✓ ${action.title}: ${result.message}")
                is ActionResult.Failure -> actionSummaries.add("✕ ${action.title}: ${result.reason}")
                is ActionResult.Skipped -> actionSummaries.add("- ${action.title}: ${result.reason}")
            }
        }

        val allSuccessful = actionResults.all { it is ActionResult.Success }
        val failureCount = actionResults.count { it is ActionResult.Failure }
        val resultSummary = if (allSuccessful) {
            "All ${actionResults.size} actions succeeded"
        } else {
            "$failureCount of ${actionResults.size} actions encountered restrictions"
        }

        val firstErrorMessage = actionResults.filterIsInstance<ActionResult.Failure>()
            .firstOrNull()?.reason

        val log = ExecutionLog(
            routineId = routine.id,
            routineName = routine.name,
            triggerName = triggerName,
            timestamp = System.currentTimeMillis(),
            isSuccess = allSuccessful,
            conditionsSummary = conditionSummary,
            actionsSummary = actionSummaries.joinToString("\n"),
            resultSummary = resultSummary,
            errorMessage = firstErrorMessage
        )

        // Record stats and logs off main thread
        repository.recordRoutineExecution(routine.id, System.currentTimeMillis())
        repository.recordExecutionLog(log)

        return log
    }

    fun restoreRoutineState(routineId: String): Boolean {
        return StateRestorationManager.restoreState(context, routineId)
    }
}
