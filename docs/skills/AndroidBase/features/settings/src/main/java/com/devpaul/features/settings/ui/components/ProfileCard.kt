package com.devpaul.features.settings.ui.components

import com.devpaul.core.platform.analytics.trackedClick
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.features.settings.R
import com.devpaul.features.settings.ui.util.profileInitials
import com.devpaul.core.platform.theme.CvFreeTheme

@Composable
internal fun ProfileCard(
    userName: String,
    userEmail: String,
    isPremium: Boolean,
    onEdit: () -> Unit,
) {
    val visibleName = userName.ifBlank { stringResource(R.string.settings_user) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profileInitials(visibleName),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = visibleName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = userEmail.ifBlank { stringResource(R.string.settings_email_unavailable) },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(10.dp))
                AccountPlanBadge(isPremium = isPremium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedButton(
                onClick = trackedClick("edit_profile_name", onEdit),
                contentPadding = PaddingValues(horizontal = 14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(stringResource(R.string.settings_edit), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AccountPlanBadge(isPremium: Boolean) {
    val containerColor = if (isPremium) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    val contentColor = if (isPremium) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isPremium) Icons.Outlined.WorkspacePremium else Icons.Outlined.PersonOutline,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(if (isPremium) R.string.settings_plan_premium else R.string.settings_plan_free),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(name = "Profile Free Light", showBackground = true, widthDp = 420)
@Composable
private fun ProfileFreePreview() {
    CvFreeTheme(darkTheme = false) {
        ProfileCard("Paul Guillen", "paul@example.com", isPremium = false, onEdit = {})
    }
}

@Preview(name = "Profile Premium Light", showBackground = true, widthDp = 420)
@Composable
private fun ProfilePremiumPreview() {
    CvFreeTheme(darkTheme = false) {
        ProfileCard("Paul Guillen", "paul@example.com", isPremium = true, onEdit = {})
    }
}

@Preview(name = "Profile Premium Dark", showBackground = true, widthDp = 420)
@Composable
private fun ProfilePremiumDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        ProfileCard("Paul Guillen", "paul@example.com", isPremium = true, onEdit = {})
    }
}
