package com.devpaul.pagocontrol.features.profile.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.profile.R

@Composable
internal fun ProfileDialogs(state: ProfileUiState, themeIndex: Int, onThemeSelected: (Int) -> Unit, onIntent: (ProfileUiIntent) -> Unit) {
    val dismiss = { onIntent(ProfileUiIntent.Dismiss) }
    state.dialog?.let { dialog ->
        val title = when (dialog) {
            ProfileDialog.Edit -> R.string.edit_profile
            ProfileDialog.Appearance -> R.string.choose_appearance
            ProfileDialog.Language -> R.string.choose_language
            ProfileDialog.Rating -> R.string.rate
            ProfileDialog.SignOut -> R.string.sign_out
            ProfileDialog.Notifications -> R.string.notifications
        }
        AlertDialog(
            onDismissRequest = dismiss,
            title = { Text(stringResource(title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (dialog) {
                        ProfileDialog.Edit -> {
                            Text(stringResource(R.string.edit_detail), style = MaterialTheme.typography.bodySmall)
                            OutlinedTextField(state.draftName, { onIntent(ProfileUiIntent.Name(it)) }, label = { Text(stringResource(R.string.name)) }, singleLine = true, enabled = !state.saving, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(
                                state.draftEmail, { onIntent(ProfileUiIntent.Email(it)) },
                                label = { Text(stringResource(R.string.email)) }, singleLine = true,
                                enabled = !state.saving, modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                                isError = state.draftEmail.isNotBlank() && !com.devpaul.pagocontrol.domain.LoginValidation.validEmail(state.draftEmail),
                            )
                        }
                        ProfileDialog.Appearance -> listOf(R.string.theme_system, R.string.theme_light, R.string.theme_dark).forEachIndexed { index, label ->
                            ChoiceRow(stringResource(label), themeIndex == index) { onThemeSelected(index); dismiss() }
                        }
                        ProfileDialog.Language -> {
                            ChoiceRow(stringResource(R.string.spanish), true, dismiss)
                            Text(stringResource(R.string.language_available), style = MaterialTheme.typography.bodySmall)
                        }
                        ProfileDialog.Rating -> {
                            Text(stringResource(R.string.rating_intro))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                (1..5).forEach { stars ->
                                    Column(
                                        Modifier.weight(1f).selectable(selected = state.stars == stars, enabled = !state.saving, role = Role.RadioButton, onClick = { onIntent(ProfileUiIntent.Stars(stars)) }).padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(if (state.stars >= stars) Icons.Filled.Star else Icons.Outlined.StarBorder, stringResource(R.string.stars, stars), Modifier.size(32.dp), tint = if (state.stars >= stars) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(stars.toString(), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            OutlinedTextField(
                                state.comment, { onIntent(ProfileUiIntent.Comment(it)) }, modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.comment)) }, minLines = 3, maxLines = 5, enabled = !state.saving,
                                supportingText = { Text(stringResource(R.string.comment_count, state.comment.length)) },
                            )
                            Text(stringResource(R.string.rating_local), style = MaterialTheme.typography.bodySmall)
                        }
                        ProfileDialog.SignOut -> Text(stringResource(R.string.sign_out_detail))
                        ProfileDialog.Notifications -> Text(stringResource(if (state.notificationsEnabled) R.string.notifications_on else R.string.notifications_off))
                    }
                    if (state.notice == ProfileNotice.SaveError) Text(stringResource(R.string.save_error), color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                when (dialog) {
                    ProfileDialog.Edit -> Button(onClick = { onIntent(ProfileUiIntent.SaveIdentity) }, enabled = state.canSaveIdentity && !state.saving) { Text(stringResource(R.string.save)) }
                    ProfileDialog.Rating -> Button(onClick = { onIntent(ProfileUiIntent.SaveRating) }, enabled = state.stars in 1..5 && !state.saving) { Text(stringResource(R.string.save_rating)) }
                    ProfileDialog.SignOut -> Button(onClick = { onIntent(ProfileUiIntent.SignOut) }, enabled = !state.saving) { Text(stringResource(R.string.sign_out)) }
                    else -> TextButton(onClick = dismiss) { Text(stringResource(R.string.close)) }
                }
            },
            dismissButton = {
                if (dialog in listOf(ProfileDialog.Edit, ProfileDialog.Rating, ProfileDialog.SignOut)) {
                    TextButton(onClick = dismiss, enabled = !state.saving) { Text(stringResource(R.string.cancel)) }
                }
            },
        )
    }
    if (state.notice != null && state.dialog == null) {
        val message = when (state.notice) {
            ProfileNotice.SavedRating -> R.string.rating_saved
            ProfileNotice.LoadError -> R.string.load_error
            ProfileNotice.SaveError -> R.string.save_error
            ProfileNotice.Upcoming -> R.string.upcoming
        }
        AlertDialog(
            onDismissRequest = dismiss, title = { Text(stringResource(R.string.profile_title)) },
            text = { Text(stringResource(message)) },
            confirmButton = {
                TextButton(onClick = { if (state.notice == ProfileNotice.LoadError) onIntent(ProfileUiIntent.Retry) else dismiss() }) {
                    Text(stringResource(if (state.notice == ProfileNotice.LoadError) R.string.retry else R.string.close))
                }
            },
        )
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
