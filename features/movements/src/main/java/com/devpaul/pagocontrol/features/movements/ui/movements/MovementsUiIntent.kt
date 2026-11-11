package com.devpaul.pagocontrol.features.movements.ui.movements

sealed interface MovementsUiIntent {
    data class Search(val value: String) : MovementsUiIntent
    data class Filter(val value: String) : MovementsUiIntent
    data class Source(val value: String) : MovementsUiIntent
    data class Status(val value: String) : MovementsUiIntent
    data class Month(val value: String) : MovementsUiIntent
    data class Review(val movement: HistoryMovement) : MovementsUiIntent
    data class Categorize(val category: String) : MovementsUiIntent
    data object Dismiss : MovementsUiIntent
    data object Upcoming : MovementsUiIntent
    data object LoadMore : MovementsUiIntent
    data object Reset : MovementsUiIntent
}
