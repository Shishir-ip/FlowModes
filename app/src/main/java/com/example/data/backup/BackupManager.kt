package com.example.data.backup

import com.example.domain.models.ActionType
import com.example.domain.models.AutomationAction
import com.example.domain.models.AutomationCondition
import com.example.domain.models.ConditionLogic
import com.example.domain.models.ConditionType
import com.example.domain.models.Routine
import com.example.domain.models.RoutinePriority
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object BackupManager {
    private const val SCHEMA_VERSION = 1

    fun exportRoutinesToJson(routines: List<Routine>): String {
        val root = JSONObject()
        root.put("version", SCHEMA_VERSION)
        root.put("app", "FlowModes")
        root.put("exportedAt", System.currentTimeMillis())

        val routinesArray = JSONArray()
        routines.forEach { r ->
            val rObj = JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                put("description", r.description)
                put("iconName", r.iconName)
                put("colorHex", r.colorHex)
                put("isEnabled", r.isEnabled)
                put("isFavorite", r.isFavorite)
                put("conditionLogic", r.conditionLogic.name)
                put("priority", r.priority.level)
                put("cooldownSeconds", r.cooldownSeconds)

                val condArray = JSONArray()
                r.conditions.forEach { c ->
                    val cObj = JSONObject().apply {
                        put("type", c.type.name)
                        put("title", c.title)
                        put("summary", c.summary)
                        put("configJson", c.configJson)
                    }
                    condArray.put(cObj)
                }
                put("conditions", condArray)

                val actArray = JSONArray()
                r.actions.forEach { a ->
                    val aObj = JSONObject().apply {
                        put("type", a.type.name)
                        put("title", a.title)
                        put("summary", a.summary)
                        put("configJson", a.configJson)
                    }
                    actArray.put(aObj)
                }
                put("actions", actArray)
            }
            routinesArray.put(rObj)
        }
        root.put("routines", routinesArray)
        return root.toString(2)
    }

    sealed class ImportResult {
        data class Success(val routines: List<Routine>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    fun importRoutinesFromJson(jsonString: String): ImportResult {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("routines")) {
                return ImportResult.Error("Invalid backup format: missing 'routines' node")
            }

            val routinesArray = root.getJSONArray("routines")
            val importedList = mutableListOf<Routine>()

            for (i in 0 until routinesArray.length()) {
                val rObj = routinesArray.getJSONObject(i)
                val routineName = rObj.optString("name", "Imported Routine")
                val newRoutineId = UUID.randomUUID().toString()

                val logic = try {
                    ConditionLogic.valueOf(rObj.optString("conditionLogic", ConditionLogic.ALL.name))
                } catch (_: Exception) {
                    ConditionLogic.ALL
                }

                val priority = RoutinePriority.fromLevel(rObj.optInt("priority", 2))

                // Parse conditions
                val condArray = rObj.optJSONArray("conditions") ?: JSONArray()
                val conditions = mutableListOf<AutomationCondition>()
                for (j in 0 until condArray.length()) {
                    val cObj = condArray.getJSONObject(j)
                    val typeStr = cObj.optString("type")
                    val type = try {
                        ConditionType.valueOf(typeStr)
                    } catch (_: Exception) {
                        null
                    }
                    if (type != null) {
                        conditions.add(
                            AutomationCondition(
                                id = UUID.randomUUID().toString(),
                                routineId = newRoutineId,
                                type = type,
                                title = cObj.optString("title", type.displayName),
                                summary = cObj.optString("summary", ""),
                                configJson = cObj.optString("configJson", "{}")
                            )
                        )
                    }
                }

                // Parse actions
                val actArray = rObj.optJSONArray("actions") ?: JSONArray()
                val actions = mutableListOf<AutomationAction>()
                for (k in 0 until actArray.length()) {
                    val aObj = actArray.getJSONObject(k)
                    val typeStr = aObj.optString("type")
                    val type = try {
                        ActionType.valueOf(typeStr)
                    } catch (_: Exception) {
                        null
                    }
                    if (type != null) {
                        actions.add(
                            AutomationAction(
                                id = UUID.randomUUID().toString(),
                                routineId = newRoutineId,
                                type = type,
                                title = aObj.optString("title", type.displayName),
                                summary = aObj.optString("summary", ""),
                                configJson = aObj.optString("configJson", "{}")
                            )
                        )
                    }
                }

                importedList.add(
                    Routine(
                        id = newRoutineId,
                        name = routineName,
                        description = rObj.optString("description", ""),
                        iconName = rObj.optString("iconName", "AutoAwesome"),
                        colorHex = rObj.optString("colorHex", "#38BDF8"),
                        isEnabled = false, // Security mandate: user must review before activating imported routines!
                        isFavorite = false,
                        conditionLogic = logic,
                        priority = priority,
                        cooldownSeconds = rObj.optLong("cooldownSeconds", 30),
                        conditions = conditions,
                        actions = actions,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }

            ImportResult.Success(importedList)
        } catch (e: Exception) {
            ImportResult.Error("Failed to parse JSON backup: ${e.localizedMessage ?: "Unknown syntax error"}")
        }
    }
}
