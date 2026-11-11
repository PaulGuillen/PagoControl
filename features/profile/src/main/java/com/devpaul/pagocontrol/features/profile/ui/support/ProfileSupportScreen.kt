package com.devpaul.pagocontrol.features.profile.ui.support

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.profile.R

@Composable
fun ProfileSupportScreen(privacy: Boolean, onBack: () -> Unit) {
    val title = stringResource(if (privacy) R.string.privacy else R.string.help)
    val articles = if (privacy) listOf(
        R.string.privacy_account_title to R.string.privacy_account_body,
        R.string.privacy_movements_title to R.string.privacy_movements_body,
        R.string.privacy_preferences_title to R.string.privacy_preferences_body,
        R.string.privacy_rating_title to R.string.privacy_rating_body,
        R.string.privacy_control_title to R.string.privacy_control_body,
    ) else listOf(
        R.string.faq_start_title to R.string.faq_start_body,
        R.string.faq_search_title to R.string.faq_search_body,
        R.string.faq_pending_title to R.string.faq_pending_body,
        R.string.faq_notifications_title to R.string.faq_notifications_body,
        R.string.faq_appearance_title to R.string.faq_appearance_body,
        R.string.faq_rating_title to R.string.faq_rating_body,
    )
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                Modifier.widthIn(max = 840.dp).fillMaxSize(), contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(if (privacy) Icons.Outlined.Shield else Icons.AutoMirrored.Outlined.HelpOutline, null, Modifier.size(32.dp))
                            Text(stringResource(if (privacy) R.string.privacy_intro_title else R.string.help_intro_title), style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(if (privacy) R.string.privacy_intro_body else R.string.help_intro_body), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (!privacy) item { Text(stringResource(R.string.faq), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                articles.forEachIndexed { index, (heading, body) ->
                    item(key = heading) {
                        if (privacy) {
                            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(stringResource(heading), style = MaterialTheme.typography.titleMedium)
                                    Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            var expanded by rememberSaveable { mutableStateOf(index == 0) }
                            val expansionLabel = stringResource(if (expanded) R.string.expanded else R.string.collapsed)
                            Surface(
                                onClick = { expanded = !expanded },
                                modifier = Modifier.fillMaxWidth().animateContentSize().semantics { stateDescription = expansionLabel },
                                shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface,
                            ) {
                                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(stringResource(heading), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                                        Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null)
                                    }
                                    if (expanded) Text(stringResource(body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
