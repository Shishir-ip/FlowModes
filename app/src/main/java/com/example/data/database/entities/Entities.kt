package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val isEnabled: Boolean,
    val isFavorite: Boolean,
    val conditionLogic: String, // "ALL" or "ANY"
    val priority: Int,
    val cooldownSeconds: Long,
    val lastTriggeredTime: Long?,
    val triggerCount: Int,
    val createdAt: Long
)

@Entity(
    tableName = "conditions",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class ConditionEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val type: String,
    val title: String,
    val summary: String,
    val configJson: String,
    val orderIndex: Int,
    val isNegated: Boolean = false
)

@Entity(
    tableName = "actions",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class ActionEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val type: String,
    val title: String,
    val summary: String,
    val configJson: String,
    val orderIndex: Int,
    val restoreOnExit: Boolean = false
)

@Entity(tableName = "modes")
data class ModeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val colorHex: String,
    val isActive: Boolean,
    val actionsJson: String,
    val activatedAt: Long?
)

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routineId: String,
    val routineName: String,
    val triggerName: String,
    val timestamp: Long,
    val isSuccess: Boolean,
    val conditionsSummary: String,
    val actionsSummary: String,
    val resultSummary: String,
    val errorMessage: String?
)
