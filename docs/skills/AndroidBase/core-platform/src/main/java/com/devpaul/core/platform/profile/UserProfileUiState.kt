package com.devpaul.core.platform.profile

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
) {
    val initials: String
        get() = name.trim().split(Regex("\\s+"))
            .filter(String::isNotBlank)
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
}
