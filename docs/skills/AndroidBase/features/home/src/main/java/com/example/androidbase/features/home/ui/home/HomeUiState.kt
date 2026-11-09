package com.example.androidbase.features.home.ui.home

import com.example.androidbase.domain.AppFailure
import com.example.androidbase.domain.ExampleItem

data class HomeUiState(
    val items: List<ExampleItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val failure: AppFailure? = null,
    val savedCount: Int = 0,
)
