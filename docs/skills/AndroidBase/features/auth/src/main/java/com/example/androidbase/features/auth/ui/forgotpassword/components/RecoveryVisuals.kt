package com.example.androidbase.features.auth.ui.forgotpassword.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.androidbase.designsystem.EntryColors
import com.example.androidbase.features.auth.R

@Composable
internal fun RecoverySecurityBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = if (EntryColors.isDark) Color(0xFF171C25) else Color(0xFFEDEFE8),
            border = if (EntryColors.isDark) BorderStroke(1.dp, EntryColors.border) else null,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.flow_back))
            }
        }
        Surface(
            shape = CircleShape,
            color = if (EntryColors.isDark) EntryColors.card else Color(0xFFEDEFE8),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            border = if (EntryColors.isDark) BorderStroke(1.dp, EntryColors.border) else null,
        ) {
            Text(
                text = stringResource(R.string.flow_account_security),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun RecoveryEmblem() {
    Box(
        modifier = Modifier.size(144.dp).drawWithCache {
            val glow = Brush.radialGradient(
                colors = listOf(EntryColors.Gold.copy(alpha = 0.2f), Color.Transparent),
                radius = size.width * 0.8f,
            )
            onDrawBehind { drawCircle(brush = glow, radius = size.width * 0.8f) }
        },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(EntryColors.Gold, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.size(84.dp),
                shape = CircleShape,
                color = if (EntryColors.isDark) Color(0xFF171600) else Color(0xFFFFF7DA),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LockReset,
                    contentDescription = null,
                    modifier = Modifier.padding(22.dp),
                    tint = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(34.dp),
            shape = CircleShape,
            color = EntryColors.Green,
            border = BorderStroke(1.dp, if (EntryColors.isDark) Color(0xFF0D1011) else Color.White),
        ) {
            Icon(
                imageVector = Icons.Outlined.VpnKey,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
internal fun RecoveryNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (EntryColors.isDark) EntryColors.card else Color(0xFFF3F4ED),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (EntryColors.isDark) BorderStroke(1.dp, EntryColors.border) else null,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = if (EntryColors.isDark) Color(0xFF242936) else Color(0xFFE5E9DF),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                    tint = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                )
            }
            Text(
                text = stringResource(R.string.flow_recovery_delivery_hint),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun RecoveryProtection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (EntryColors.isDark) Color(0xFF171C25) else Color(0xFFEDEFE8),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = if (EntryColors.isDark) BorderStroke(1.dp, EntryColors.border) else null,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (EntryColors.isDark) Color(0xFF242936) else Color(0xFFE1E5DC),
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(26.dp),
                    tint = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF594F32),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(R.string.flow_recovery_protection), style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.flow_security_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
