package com.example.run.presentation.run_overview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.run.RunRepository
import com.example.core.domain.run.SyncRunScheduler
import com.example.run.presentation.run_overview.mapper.toRunUi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class RunOverviewViewModel(
    private val runRepository: RunRepository,
    private val syncRunScheduler: SyncRunScheduler
): ViewModel() {

    var state by mutableStateOf(RunOverviewState())
        private set

    init {

        viewModelScope.launch {
            syncRunScheduler.scheduleSync(
                type = SyncRunScheduler.SyncType.FetchRuns(30.minutes)
            )
        }

        runRepository.getRuns().onEach { runs ->
            val runsUi = runs.map { it.toRunUi() }
            state = state.copy(runs = runsUi)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            runRepository.syncPendingRuns()
            runRepository.fetchRuns()
        }
    }

    fun onAction(action: RunOverviewAction) {
        when(action) {
            RunOverviewAction.OnAnalyticsClick -> Unit
            RunOverviewAction.OnLogoutClick -> Unit
            RunOverviewAction.OnStartRun -> Unit
            is RunOverviewAction.DeleteRun -> {
                viewModelScope.launch {
                    runRepository.deleteRunById(action.runUi.id)
                }
            }
        }
    }
}