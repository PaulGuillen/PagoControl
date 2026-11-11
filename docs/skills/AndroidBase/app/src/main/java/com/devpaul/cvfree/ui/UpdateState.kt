package com.devpaul.cvfree.ui

sealed class UpdateState {
    data object Loading : UpdateState()
    data object Required : UpdateState()
    data object NotRequired : UpdateState()
}