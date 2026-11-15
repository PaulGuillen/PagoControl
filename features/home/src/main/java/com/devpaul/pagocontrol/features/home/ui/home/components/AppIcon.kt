package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.ui.home.components.model.MonitoredApp

@Composable
fun AppIcon(app: MonitoredApp) {
    if (app.icon != null) {
        Image(bitmap = app.icon, contentDescription = null, modifier = Modifier.size(48.dp))
    } else {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier.padding(12.dp).size(24.dp)
            )
        }
    }
}
