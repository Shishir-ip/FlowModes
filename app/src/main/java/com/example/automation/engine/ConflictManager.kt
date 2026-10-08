package com.example.automation.engine

import com.example.domain.models.Routine
import java.util.concurrent.ConcurrentHashMap

object ConflictManager {
    // Routine ID -> last execution timestamp in millis
    private val lastExecutedMap = ConcurrentHashMap<String, Long>()

    // Sliding window of executed routine IDs in the last 15 seconds for loop detection
    private val executionHistoryWindow = mutableListOf<Pair<String, Long>>()
    private const val MAX_LOOP_CHAIN_SIZE = 6
    private const val LOOP_WINDOW_MS = 15000L

    @Synchronized
    fun canExecuteRoutine(routine: Routine, isManualRun: Boolean): Pair<Boolean, String> {
        if (isManualRun) {
            return Pair(true, "Manual run permitted")
        }

        val currentTime = System.currentTimeMillis()

        // 1. Cooldown Check
        val lastExecuted = lastExecutedMap[routine.id]
        if (lastExecuted != null) {
            val elapsedSeconds = (currentTime - lastExecuted) / 1000
            if (elapsedSeconds < routine.cooldownSeconds) {
                val remaining = routine.cooldownSeconds - elapsedSeconds
                return Pair(false, "Cooldown active ($remaining seconds remaining)")
            }
        }

        // 2. Loop Protection
        cleanupOldWindowEvents(currentTime)
        val occurrencesInWindow = executionHistoryWindow.count { it.first == routine.id }
        if (occurrencesInWindow >= 3 || executionHistoryWindow.size >= MAX_LOOP_CHAIN_SIZE) {
            return Pair(false, "Loop protection triggered (excessive cascading triggers in last 15s)")
        }

        return Pair(true, "Execution permitted")
    }

    @Synchronized
    fun recordExecution(routineId: String) {
        val now = System.currentTimeMillis()
        lastExecutedMap[routineId] = now
        executionHistoryWindow.add(Pair(routineId, now))
        cleanupOldWindowEvents(now)
    }

    private fun cleanupOldWindowEvents(now: Long) {
        executionHistoryWindow.removeAll { now - it.second > LOOP_WINDOW_MS }
    }

    fun sortRoutinesByPriority(routines: List<Routine>): List<Routine> {
        return routines.sortedWith(
            compareByDescending<Routine> { it.priority.level }
                .thenByDescending { it.createdAt }
        )
    }

    fun resetState() {
        lastExecutedMap.clear()
        executionHistoryWindow.clear()
    }
}
