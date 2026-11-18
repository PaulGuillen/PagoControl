package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.InstitutionBadge
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.designsystem.movementLabels
import com.devpaul.pagocontrol.features.home.R
import com.devpaul.pagocontrol.features.home.domain.entity.Movement

@Composable
fun MovementRow(
    movement: Movement,
    deleting: Boolean = false,
    onDelete: (() -> Unit)? = null,
) {
    var showNotification by rememberSaveable(movement.id) { mutableStateOf(false) }
    if (showNotification) {
        MovementNotificationDialog(movement = movement, onDismiss = { showNotification = false })
    }
    val labels = movementLabels(movement.title, movement.source, movement.packageName, movement.notificationTitle, movement.notificationText)
    PagoCard(Modifier.fillMaxWidth().clickable(
        role = Role.Button,
        onClickLabel = stringResource(R.string.movement_notification_open),
        onClick = { showNotification = true },
    )) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MovementAvatar(movement.source, movement.packageName)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = labels.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    InstitutionBadge(labels.institution, neutral = labels.fromGmail, packageName = movement.packageName)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = movement.detail,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatSoles(movement.amountCents, signed = !movement.ownTransfer),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (!movement.ownTransfer && movement.amountCents > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, enabled = !deleting) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(
                            R.string.gmail_delete_action,
                            movement.title
                        )
                    )
                }
            }
        }
        if (movement.ownTransfer) {
            Text(
                stringResource(R.string.home_own_transfer),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
