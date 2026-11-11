package com.devpaul.pagocontrol.features.auth.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.features.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.devpaul.pagocontrol.features.auth.domain.usecase.GetCurrentUserUseCase
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import com.devpaul.pagocontrol.session.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AuthSessionViewModel(
    repository: AuthRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getCurrentUserProfile: GetCurrentUserProfileUseCase,
) : ViewModel() {
    val session = repository.session
    private var refreshJob: Job? = null
    fun isAuthenticated(): Boolean = getCurrentUser() != null
    fun refreshUser() {
        refreshJob?.cancel()
        if (!isAuthenticated()) return
        refreshJob = viewModelScope.launch {
            try {
                getCurrentUserProfile()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppAnalytics.error("load_user_profile", error)
            }
        }
    }
}
