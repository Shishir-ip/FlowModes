package com.example

import android.app.Application
import com.example.automation.engine.AutomationEngine
import com.example.automation.scheduler.FlowScheduler
import com.example.data.database.FlowDatabase
import com.example.data.repository.AutomationRepository
import com.example.data.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FlowApplication : Application() {

    lateinit var database: FlowDatabase
        private set
    lateinit var repository: AutomationRepository
        private set
    lateinit var preferenceRepository: PreferenceRepository
        private set
    lateinit var automationEngine: AutomationEngine
        private set
    lateinit var flowScheduler: FlowScheduler
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        database = FlowDatabase.getInstance(this)
        repository = AutomationRepository(database)
        preferenceRepository = PreferenceRepository(this)
        automationEngine = AutomationEngine(this, repository)
        flowScheduler = FlowScheduler(this)

        applicationScope.launch {
            val isFirstLaunchDone = preferenceRepository.isFirstLaunchDone.first()
            if (!isFirstLaunchDone) {
                repository.initializeDefaultDataIfEmpty()
                preferenceRepository.setFirstLaunchDone(true)
            }
            flowScheduler.scheduleNextEvaluation()
        }
    }
}
