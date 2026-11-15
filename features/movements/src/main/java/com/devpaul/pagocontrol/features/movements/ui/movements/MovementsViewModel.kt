package com.devpaul.pagocontrol.features.movements.ui.movements

import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.MovementHistoryRepository
import java.time.YearMonth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.devpaul.pagocontrol.platform.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MovementsViewModel(private val history: MovementHistoryRepository) : BaseViewModel<MovementsUiState>(MovementsUiState()) {
    private var historyJob: Job? = null

    init { observeHistory() }

    private fun observeHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            updateState { it.copy(loading = true, error = false) }
            try {
                history.observeMovementHistory().collect { movements ->
                    updateState { state ->
                        state.copy(
                            movements = movements.map { it.toHistoryMovement() },
                            reviewing = state.reviewing?.let { selected ->
                                movements.find { it.id == selected.id }?.toHistoryMovement()
                            },
                            loading = false,
                            error = false,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(loading = false, error = true) }
            }
        }
    }

    fun onIntent(intent: MovementsUiIntent) {
        if (intent == MovementsUiIntent.Retry) {
            observeHistory()
            return
        }
        updateState { state ->
            when (intent) {
                MovementsUiIntent.Retry -> state
                is MovementsUiIntent.Search -> state.copy(query = intent.value, historyLimit = MovementsUiState.HISTORY_PAGE_SIZE)
                is MovementsUiIntent.Channel -> state.copy(channel = intent.value, historyLimit = MovementsUiState.HISTORY_PAGE_SIZE)
                is MovementsUiIntent.Month -> state.copy(month = intent.value, historyLimit = MovementsUiState.HISTORY_PAGE_SIZE)
                is MovementsUiIntent.Review -> state.copy(reviewing = intent.movement)
                MovementsUiIntent.Dismiss -> state.copy(reviewing = null)
                MovementsUiIntent.ViewAll -> state.copy(showAll = true, historyLimit = MovementsUiState.HISTORY_PAGE_SIZE)
                MovementsUiIntent.Back -> state.copy(showAll = false, reviewing = null)
                MovementsUiIntent.LoadMore -> if (state.hasMore) state.copy(
                    historyLimit = state.historyLimit + MovementsUiState.HISTORY_PAGE_SIZE
                ) else state
                MovementsUiIntent.Reset -> state.copy(
                    query = "", channel = null,
                    month = YearMonth.from(state.referenceDate),
                    historyLimit = MovementsUiState.HISTORY_PAGE_SIZE,
                )
            }
        }
    }
}
