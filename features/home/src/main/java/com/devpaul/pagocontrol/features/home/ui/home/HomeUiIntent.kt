package com.devpaul.pagocontrol.features.home.ui.home

sealed interface HomeUiIntent {
    data class SelectSection(val section: HomeSection) : HomeUiIntent
    data object Retry : HomeUiIntent
    data class SelectPeriod(val monthly: Boolean) : HomeUiIntent
    data class SelectBar(val index: Int) : HomeUiIntent
    data object Upcoming : HomeUiIntent
    data object DismissUpcoming : HomeUiIntent
    data object SignOut : HomeUiIntent
    data class SetNotificationSource(val packageName: String, val enabled: Boolean) : HomeUiIntent
    data class ConfirmPayment(val id: String, val amountCents: Long) : HomeUiIntent
    data class DiscardNotification(val id: String) : HomeUiIntent
}
