package com.devpaul.pagocontrol.features.home.ui.home.components.model

import androidx.compose.ui.geometry.Rect

data class HomeGuideAction(
    val bounds: Rect,
    val label: String,
    val enabled: Boolean = true,
    val checked: Boolean? = null,
    val onClick: () -> Unit,
)
