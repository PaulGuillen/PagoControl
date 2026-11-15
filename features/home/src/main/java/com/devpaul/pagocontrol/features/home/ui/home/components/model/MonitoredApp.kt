package com.devpaul.pagocontrol.features.home.ui.home.components.model

import androidx.compose.ui.graphics.ImageBitmap

data class MonitoredApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap? = null
)
