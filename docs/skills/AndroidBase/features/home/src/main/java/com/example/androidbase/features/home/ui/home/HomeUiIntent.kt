package com.example.androidbase.features.home.ui.home

sealed interface HomeUiIntent {
    data class Add(val title: String) : HomeUiIntent
    data class SetDarkTheme(val enabled: Boolean) : HomeUiIntent
    data object Retry : HomeUiIntent
}
