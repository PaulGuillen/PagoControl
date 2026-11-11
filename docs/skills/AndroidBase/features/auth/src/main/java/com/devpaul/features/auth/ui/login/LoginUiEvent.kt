package com.devpaul.features.auth.ui.login

sealed interface LoginUiEvent {
    data object Authenticated : LoginUiEvent
    data class ShowSnackbar(val message: Int) : LoginUiEvent
}
