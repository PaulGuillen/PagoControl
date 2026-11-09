package com.devpaul.pagocontrol.domain

object LoginValidation {
    fun validEmail(email: String): Boolean =
        email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))

    fun validPassword(password: String): Boolean = password.length >= 8
}
