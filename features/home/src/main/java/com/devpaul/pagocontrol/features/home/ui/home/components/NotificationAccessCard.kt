package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.R

@Composable
fun NotificationAccessCard(
    settingsFailed: Boolean,
    onActivate: () -> Unit,
    actionModifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = null)
            Text(
                stringResource(R.string.notifications_access_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(R.string.notifications_access_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.notifications_access_privacy),
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onActivate,
                modifier = actionModifier
            ) { Text(stringResource(R.string.notifications_access)) }
            if (settingsFailed) Text(
                stringResource(R.string.notifications_settings_error),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
