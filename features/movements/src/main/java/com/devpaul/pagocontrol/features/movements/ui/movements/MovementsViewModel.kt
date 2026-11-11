package com.devpaul.pagocontrol.features.movements.ui.movements

import com.devpaul.pagocontrol.platform.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class MovementsViewModel : BaseViewModel<MovementsUiState>(MovementsUiState()) {
    fun onIntent(intent: MovementsUiIntent) {
        updateState { state ->
            when (intent) {
                is MovementsUiIntent.Search -> state.copy(query = intent.value)
                is MovementsUiIntent.Filter -> state.copy(filter = intent.value)
                is MovementsUiIntent.Source -> state.copy(source = intent.value)
                is MovementsUiIntent.Status -> state.copy(status = intent.value)
                is MovementsUiIntent.Month -> state.copy(month = intent.value)
                is MovementsUiIntent.Review -> state.copy(reviewing = intent.movement)
                is MovementsUiIntent.Categorize -> state.copy(reviewing = null, movements = state.movements.map {
                    if (it.id == state.reviewing?.id && it.pending) it.copy(pending = false, badge = intent.category, status = "Confirmado") else it
                })
                MovementsUiIntent.Dismiss -> state.copy(reviewing = null, notice = null)
                MovementsUiIntent.Upcoming -> state.copy(notice = "Esta función estará disponible próximamente.")
                MovementsUiIntent.LoadMore -> state.copy(notice = "Has visualizado todos los movimientos de ejemplo disponibles.")
                MovementsUiIntent.Reset -> state.copy(query = "", filter = "all", source = "Todas", status = "Todos", month = "Noviembre 2023")
            }
        }
    }
}
