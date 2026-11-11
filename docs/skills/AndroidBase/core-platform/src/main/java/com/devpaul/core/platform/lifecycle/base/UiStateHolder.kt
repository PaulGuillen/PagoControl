package com.devpaul.core.platform.lifecycle.base

import kotlinx.coroutines.flow.StateFlow

interface UiStateHolder<UiState> {
    val uiState: StateFlow<UiState>
}
