package com.devpaul.features.auth.ui.mapper

import androidx.annotation.StringRes

import com.devpaul.features.auth.R

import com.devpaul.features.auth.domain.exception.AuthError

@StringRes
internal fun AuthError.toMessageResource(): Int = when (this) {
    AuthError.INVALID_EMAIL -> R.string.ui_enter_a_valid_email_address_4ddef7
    AuthError.EMPTY_PASSWORD -> R.string.ui_enter_your_password_5337bc
    AuthError.SHORT_PASSWORD -> R.string.ui_password_must_be_at_least_8_characters_long_0af404
    AuthError.INVALID_NAME -> R.string.ui_enter_a_valid_name_893e60
    AuthError.EMAIL_ALREADY_IN_USE -> R.string.ui_an_account_with_this_email_already_exists_bec7d2
    AuthError.INVALID_CREDENTIALS -> R.string.ui_the_email_or_password_is_incorrect_2ee7a8
    AuthError.USER_DISABLED -> R.string.ui_this_account_is_disabled_1b4749
    AuthError.TOO_MANY_REQUESTS ->
        R.string.ui_too_many_attempts_wait_a_few_minutes_and_try_again_483702
    AuthError.ATTEMPT_LIMIT_REACHED -> R.string.auth_attempt_limit_reached
    AuthError.NETWORK -> R.string.ui_couldn_t_connect_check_your_connection_and_try_again_e38359
    AuthError.UNKNOWN -> R.string.ui_the_operation_couldn_t_be_completed_try_again_03bf2c
}
