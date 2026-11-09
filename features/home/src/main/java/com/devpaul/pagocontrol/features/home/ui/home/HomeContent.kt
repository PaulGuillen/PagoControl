package com.devpaul.pagocontrol.features.home.ui.home

import com.devpaul.pagocontrol.domain.Dashboard

sealed interface HomeContent {
    data object Loading : HomeContent
    data object Error : HomeContent
    data class Ready(val dashboard: Dashboard) : HomeContent
}
