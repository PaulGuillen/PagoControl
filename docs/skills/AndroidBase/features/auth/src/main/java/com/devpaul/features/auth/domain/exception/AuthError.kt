package com.devpaul.features.auth.domain.exception

enum class AuthError {
    INVALID_EMAIL,
    EMPTY_PASSWORD,
    SHORT_PASSWORD,
    INVALID_NAME,
    EMAIL_ALREADY_IN_USE,
    INVALID_CREDENTIALS,
    USER_DISABLED,
    TOO_MANY_REQUESTS,
    ATTEMPT_LIMIT_REACHED,
    NETWORK,
    UNKNOWN,
}
