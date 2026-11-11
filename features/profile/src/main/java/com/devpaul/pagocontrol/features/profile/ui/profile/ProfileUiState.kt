package com.devpaul.pagocontrol.features.profile.ui.profile

enum class ProfileDialog { Edit, Appearance, Language, Rating, SignOut, Notifications }
enum class ProfileNotice { SavedRating, SaveError, LoadError, Upcoming }

data class ProfileUiState(
    val emailEditable: Boolean = true,
    val name: String = "",
    val email: String = "",
    val notificationsEnabled: Boolean = true,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val dialog: ProfileDialog? = null,
    val notice: ProfileNotice? = null,
    val draftName: String = "",
    val draftEmail: String = "",
    val stars: Int = 0,
    val comment: String = "",
) {
    val canSaveIdentity: Boolean
        get() = draftName.trim().length in 2..80 &&
            (draftEmail.isBlank() || com.devpaul.pagocontrol.domain.LoginValidation.validEmail(draftEmail))
}
