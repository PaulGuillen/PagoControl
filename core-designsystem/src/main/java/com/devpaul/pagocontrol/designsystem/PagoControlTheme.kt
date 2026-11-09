package com.devpaul.pagocontrol.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFFF4C542), onPrimary = Color(0xFF171A16),
    primaryContainer = Color(0xFFF4C542), onPrimaryContainer = Color(0xFF241A00),
    secondary = Color(0xFF16845B), onSecondary = Color.White,
    secondaryContainer = Color(0xFF94F6C4), onSecondaryContainer = Color(0xFF005236),
    tertiary = Color(0xFFAD322E), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD6), onTertiaryContainer = Color(0xFF93000A),
    background = Color(0xFFFAFAF7), onBackground = Color(0xFF171A16),
    surface = Color.White, onSurface = Color(0xFF171A16),
    surfaceVariant = Color(0xFFF4F4EF), onSurfaceVariant = Color(0xFF5C6258),
    surfaceContainer = Color(0xFFF4F4EF), surfaceContainerLow = Color(0xFFF4F4EF),
    surfaceContainerHigh = Color(0xFFE7E9E1), surfaceContainerHighest = Color(0xFFE1E3DC),
    outline = Color(0xFF73776F), outlineVariant = Color(0xFFE7E9E1),
    error = Color(0xFFBA1A1A), onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFD45A), onPrimary = Color(0xFF111410),
    primaryContainer = Color(0xFFFFD45A), onPrimaryContainer = Color(0xFF111410),
    secondary = Color(0xFF55D59C), onSecondary = Color(0xFF002113),
    secondaryContainer = Color(0xFF173C2C), onSecondaryContainer = Color(0xFF94F6C4),
    tertiary = Color(0xFFFF7777), onTertiary = Color(0xFF410004),
    tertiaryContainer = Color(0xFF472725), onTertiaryContainer = Color(0xFFFFB3AE),
    background = Color(0xFF111410), onBackground = Color(0xFFF5F6F0),
    surface = Color(0xFF1B201A), onSurface = Color(0xFFF5F6F0),
    surfaceVariant = Color(0xFF252B23), onSurfaceVariant = Color(0xFFA8B0A4),
    surfaceContainer = Color(0xFF252B23), surfaceContainerLow = Color(0xFF1B201A),
    surfaceContainerHigh = Color(0xFF252B23), surfaceContainerHighest = Color(0xFF353C33),
    outline = Color(0xFFA8B0A4), outlineVariant = Color(0xFF353C33),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
)

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun manropeFont(weight: FontWeight) = Font(
    R.font.manrope, weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Manrope = FontFamily(
    manropeFont(FontWeight.Normal), manropeFont(FontWeight.Medium),
    manropeFont(FontWeight.SemiBold), manropeFont(FontWeight.Bold), manropeFont(FontWeight.ExtraBold),
)

private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = Manrope, fontSize = size.sp, lineHeight = height.sp, fontWeight = weight,
    fontFeatureSettings = "tnum",
)

private val PagoTypography = Typography(
    displaySmall = type(38, 44, FontWeight.ExtraBold),
    headlineLarge = type(28, 36, FontWeight.Bold),
    headlineMedium = type(22, 28, FontWeight.Bold),
    headlineSmall = type(18, 24, FontWeight.SemiBold),
    titleLarge = type(22, 28, FontWeight.Bold),
    titleMedium = type(16, 22, FontWeight.SemiBold),
    titleSmall = type(14, 20, FontWeight.SemiBold),
    bodyLarge = type(16, 24), bodyMedium = type(14, 20), bodySmall = type(12, 16),
    labelLarge = type(14, 20, FontWeight.SemiBold),
    labelMedium = type(12, 16, FontWeight.SemiBold),
    labelSmall = type(10, 14, FontWeight.Bold),
)

object PagoSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
}

@Composable
fun PagoControlTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PagoTypography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
        ),
        content = content,
    )
}
