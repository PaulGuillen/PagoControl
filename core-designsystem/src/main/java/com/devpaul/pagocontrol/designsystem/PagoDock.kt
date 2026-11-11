package com.devpaul.pagocontrol.designsystem

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PagoDock(movementsSelected: Boolean, onHome: () -> Unit, onMovements: () -> Unit, profileSelected: Boolean = false, onProfile: () -> Unit = {}, reportsSelected: Boolean = false, onReports: () -> Unit = {}) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        DockItem(icon = Icons.Outlined.Home, activeIcon = Icons.Filled.Home, label = stringResource(R.string.dock_home), selected = !movementsSelected && !profileSelected && !reportsSelected, onClick = onHome)
        DockItem(icon = Icons.AutoMirrored.Outlined.ReceiptLong, activeIcon = Icons.AutoMirrored.Filled.ReceiptLong, label = stringResource(R.string.dock_movements), selected = movementsSelected, onClick = onMovements)
        DockItem(icon = Icons.Outlined.Insights, activeIcon = Icons.Filled.Insights, label = stringResource(R.string.dock_reports), selected = reportsSelected, onClick = onReports)
        DockItem(icon = Icons.Outlined.Person, activeIcon = Icons.Filled.Person, label = stringResource(R.string.dock_profile), selected = profileSelected, onClick = onProfile)
    }
}

@Composable
private fun RowScope.DockItem(icon: ImageVector, activeIcon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = if (selected) activeIcon else icon, contentDescription = null) },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}
