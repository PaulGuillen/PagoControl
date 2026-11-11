package com.devpaul.pagocontrol.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.devpaul.pagocontrol.R

@Composable
fun ForceUpdateScreen(onOpenStore: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = rememberForceUpdateColors(isDark)

    val transition = rememberInfiniteTransition(label = "pulse")
    val dotScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .background(colors.card)
        ) {
            ForceUpdateHeader(colors, dotScale)
            ForceUpdateBody(colors, isDark, onOpenStore)
        }
    }
}

@Composable
private fun ForceUpdateHeader(
    colors: ForceUpdateColors,
    dotScale: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.header)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ForceUpdateBadge(colors, dotScale)

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.iconBackground)
                .border(1.5.dp, colors.iconBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun ForceUpdateBadge(
    colors: ForceUpdateColors,
    dotScale: Float
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .scale(dotScale)
                .clip(CircleShape)
                .background(colors.pulse)
        )

        Text(
            text = stringResource(R.string.update_available),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun ForceUpdateBody(
    colors: ForceUpdateColors,
    isDark: Boolean,
    onOpenStore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.update_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.title,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.update_description),
            style = MaterialTheme.typography.bodySmall,
            color = colors.description,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        HorizontalDivider(color = colors.divider)

        Spacer(Modifier.height(16.dp))

        ForceUpdateFeature(Icons.Default.Security, stringResource(R.string.update_security), colors, isDark)
        ForceUpdateFeature(Icons.Default.Bolt, stringResource(R.string.update_performance), colors, isDark)
        ForceUpdateFeature(Icons.Default.NewReleases, stringResource(R.string.update_features), colors, isDark)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenStore,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.update_action),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.update_caption),
            style = MaterialTheme.typography.labelSmall,
            color = colors.caption,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ForceUpdateFeature(
    icon: ImageVector,
    text: String,
    colors: ForceUpdateColors,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent.copy(alpha = if (isDark) 0.16f else 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(17.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.featureText
        )
    }
}

@Composable
private fun rememberForceUpdateColors(isDark: Boolean): ForceUpdateColors {
    val scheme = MaterialTheme.colorScheme
    return ForceUpdateColors(
        background = scheme.background,
        card = scheme.surface,
        header = scheme.primaryContainer,
        accent = scheme.primary,
        title = scheme.onSurface,
        description = scheme.onSurfaceVariant,
        featureText = scheme.onSurface,
        caption = scheme.onSurfaceVariant,
        border = scheme.outlineVariant,
        divider = scheme.outlineVariant,
        iconBackground = scheme.primary.copy(alpha = if (isDark) 0.20f else 0.12f),
        iconBorder = scheme.primary.copy(alpha = 0.30f),
        pulse = scheme.primary,
    )
}

@Composable
fun ForceUpdateScreenDefault() {
    val context = LocalContext.current
    ForceUpdateScreen { openPlayStore(context) }
}

fun openPlayStore(context: Context) {
    val appPackageName = context.packageName

    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "market://details?id=$appPackageName".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (error: Exception) {
        AppAnalytics.error("open_play_store", error)
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

@Preview(
    name = "ForceUpdate - Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "ForceUpdate - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ForceUpdatePreview() {
    PagoControlTheme(
        darkTheme = isSystemInDarkTheme(),
    ) {
        ForceUpdateScreenDefault()
    }
}
