package com.devpaul.pagocontrol.session

sealed interface SignInResult {
    data object Success : SignInResult
    data object InvalidCredentials : SignInResult
    data object Unavailable : SignInResult
}
