package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.devpaul.pagocontrol.features.reports.R

@Composable
internal fun ReportChartTabs(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(R.string.reports_tab_flow, R.string.reports_tab_categories, R.string.reports_tab_channels)
    val icons = listOf(Icons.Outlined.DonutLarge, Icons.Outlined.Category, Icons.Outlined.AccountBalanceWallet)
    ReportOptionTabs(labels, icons, selected, onSelect)
}

@Composable
internal fun ReportOptionTabs(labels: List<Int>, icons: List<ImageVector>, selected: Int, onSelect: (Int) -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEachIndexed { index, label ->
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight().selectable(selected = selected == index, role = Role.Tab, onClick = { onSelect(index) }),
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (selected == index) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Column(Modifier.fillMaxHeight().padding(horizontal = 4.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
                        Icon(icons[index], null, Modifier.size(20.dp))
                        Text(stringResource(label), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
