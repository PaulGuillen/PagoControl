package com.devpaul.pagocontrol.session

sealed interface SignInResult {
    data object Success : SignInResult
    data object InvalidCredentials : SignInResult
    data object Unavailable : SignInResult
    data object Network : SignInResult
    data object TooManyRequests : SignInResult
    data object UserDisabled : SignInResult
    data object ProfileIncomplete : SignInResult
}
