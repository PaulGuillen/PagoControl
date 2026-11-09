package com.devpaul.pagocontrol.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

object EntryColors {
    val Gold = Color(0xFFF6C63E)
    val Purple = Color(0xFF7C218B)
    val Cyan = Color(0xFF007C9C)
    val Green = Color(0xFF006B4D)
    val Blue = Color(0xFF235C9C)
    val Red = Color(0xFF8E2027)
    val Amber = Color(0xFF725800)
    val Mint = Color(0xFF31D49B)

    val isDark: Boolean
        @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val card: Color
        @Composable get() = if (isDark) Color(0xFF1B202A) else Color.White

    val border: Color
        @Composable get() = if (isDark) Color(0xFF343942) else Color(0xFFE1E5DD)

    val background: Color
        @Composable get() = if (isDark) Color(0xFF0D1011) else Color(0xFFF9FAF3)
}
