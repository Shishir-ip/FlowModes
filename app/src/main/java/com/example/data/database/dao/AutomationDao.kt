package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.database.entities.ActionEntity
import com.example.data.database.entities.ConditionEntity
import com.example.data.database.entities.ExecutionLogEntity
import com.example.data.database.entities.ModeEntity
import com.example.data.database.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow

data class RoutineWithDetails(
    @Embedded val routine: RoutineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val conditions: List<ConditionEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val actions: List<ActionEntity>
)

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllRoutines(): Flow<List<RoutineWithDetails>>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): RoutineWithDetails?

    @Transaction
    @Query("SELECT * FROM routines WHERE isEnabled = 1")
    suspend fun getActiveRoutines(): List<RoutineWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("UPDATE routines SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRoutineEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE routines SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setRoutineFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE routines SET lastTriggeredTime = :timestamp, triggerCount = triggerCount + 1 WHERE id = :id")
    suspend fun recordRoutineExecution(id: String, timestamp: Long)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConditions(conditions: List<ConditionEntity>)

    @Query("DELETE FROM conditions WHERE routineId = :routineId")
    suspend fun deleteConditionsForRoutine(routineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ActionEntity>)

    @Query("DELETE FROM actions WHERE routineId = :routineId")
    suspend fun deleteActionsForRoutine(routineId: String)

    @Transaction
    suspend fun upsertCompleteRoutine(
        routine: RoutineEntity,
        conditions: List<ConditionEntity>,
        actions: List<ActionEntity>
    ) {
        insertRoutine(routine)
        deleteConditionsForRoutine(routine.id)
        insertConditions(conditions)
        deleteActionsForRoutine(routine.id)
        insertActions(actions)
    }
}

@Dao
interface ModeDao {
    @Query("SELECT * FROM modes ORDER BY name ASC")
    fun getAllModes(): Flow<List<ModeEntity>>

    @Query("SELECT * FROM modes WHERE id = :id")
    suspend fun getModeById(id: String): ModeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertModes(modes: List<ModeEntity>)

    @Query("UPDATE modes SET isActive = :isActive, activatedAt = :activatedAt WHERE id = :id")
    suspend fun setModeActive(id: String, isActive: Boolean, activatedAt: Long?)

    @Query("UPDATE modes SET isActive = 0, activatedAt = NULL WHERE id != :exceptId")
    suspend fun deactivateOtherModes(exceptId: String)

    @Query("UPDATE modes SET isActive = 0, activatedAt = NULL")
    suspend fun deactivateAllModes()

    @Query("DELETE FROM modes WHERE id = :id")
    suspend fun deleteMode(id: String)
}

@Dao
interface ExecutionLogDao {
    @Query("SELECT * FROM execution_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<ExecutionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ExecutionLogEntity)

    @Query("DELETE FROM execution_logs")
    suspend fun clearAllLogs()
}
