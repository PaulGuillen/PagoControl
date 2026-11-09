package com.devpaul.pagocontrol.features.home.ui.home

data class HomeUiState(
    val content: HomeContent = HomeContent.Loading,
    val monthly: Boolean = false,
    val selectedBar: Int = 5,
    val confirmed: Boolean = false,
    val showUpcoming: Boolean = false,
    val signingOut: Boolean = false,
)
