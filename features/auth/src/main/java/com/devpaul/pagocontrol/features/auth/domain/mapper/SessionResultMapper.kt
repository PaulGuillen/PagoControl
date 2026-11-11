package com.devpaul.pagocontrol.features.auth.domain.mapper

import com.devpaul.pagocontrol.features.auth.domain.exception.AuthError
import com.devpaul.pagocontrol.session.AccountResult
import com.devpaul.pagocontrol.session.SignInResult

fun AuthError.toSignInResult(): SignInResult = when (this) {
    AuthError.INVALID_EMAIL, AuthError.EMPTY_PASSWORD, AuthError.INVALID_CREDENTIALS -> SignInResult.InvalidCredentials
    AuthError.NETWORK -> SignInResult.Network
    AuthError.USER_DISABLED -> SignInResult.UserDisabled
    AuthError.TOO_MANY_REQUESTS, AuthError.ATTEMPT_LIMIT_REACHED -> SignInResult.TooManyRequests
    AuthError.PROFILE_INCOMPLETE -> SignInResult.ProfileIncomplete
    else -> SignInResult.Unavailable
}

fun AuthError.toAccountResult(): AccountResult = when (this) {
    AuthError.INVALID_EMAIL, AuthError.INVALID_NAME, AuthError.EMPTY_PASSWORD -> AccountResult.InvalidInput
    AuthError.SHORT_PASSWORD -> AccountResult.WeakPassword
    AuthError.EMAIL_ALREADY_IN_USE -> AccountResult.EmailAlreadyInUse
    AuthError.NETWORK -> AccountResult.Network
    AuthError.TOO_MANY_REQUESTS, AuthError.ATTEMPT_LIMIT_REACHED -> AccountResult.TooManyRequests
    AuthError.PROFILE_INCOMPLETE -> AccountResult.ProfileIncomplete
    else -> AccountResult.Unavailable
}
