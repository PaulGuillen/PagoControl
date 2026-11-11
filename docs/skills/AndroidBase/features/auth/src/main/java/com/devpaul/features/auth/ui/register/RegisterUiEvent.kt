package com.devpaul.features.auth.ui.register

sealed interface RegisterUiEvent {
    data object Authenticated : RegisterUiEvent
    data class ShowSnackbar(val message: Int) : RegisterUiEvent
}
