package com.devpaul.pagocontrol.features.movements.ui.movements

import java.time.YearMonth

sealed interface MovementsUiIntent {
    data class Search(val value: String) : MovementsUiIntent
    data class Channel(val value: MovementChannel?) : MovementsUiIntent
    data class Month(val value: YearMonth) : MovementsUiIntent
    data class Review(val movement: HistoryMovement) : MovementsUiIntent
    data object Retry : MovementsUiIntent
    data object Dismiss : MovementsUiIntent
    data object ViewAll : MovementsUiIntent
    data object Back : MovementsUiIntent
    data object LoadMore : MovementsUiIntent
    data object Reset : MovementsUiIntent
}
