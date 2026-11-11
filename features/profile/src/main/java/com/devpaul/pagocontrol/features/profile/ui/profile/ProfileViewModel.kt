package com.devpaul.pagocontrol.features.profile.ui.profile

import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.domain.ProfilePreferences
import com.devpaul.pagocontrol.platform.BaseViewModel
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.AccountResult
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val profile: ProfileRepository,
    private val auth: AuthRepository,
) : BaseViewModel<ProfileUiState>(ProfileUiState(emailEditable = auth.isDemo)) {
    private val user = auth.session.value
    private var observing: Job? = null
    private var savedPreferences = ProfilePreferences()

    init { observe() }

    fun onIntent(intent: ProfileUiIntent) {
        if (state.value.saving) return
        when (intent) {
            is ProfileUiIntent.Open -> updateState {
                it.copy(
                    dialog = intent.dialog, draftName = it.name, draftEmail = it.email,
                    stars = if (intent.dialog == ProfileDialog.Rating) savedPreferences.rating else it.stars,
                    comment = if (intent.dialog == ProfileDialog.Rating) savedPreferences.ratingComment else it.comment,
                )
            }
            is ProfileUiIntent.Name -> updateState { it.copy(draftName = intent.value.take(80)) }
            is ProfileUiIntent.Email -> updateState { it.copy(draftEmail = intent.value.take(254)) }
            is ProfileUiIntent.Stars -> if (intent.value in 1..5) updateState { it.copy(stars = intent.value) }
            is ProfileUiIntent.Comment -> updateState { it.copy(comment = intent.value.take(500)) }
            is ProfileUiIntent.Notifications -> user?.let { current ->
                save { profile.setNotifications(current.id, intent.enabled) }
            }
            ProfileUiIntent.SaveIdentity -> {
                val current = user ?: return
                val snapshot = state.value
                if (snapshot.canSaveIdentity && snapshot.dialog == ProfileDialog.Edit) save {
                    if (auth.isDemo) profile.saveIdentity(current.id, snapshot.draftName.trim(), snapshot.draftEmail.trim())
                    else check(auth.updateName(snapshot.draftName.trim()) == AccountResult.Success)
                }
            }
            ProfileUiIntent.SaveRating -> {
                val current = user ?: return
                val snapshot = state.value
                if (snapshot.stars in 1..5 && snapshot.dialog == ProfileDialog.Rating) save(ProfileNotice.SavedRating) {
                    profile.saveRating(current.id, snapshot.stars, snapshot.comment)
                }
            }
            ProfileUiIntent.SignOut -> if (state.value.dialog == ProfileDialog.SignOut) save { auth.signOut() }
            ProfileUiIntent.Dismiss -> updateState { it.copy(dialog = null, notice = null) }
            ProfileUiIntent.Upcoming -> updateState { it.copy(notice = ProfileNotice.Upcoming) }
            ProfileUiIntent.Retry -> observe()
        }
    }

    private fun observe() {
        observing?.cancel()
        val current = user ?: run { updateState { it.copy(loading = false) }; return }
        updateState { it.copy(loading = true, notice = null) }
        observing = viewModelScope.launch {
            try {
                combine(profile.observe(current.id), auth.session) { preferences, authenticated -> preferences to authenticated }.collect { (preferences, authenticated) ->
                    savedPreferences = preferences
                    updateState {
                        it.copy(
                            name = if (auth.isDemo) preferences.name ?: current.name else authenticated?.name.orEmpty(),
                            email = if (auth.isDemo) preferences.email ?: current.email else authenticated?.email.orEmpty(),
                            notificationsEnabled = preferences.notificationsEnabled,
                            stars = if (it.dialog == ProfileDialog.Rating) it.stars else preferences.rating,
                            comment = if (it.dialog == ProfileDialog.Rating) it.comment else preferences.ratingComment,
                            loading = false,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(loading = false, notice = ProfileNotice.LoadError) }
            }
        }
    }

    private fun save(notice: ProfileNotice? = null, operation: suspend () -> Unit) {
        updateState { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                operation()
                updateState { it.copy(dialog = null, notice = notice) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(notice = ProfileNotice.SaveError) }
            } finally {
                updateState { it.copy(saving = false) }
            }
        }
    }
}
