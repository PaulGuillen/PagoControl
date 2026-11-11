package com.devpaul.pagocontrol.features.profile.ui.profile

sealed interface ProfileUiIntent {
    data class Open(val dialog: ProfileDialog) : ProfileUiIntent
    data class Name(val value: String) : ProfileUiIntent
    data class Email(val value: String) : ProfileUiIntent
    data class Notifications(val enabled: Boolean) : ProfileUiIntent
    data class Stars(val value: Int) : ProfileUiIntent
    data class Comment(val value: String) : ProfileUiIntent
    data object SaveIdentity : ProfileUiIntent
    data object SaveRating : ProfileUiIntent
    data object SignOut : ProfileUiIntent
    data object Dismiss : ProfileUiIntent
    data object Upcoming : ProfileUiIntent
    data object Retry : ProfileUiIntent
}
