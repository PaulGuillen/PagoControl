package com.example.androidbase.features.onboarding.ui.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbase.designsystem.EntryColors
import com.example.androidbase.features.onboarding.R

@Composable
internal fun PrivacyIllustration() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.widthIn(max = 340.dp).fillMaxWidth().aspectRatio(1f)) {
            val border = EntryColors.border
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(EntryColors.Gold.copy(alpha = 0.1f), Color.Transparent)),
                    radius = size.minDimension * 0.45f,
                )
                listOf(0.3f, 0.45f).forEach { fraction ->
                    drawCircle(
                        color = border,
                        radius = size.minDimension * fraction,
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 6.dp.toPx())),
                        ),
                    )
                }
            }
            SourceBadge(
                label = stringResource(R.string.onboarding_provider_yape),
                icon = Icons.Outlined.Smartphone,
                color = EntryColors.Purple,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 12.dp),
            )
            SourceBadge(
                label = stringResource(R.string.onboarding_provider_plin),
                icon = Icons.Outlined.QrCode2,
                color = EntryColors.Cyan,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 20.dp),
            )
            SourceBadge(
                label = stringResource(R.string.onboarding_banks),
                icon = Icons.Outlined.AccountBalance,
                color = EntryColors.Blue,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 12.dp),
            )
            SourceBadge(
                label = stringResource(R.string.onboarding_notifications),
                icon = Icons.Outlined.NotificationsActive,
                color = EntryColors.Green,
                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 20.dp),
            )
            Box(Modifier.align(Alignment.Center).size(128.dp)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = EntryColors.card,
                    border = BorderStroke(8.dp, EntryColors.Gold.copy(alpha = 0.1f)),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        modifier = Modifier.padding(36.dp),
                        tint = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                    )
                }
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 8.dp),
                    shape = CircleShape,
                    color = EntryColors.Green,
                    contentColor = Color.White,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_permission_control),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
        PrivacyItem(Icons.Outlined.Password, R.string.onboarding_privacy_1)
        PrivacyItem(Icons.Outlined.CloudOff, R.string.onboarding_privacy_2)
        PrivacyItem(Icons.Outlined.Security, R.string.onboarding_privacy_3)
    }
}

@Composable
private fun SourceBadge(label: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = EntryColors.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, EntryColors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = color, contentColor = Color.White) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(5.dp).size(16.dp),
                    tint = Color.White,
                )
            }
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PrivacyItem(icon: ImageVector, label: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = EntryColors.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, EntryColors.border),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = EntryColors.Green, contentColor = Color.White) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(7.dp).size(20.dp),
                    tint = Color.White,
                )
            }
            Text(
                text = stringResource(label),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (EntryColors.isDark) EntryColors.Mint else EntryColors.Green,
            )
        }
    }
}
