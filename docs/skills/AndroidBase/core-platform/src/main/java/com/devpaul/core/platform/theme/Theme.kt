package com.devpaul.core.platform.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppDarkTheme = staticCompositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = CvBlueDark,
    onPrimary = CvDarkBackground,
    primaryContainer = ColorBlue900,
    onPrimaryContainer = CvDarkText,
    secondary = CvDarkMuted,
    onSecondary = CvDarkBackground,
    tertiary = CvTerracottaDark,
    onTertiary = CvDarkBackground,
    background = CvDarkBackground,
    onBackground = CvDarkText,
    surface = CvDarkSurface,
    onSurface = CvDarkText,
    surfaceDim = CvDarkBackground,
    surfaceBright = CvDarkSurfaceVariant,
    surfaceContainerLowest = CvDarkBackground,
    surfaceContainerLow = CvDarkSurface,
    surfaceContainer = CvDarkSurface,
    surfaceContainerHigh = CvDarkSurfaceVariant,
    surfaceContainerHighest = CvDarkSurfaceVariant,
    surfaceVariant = CvDarkSurfaceVariant,
    onSurfaceVariant = CvDarkMuted,
    outline = CvDarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = CvBlue,
    onPrimary = White,
    primaryContainer = ColorBlue100,
    onPrimaryContainer = ColorBlue950,
    secondary = CvInk,
    onSecondary = White,
    tertiary = CvTerracotta,
    onTertiary = White,
    background = CvLightBackground,
    onBackground = ColorBlue950,
    surface = CvLightSurface,
    onSurface = ColorBlue950,
    surfaceDim = CvLightOutline,
    surfaceBright = CvLightSurface,
    surfaceContainerLowest = CvLightSurface,
    surfaceContainerLow = CvLightBackground,
    surfaceContainer = CvLightSurface,
    surfaceContainerHigh = CvLightSurface,
    surfaceContainerHighest = ColorBlue50,
    surfaceVariant = ColorBlue50,
    onSurfaceVariant = CvLightMuted,
    outline = CvLightOutline,
)

@Composable
fun CvFreeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CvFreeTypography,
            content = content,
        )
    }
}
