package com.devpaul.pagocontrol.features.home.ui.home.model

import com.devpaul.pagocontrol.features.home.domain.entity.Dashboard

sealed interface HomeContent {
    data object Loading : HomeContent
    data object Error : HomeContent
    data class Ready(val dashboard: Dashboard) : HomeContent
}
