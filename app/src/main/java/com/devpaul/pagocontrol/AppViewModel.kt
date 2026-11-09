package com.devpaul.pagocontrol

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.AppearanceRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class AppViewModel(auth: AuthRepository, private val appearance: AppearanceRepository) : ViewModel() {
    val session = auth.session
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
