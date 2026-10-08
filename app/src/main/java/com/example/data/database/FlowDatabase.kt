package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.database.dao.ExecutionLogDao
import com.example.data.database.dao.ModeDao
import com.example.data.database.dao.RoutineDao
import com.example.data.database.entities.ActionEntity
import com.example.data.database.entities.ConditionEntity
import com.example.data.database.entities.ExecutionLogEntity
import com.example.data.database.entities.ModeEntity
import com.example.data.database.entities.RoutineEntity

@Database(
    entities = [
        RoutineEntity::class,
        ConditionEntity::class,
        ActionEntity::class,
        ModeEntity::class,
        ExecutionLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun modeDao(): ModeDao
    abstract fun executionLogDao(): ExecutionLogDao

    companion object {
        @Volatile
        private var INSTANCE: FlowDatabase? = null

        fun getInstance(context: Context): FlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowDatabase::class.java,
                    "flowmodes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
