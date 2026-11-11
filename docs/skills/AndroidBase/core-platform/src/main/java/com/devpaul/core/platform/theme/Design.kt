package com.devpaul.core.platform.theme

import android.app.Activity
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

fun Activity.setStatusBarColor(color: Color, darkIcons: Boolean = false) {
    val window: Window = window
    window.statusBarColor = color.toArgb()
    window.navigationBarColor = color.toArgb()
    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = darkIcons
        isAppearanceLightNavigationBars = darkIcons
    }
}

@Composable
fun SetStatusBarColor(color: Color, darkIcons: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity
    SideEffect {
        activity?.setStatusBarColor(color, darkIcons)
    }
}
