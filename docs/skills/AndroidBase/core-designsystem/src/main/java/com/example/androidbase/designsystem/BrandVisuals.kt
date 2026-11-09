package com.example.androidbase.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BrandMark(modifier: Modifier = Modifier, large: Boolean = false) {
    Surface(
        modifier = modifier, color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(if (large) 32.dp else 24.dp),
    ) {
        Box(modifier = Modifier.padding(if (large) 28.dp else 18.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Outlined.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(if (large) 64.dp else 36.dp))
        }
    }
}

@Composable
fun AmbientSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.background(Brush.radialGradient(
            colors = listOf(colors.primary.copy(alpha = 0.14f), colors.background, colors.secondary.copy(alpha = 0.07f)),
            radius = 1200f,
        )),
        content = content,
    )
}

@Composable
fun SplashSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val dark = EntryColors.isDark
    val background = EntryColors.background
    Box(
        modifier = modifier.drawWithCache {
            val gold = Brush.radialGradient(
                colors = listOf(EntryColors.Gold.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.08f),
                radius = size.maxDimension * 0.65f,
            )
            val green = Brush.radialGradient(
                colors = listOf(EntryColors.Mint.copy(alpha = if (dark) 0.16f else 0.12f), Color.Transparent),
                center = Offset(size.width * 0.95f, size.height * 0.48f),
                radius = size.maxDimension * 0.52f,
            )
            val bottom = Brush.radialGradient(
                colors = listOf(EntryColors.Gold.copy(alpha = if (dark) 0.12f else 0.1f), Color.Transparent),
                center = Offset(size.width * 0.5f, size.height * 1.02f),
                radius = size.maxDimension * 0.5f,
            )
            onDrawBehind {
                drawRect(background)
                drawRect(gold)
                drawRect(green)
                drawRect(bottom)
            }
        },
        content = content,
    )
}
