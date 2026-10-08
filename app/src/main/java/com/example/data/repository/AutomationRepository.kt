package com.example.data.repository

import com.example.data.database.FlowDatabase
import com.example.data.database.dao.RoutineWithDetails
import com.example.data.database.entities.ActionEntity
import com.example.data.database.entities.ConditionEntity
import com.example.data.database.entities.ExecutionLogEntity
import com.example.data.database.entities.ModeEntity
import com.example.data.database.entities.RoutineEntity
import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.ExecutionLog
import com.example.domain.models.Mode
import com.example.domain.models.Routine
import com.example.domain.models.RoutinePriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AutomationRepository(private val database: FlowDatabase) {
    private val routineDao = database.routineDao()
    private val modeDao = database.modeDao()
    private val logDao = database.executionLogDao()

    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default).distinctUntilChanged()

    val allModes: Flow<List<Mode>> = modeDao.getAllModes().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default).distinctUntilChanged()

    val recentLogs: Flow<List<ExecutionLog>> = logDao.getRecentLogs().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.Default).distinctUntilChanged()

    suspend fun getRoutineById(id: String): Routine? {
        return routineDao.getRoutineById(id)?.toDomain()
    }

    suspend fun getActiveRoutines(): List<Routine> {
        return routineDao.getActiveRoutines().map { it.toDomain() }
    }

    suspend fun upsertRoutine(routine: Routine) {
        val routineEntity = RoutineEntity(
            id = routine.id,
            name = routine.name,
            description = routine.description,
            iconName = routine.iconName,
            colorHex = routine.colorHex,
            isEnabled = routine.isEnabled,
            isFavorite = routine.isFavorite,
            conditionLogic = routine.conditionLogic.name,
            priority = routine.priority.level,
            cooldownSeconds = routine.cooldownSeconds,
            lastTriggeredTime = routine.lastTriggeredTime,
            triggerCount = routine.triggerCount,
            createdAt = routine.createdAt
        )

        val conditionEntities = routine.conditions.mapIndexed { index, c ->
            ConditionEntity(
                id = c.id.ifEmpty { UUID.randomUUID().toString() },
                routineId = routine.id,
                type = c.type.name,
                title = c.title,
                summary = c.summary,
                configJson = c.configJson,
                orderIndex = index,
                isNegated = c.isNegated
            )
        }

        val actionEntities = routine.actions.mapIndexed { index, a ->
            ActionEntity(
                id = a.id.ifEmpty { UUID.randomUUID().toString() },
                routineId = routine.id,
                type = a.type.name,
                title = a.title,
                summary = a.summary,
                configJson = a.configJson,
                orderIndex = index,
                restoreOnExit = a.restoreOnExit
            )
        }

        routineDao.upsertCompleteRoutine(routineEntity, conditionEntities, actionEntities)
    }

    suspend fun setRoutineEnabled(id: String, isEnabled: Boolean) {
        routineDao.setRoutineEnabled(id, isEnabled)
    }

    suspend fun setRoutineFavorite(id: String, isFavorite: Boolean) {
        routineDao.setRoutineFavorite(id, isFavorite)
    }

    suspend fun recordRoutineExecution(id: String, timestamp: Long) {
        routineDao.recordRoutineExecution(id, timestamp)
    }

    suspend fun deleteRoutine(id: String) {
        routineDao.deleteRoutine(id)
    }

    suspend fun duplicateRoutine(source: Routine) {
        val newId = UUID.randomUUID().toString()
        val duplicated = source.copy(
            id = newId,
            name = "${source.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            triggerCount = 0,
            lastTriggeredTime = null,
            conditions = source.conditions.map { it.copy(id = UUID.randomUUID().toString(), routineId = newId) },
            actions = source.actions.map { it.copy(id = UUID.randomUUID().toString(), routineId = newId) }
        )
        upsertRoutine(duplicated)
    }

    suspend fun setModeActive(id: String, isActive: Boolean) {
        if (isActive) {
            modeDao.deactivateOtherModes(id)
            modeDao.setModeActive(id, true, System.currentTimeMillis())
        } else {
            modeDao.setModeActive(id, false, null)
        }
    }

    suspend fun upsertMode(mode: Mode) {
        val actionsArray = JSONArray()
        mode.actions.forEach { a ->
            val obj = JSONObject().apply {
                put("type", a.type.name)
                put("title", a.title)
                put("summary", a.summary)
                put("configJson", a.configJson)
            }
            actionsArray.put(obj)
        }
        val entity = ModeEntity(
            id = mode.id,
            name = mode.name,
            description = mode.description,
            iconName = mode.iconName,
            colorHex = mode.colorHex,
            isActive = mode.isActive,
            actionsJson = actionsArray.toString(),
            activatedAt = mode.activatedAt
        )
        modeDao.insertMode(entity)
    }

    suspend fun recordExecutionLog(log: ExecutionLog) {
        logDao.insertLog(
            ExecutionLogEntity(
                routineId = log.routineId,
                routineName = log.routineName,
                triggerName = log.triggerName,
                timestamp = log.timestamp,
                isSuccess = log.isSuccess,
                conditionsSummary = log.conditionsSummary,
                actionsSummary = log.actionsSummary,
                resultSummary = log.resultSummary,
                errorMessage = log.errorMessage
            )
        )
    }

    suspend fun clearLogs() {
        logDao.clearAllLogs()
    }

    suspend fun initializeDefaultDataIfEmpty() {
        // Prepopulate modes if none exist
        val defaultModes = listOf(
            Mode(
                id = "mode_work",
                name = "Work",
                description = "Focus on tasks with muted distractions and optimized sound",
                iconName = "Work",
                colorHex = "#38BDF8", // Cyan / Blue
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_work",
                        type = ActionType.SET_RINGER_MODE,
                        title = "Vibrate Mode",
                        summary = "Set sound mode to Vibrate",
                        configJson = "{\"mode\":\"VIBRATE\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_work",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Media Volume 20%",
                        summary = "Lower media volume to 20%",
                        configJson = "{\"volume\":20}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_work",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Work Mode Activated",
                        summary = "Focus profile engaged",
                        configJson = "{\"title\":\"Work Mode\",\"message\":\"Focus profile active\"}"
                    )
                )
            ),
            Mode(
                id = "mode_study",
                name = "Study",
                description = "Deep concentration with silent alerts and calm lighting",
                iconName = "School",
                colorHex = "#818CF8", // Indigo
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_study",
                        type = ActionType.SET_RINGER_MODE,
                        title = "Silent Alerts",
                        summary = "Set ringer mode to Silent",
                        configJson = "{\"mode\":\"SILENT\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_study",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Study Session",
                        summary = "Notifications silenced for study",
                        configJson = "{\"title\":\"Study Mode\",\"message\":\"Deep study session active\"}"
                    )
                )
            ),
            Mode(
                id = "mode_sleep",
                name = "Sleep",
                description = "Night rest with zero disturbance and dimmed brightness",
                iconName = "Bedtime",
                colorHex = "#A78BFA", // Violet
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_sleep",
                        type = ActionType.SET_RINGER_MODE,
                        title = "Mute Ringtone",
                        summary = "Mute phone ringtone",
                        configJson = "{\"mode\":\"SILENT\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_sleep",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Mute Media",
                        summary = "Set volume to 0%",
                        configJson = "{\"volume\":0}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_sleep",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Sleep Well",
                        summary = "Night routine active",
                        configJson = "{\"title\":\"Sleep Mode Active\",\"message\":\"Good night! Disturbances silenced.\"}"
                    )
                )
            ),
            Mode(
                id = "mode_gaming",
                name = "Gaming",
                description = "Boost audio immersion and visual vibrancy",
                iconName = "SportsEsports",
                colorHex = "#F43F5E", // Rose
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_gaming",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Boost Media to 85%",
                        summary = "High volume for immersive sound",
                        configJson = "{\"volume\":85}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_gaming",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Gaming Mode ON",
                        summary = "Performance & audio maximized",
                        configJson = "{\"title\":\"Gaming Mode\",\"message\":\"Audio boosted for gaming\"}"
                    )
                )
            ),
            Mode(
                id = "mode_driving",
                name = "Driving",
                description = "Safe hands-free audio and road alerts",
                iconName = "DirectionsCar",
                colorHex = "#F59E0B", // Amber
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_driving",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Media Volume 90%",
                        summary = "Set speaker/Bluetooth volume to 90%",
                        configJson = "{\"volume\":90}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_driving",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Driving Mode",
                        summary = "Safe travels",
                        configJson = "{\"title\":\"Driving Mode Active\",\"message\":\"Eyes on the road. Drive safe!\"}"
                    )
                )
            ),
            Mode(
                id = "mode_travel",
                name = "Travel",
                description = "Power saving and travel assistance",
                iconName = "Flight",
                colorHex = "#10B981", // Emerald
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "mode_travel",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Travel Mode Active",
                        summary = "Power-conscious profile enabled",
                        configJson = "{\"title\":\"Travel Mode\",\"message\":\"Roaming & power-conscious profile active\"}"
                    )
                )
            )
        )

        defaultModes.forEach { mode ->
            upsertMode(mode)
        }

        // Demo Routines
        val sampleRoutines = listOf(
            Routine(
                id = "sample_night",
                name = "Night Sleep Routine",
                description = "Automatically mutes distractions when charging late at night",
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
                        routineId = "sample_night",
                        type = ConditionType.TIME_RANGE,
                        title = "Time between 22:00 and 07:00",
                        summary = "Night hours",
                        configJson = "{\"startHour\":22,\"startMinute\":0,\"endHour\":7,\"endMinute\":0}"
                    ),
                    AutomationCondition(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_night",
                        type = ConditionType.CHARGING_STARTED,
                        title = "Phone is Charging",
                        summary = "Connected to power source",
                        configJson = "{}"
                    )
                ),
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_night",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Mute Media Volume",
                        summary = "Set volume to 0%",
                        configJson = "{\"volume\":0}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_night",
                        type = ActionType.SET_RINGER_MODE,
                        title = "Set Silent Mode",
                        summary = "Mute calls and messages",
                        configJson = "{\"mode\":\"SILENT\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_night",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Night Routine Active",
                        summary = "Shows sleep status notification",
                        configJson = "{\"title\":\"Night Routine Active\",\"message\":\"Good night! Media muted and alerts silenced.\"}"
                    )
                )
            ),
            Routine(
                id = "sample_work",
                name = "Office Work Hours",
                description = "Subtle vibration and low media volume during weekdays",
                iconName = "Work",
                colorHex = "#38BDF8",
                isEnabled = true,
                isFavorite = true,
                conditionLogic = ConditionLogic.ALL,
                priority = RoutinePriority.NORMAL,
                cooldownSeconds = 60,
                conditions = listOf(
                    AutomationCondition(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_work",
                        type = ConditionType.DAYS_OF_WEEK,
                        title = "Every Weekday",
                        summary = "Monday through Friday",
                        configJson = "{\"days\":[2,3,4,5,6]}"
                    ),
                    AutomationCondition(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_work",
                        type = ConditionType.TIME_RANGE,
                        title = "09:00 – 17:00",
                        summary = "Business hours",
                        configJson = "{\"startHour\":9,\"startMinute\":0,\"endHour\":17,\"endMinute\":0}"
                    )
                ),
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_work",
                        type = ActionType.SET_RINGER_MODE,
                        title = "Switch to Vibrate",
                        summary = "Prevent loud ringtones in meetings",
                        configJson = "{\"mode\":\"VIBRATE\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_work",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Media Volume 25%",
                        summary = "Keep media quiet",
                        configJson = "{\"volume\":25}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_work",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Work Mode Started",
                        summary = "Vibrate profile active",
                        configJson = "{\"title\":\"Work Routine Active\",\"message\":\"Vibration active for meetings.\"}"
                    )
                )
            ),
            Routine(
                id = "sample_headset",
                name = "Headset Audio Boost",
                description = "When headphones are plugged in, set optimal music volume",
                iconName = "Headphones",
                colorHex = "#34D399",
                isEnabled = true,
                isFavorite = false,
                conditionLogic = ConditionLogic.ANY,
                priority = RoutinePriority.NORMAL,
                cooldownSeconds = 15,
                conditions = listOf(
                    AutomationCondition(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_headset",
                        type = ConditionType.HEADSET_CONNECTED,
                        title = "Headphones Connected",
                        summary = "Wired or Bluetooth audio device plugged in",
                        configJson = "{}"
                    )
                ),
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_headset",
                        type = ActionType.SET_MEDIA_VOLUME,
                        title = "Set Media Volume to 60%",
                        summary = "Comfortable listening level",
                        configJson = "{\"volume\":60}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_headset",
                        type = ActionType.TRIGGER_VIBRATION,
                        title = "Haptic Click",
                        summary = "Haptic feedback on headphone connection",
                        configJson = "{}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_headset",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Headphones Connected",
                        summary = "Media volume set to 60%",
                        configJson = "{\"title\":\"Audio Connected\",\"message\":\"Media volume set to 60%.\"}"
                    )
                )
            ),
            Routine(
                id = "sample_battery_saver",
                name = "Low Battery Alert",
                description = "Notifies and provides battery settings shortcut when below 20%",
                iconName = "BatteryAlert",
                colorHex = "#F87171",
                isEnabled = true,
                isFavorite = false,
                conditionLogic = ConditionLogic.ANY,
                priority = RoutinePriority.CRITICAL,
                cooldownSeconds = 120,
                conditions = listOf(
                    AutomationCondition(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_battery_saver",
                        type = ConditionType.BATTERY_LEVEL_BELOW,
                        title = "Battery below 20%",
                        summary = "Low battery warning threshold",
                        configJson = "{\"threshold\":20}"
                    )
                ),
                actions = listOf(
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_battery_saver",
                        type = ActionType.SHOW_NOTIFICATION,
                        title = "Low Battery Detected",
                        summary = "Suggests plugging in power",
                        configJson = "{\"title\":\"Low Battery (\u226420%)\",\"message\":\"Plug in charger to maintain automations.\"}"
                    ),
                    AutomationAction(
                        id = UUID.randomUUID().toString(),
                        routineId = "sample_battery_saver",
                        type = ActionType.TRIGGER_VIBRATION,
                        title = "Vibration Alert",
                        summary = "Haptic alert for low power",
                        configJson = "{}"
                    )
                )
            )
        )

        sampleRoutines.forEach { routine ->
            upsertRoutine(routine)
        }
    }
}

private fun RoutineWithDetails.toDomain(): Routine {
    val logic = try {
        ConditionLogic.valueOf(routine.conditionLogic)
    } catch (_: Exception) {
        ConditionLogic.ALL
    }

    return Routine(
        id = routine.id,
        name = routine.name,
        description = routine.description,
        iconName = routine.iconName,
        colorHex = routine.colorHex,
        isEnabled = routine.isEnabled,
        isFavorite = routine.isFavorite,
        conditionLogic = logic,
        priority = RoutinePriority.fromLevel(routine.priority),
        cooldownSeconds = routine.cooldownSeconds,
        lastTriggeredTime = routine.lastTriggeredTime,
        triggerCount = routine.triggerCount,
        createdAt = routine.createdAt,
        conditions = conditions.sortedBy { it.orderIndex }.map { c ->
            val condType = try {
                ConditionType.valueOf(c.type)
            } catch (_: Exception) {
                ConditionType.TIME_SPECIFIC
            }
            AutomationCondition(
                id = c.id,
                routineId = c.routineId,
                type = condType,
                title = c.title,
                summary = c.summary,
                configJson = c.configJson,
                isNegated = c.isNegated
            )
        },
        actions = actions.sortedBy { it.orderIndex }.map { a ->
            val actType = try {
                ActionType.valueOf(a.type)
            } catch (_: Exception) {
                ActionType.SHOW_NOTIFICATION
            }
            AutomationAction(
                id = a.id,
                routineId = a.routineId,
                type = actType,
                title = a.title,
                summary = a.summary,
                configJson = a.configJson,
                restoreOnExit = a.restoreOnExit
            )
        }
    )
}

private fun ModeEntity.toDomain(): Mode {
    val actionsList = mutableListOf<AutomationAction>()
    try {
        val array = JSONArray(actionsJson)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val typeStr = obj.optString("type", ActionType.SHOW_NOTIFICATION.name)
            val actType = try {
                ActionType.valueOf(typeStr)
            } catch (_: Exception) {
                ActionType.SHOW_NOTIFICATION
            }
            actionsList.add(
                AutomationAction(
                    id = UUID.randomUUID().toString(),
                    routineId = id,
                    type = actType,
                    title = obj.optString("title", actType.displayName),
                    summary = obj.optString("summary", ""),
                    configJson = obj.optString("configJson", "{}")
                )
            )
        }
    } catch (_: Exception) {}

    return Mode(
        id = id,
        name = name,
        description = description,
        iconName = iconName,
        colorHex = colorHex,
        isActive = isActive,
        actions = actionsList,
        activatedAt = activatedAt
    )
}

private fun ExecutionLogEntity.toDomain(): ExecutionLog {
    return ExecutionLog(
        id = id,
        routineId = routineId,
        routineName = routineName,
        triggerName = triggerName,
        timestamp = timestamp,
        isSuccess = isSuccess,
        conditionsSummary = conditionsSummary,
        actionsSummary = actionsSummary,
        resultSummary = resultSummary,
        errorMessage = errorMessage
    )
}
