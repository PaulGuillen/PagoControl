package com.devpaul.pagocontrol.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun PagoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier, shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(PagoSpacing.medium), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
fun PagoBrand(subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
            Icon(
                Icons.Outlined.AccountBalanceWallet, contentDescription = null,
                modifier = Modifier.padding(10.dp).size(24.dp), tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Column {
            Text(stringResource(R.string.brand_name), style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AppearanceMenu(selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val choices = listOf(R.string.theme_system, R.string.theme_light, R.string.theme_dark)
    val selectedLabel = stringResource(choices.getOrElse(selectedIndex) { R.string.theme_system })
    Column {
        IconButton(onClick = { expanded = true }, modifier = Modifier.semantics { stateDescription = selectedLabel }) {
            Icon(Icons.Outlined.Contrast, stringResource(R.string.appearance))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            choices.forEachIndexed { index, resource ->
                DropdownMenuItem(
                    text = { Text(stringResource(resource), fontWeight = if (selectedIndex == index) androidx.compose.ui.text.font.FontWeight.Bold else null) },
                    onClick = { expanded = false; onSelect(index) },
                )
            }
        }
    }
}

fun formatSoles(cents: Long, signed: Boolean = false): String {
    val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
    val amount = java.math.BigDecimal.valueOf(cents).movePointLeft(2).abs()
    val prefix = when { cents < 0 -> "- "; signed && cents > 0 -> "+ "; else -> "" }
    return "${prefix}S/ ${formatter.format(amount)}"
}

private fun institutionColor(source: String): Color? = when (source) {
    "Yape" -> Color(0xFF742284)
    "Plin" -> Color(0xFF00B5E2)
    "BCP" -> Color(0xFF002A8F)
    "Interbank" -> Color(0xFF009B3A)
    "BBVA" -> Color(0xFF004481)
    else -> null
}

@Composable
fun InstitutionAvatar(source: String) {
    val brandColor = institutionColor(source)
    Surface(
        shape = CircleShape,
        color = brandColor?.copy(alpha = 0.12f) ?: MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Text(
                if (source == "BCP") source else source.take(1),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
fun InstitutionBadge(source: String) {
    val brandColor = institutionColor(source)
    Surface(
        shape = CircleShape,
        color = brandColor ?: MaterialTheme.colorScheme.surfaceVariant,
        contentColor = when {
            source == "Plin" -> Color(0xFF002A35)
            brandColor != null -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(source, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}
