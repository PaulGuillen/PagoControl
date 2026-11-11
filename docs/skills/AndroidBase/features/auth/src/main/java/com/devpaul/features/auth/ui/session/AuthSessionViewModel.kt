package com.devpaul.features.auth.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.core.domain.result.UseCaseResult
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.features.auth.domain.usecase.GetCurrentUserUseCase
import com.devpaul.features.auth.domain.usecase.GetCurrentUserProfileUseCase
import com.devpaul.features.auth.domain.usecase.SignOutUseCase
import com.devpaul.features.auth.domain.usecase.UpdateUserNameUseCase
import com.devpaul.features.auth.ui.mapper.toMessageResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AuthSessionViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserProfileUseCase: GetCurrentUserProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var loadUserJob: Job? = null

    init {
        refreshUser()
    }

    fun isAuthenticated(): Boolean = getCurrentUserUseCase() != null

    fun refreshUser() {
        loadUserJob?.cancel()
        _uiState.value = UserProfileUiState()
        loadUserJob = viewModelScope.launch {
            val user = try {
                getCurrentUserProfileUseCase()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                AppAnalytics.error("load_user_profile", error)
                getCurrentUserUseCase()
            }
            _uiState.value = UserProfileUiState(
                isLoading = false,
                name = user?.name.orEmpty().trim(),
                email = user?.email.orEmpty(),
            )
        }
    }

    suspend fun updateUserName(name: String): Int? {
        loadUserJob?.cancel()
        _uiState.update { it.copy(isLoading = false) }
        return when (val result = updateUserNameUseCase(name)) {
            is UseCaseResult.Success -> {
                AppAnalytics.outcome("update_profile_name", "success")
                _uiState.value = UserProfileUiState(
                    isLoading = false,
                    name = result.value.name.trim(),
                    email = result.value.email,
                )
                null
            }
            is UseCaseResult.Error -> {
                AppAnalytics.error("update_profile_name", code = result.error.error.name.lowercase())
                result.error.error.toMessageResource()
            }
        }
    }

    fun logout() {
        loadUserJob?.cancel()
        _uiState.value = UserProfileUiState(isLoading = false)
        signOutUseCase()
        AppAnalytics.outcome("logout", "success")
    }
}
