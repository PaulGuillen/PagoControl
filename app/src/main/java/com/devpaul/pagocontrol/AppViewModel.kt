package com.devpaul.pagocontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.AppearanceRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.ThemeMode
import com.devpaul.pagocontrol.domain.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(auth: AuthRepository, private val appearance: AppearanceRepository, profile: ProfileRepository) : ViewModel() {
    val session = auth.session.flatMapLatest { user ->
        if (user == null) flowOf(null) else profile.observe(user.id).map { preferences ->
            user.copy(name = preferences.name ?: user.name, email = preferences.email ?: user.email)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), auth.session.value)
    val theme = appearance.themeMode.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)
    private val mutableThemeError = MutableStateFlow(false)
    val themeError = mutableThemeError.asStateFlow()

    fun selectTheme(index: Int) {
        val mode = ThemeMode.entries.getOrNull(index) ?: return
        viewModelScope.launch {
            try {
                appearance.setThemeMode(mode)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableThemeError.value = true
            }
        }
    }

    fun dismissThemeError() { mutableThemeError.value = false }
}
