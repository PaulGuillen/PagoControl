package com.devpaul.core.platform.lifecycle.base

import kotlinx.coroutines.flow.SharedFlow

interface UiEventHolder<UiEvent> {
    val uiEvents: SharedFlow<UiEvent>
}
