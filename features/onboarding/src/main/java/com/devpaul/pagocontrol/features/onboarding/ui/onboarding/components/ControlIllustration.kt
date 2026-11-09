package com.devpaul.pagocontrol.features.onboarding.ui.onboarding.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.EntryColors
import com.devpaul.pagocontrol.features.onboarding.R

@Composable
internal fun ControlIllustration() {
    val positive = if (EntryColors.isDark) EntryColors.Mint else EntryColors.Green
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = EntryColors.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, EntryColors.border),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = positive,
                    )
                    Text(stringResource(R.string.onboarding_balance), style = MaterialTheme.typography.labelMedium)
                }
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = EntryColors.Green,
                    contentColor = Color.White,
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_balance_sources),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Text("S/ 4,580.50", style = MaterialTheme.typography.displaySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.onboarding_upcoming_debits), style = MaterialTheme.typography.labelMedium)
                Text(
                    text = stringResource(R.string.onboarding_this_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                )
            }
            DebitPreview(
                title = stringResource(R.string.onboarding_netflix),
                detail = stringResource(R.string.onboarding_due),
                amount = "-S/ 44.90",
                icon = Icons.Outlined.Movie,
                color = EntryColors.Red,
            )
            DebitPreview(
                title = stringResource(R.string.onboarding_electricity),
                detail = stringResource(R.string.onboarding_notice_example),
                amount = "-S/ 115.80",
                icon = Icons.Outlined.Bolt,
                color = EntryColors.Amber,
            )
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (EntryColors.isDark) Color(0xFF242936) else Color(0xFFF0F3EC),
                border = BorderStroke(1.dp, EntryColors.border),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = positive,
                        )
                        Text(
                            text = stringResource(R.string.onboarding_saving),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(text = "66.8%", color = positive, style = MaterialTheme.typography.titleMedium)
                    }
                    LinearProgressIndicator(
                        progress = { 0.668f },
                        modifier = Modifier.fillMaxWidth(),
                        color = positive,
                        trackColor = EntryColors.border,
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(stringResource(R.string.onboarding_spent), style = MaterialTheme.typography.labelSmall)
                        Text(stringResource(R.string.onboarding_goal), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Text(
                text = stringResource(R.string.onboarding_preview),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DebitPreview(title: String, detail: String, amount: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (EntryColors.isDark) Color(0xFF242936) else Color(0xFFF0F3EC),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, EntryColors.border),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = MaterialTheme.shapes.small, color = color, contentColor = Color.White) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(22.dp),
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(amount, style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.onboarding_demo_status), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
