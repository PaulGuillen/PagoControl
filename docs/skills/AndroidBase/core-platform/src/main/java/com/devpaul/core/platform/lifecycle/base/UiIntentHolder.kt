package com.devpaul.core.platform.lifecycle.base

interface UiIntentHolder<UiIntent> {
    fun executeUiIntent(intent: UiIntent)

    suspend fun onUiIntent(intent: UiIntent)
}
