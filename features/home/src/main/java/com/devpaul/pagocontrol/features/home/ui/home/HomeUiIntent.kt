package com.devpaul.pagocontrol.features.home.ui.home

sealed interface HomeUiIntent {
    data object Retry : HomeUiIntent
    data class SelectPeriod(val monthly: Boolean) : HomeUiIntent
    data class SelectBar(val index: Int) : HomeUiIntent
    data object ConfirmNotification : HomeUiIntent
    data object Upcoming : HomeUiIntent
    data object DismissUpcoming : HomeUiIntent
    data object SignOut : HomeUiIntent
}
