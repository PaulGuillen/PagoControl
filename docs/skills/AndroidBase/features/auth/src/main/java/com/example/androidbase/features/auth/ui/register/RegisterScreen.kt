package com.example.androidbase.features.auth.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidbase.designsystem.BaseTheme
import com.example.androidbase.features.auth.R
import com.example.androidbase.designsystem.AccountLink
import com.example.androidbase.features.auth.ui.components.*

@Composable
fun RegisterScreen(
    state: RegisterUiState, onIntent: (RegisterUiIntent) -> Unit, onLogin: () -> Unit,
    themeIndex: Int = 0, onThemeSelected: (Int) -> Unit = {},
) {
    var providerInfo by rememberSaveable { mutableStateOf(false) }
    var legalInfo by rememberSaveable { mutableStateOf(false) }
    AuthLayout(onBack = onLogin, themeIndex = themeIndex, onThemeSelected = onThemeSelected) {
        AuthHeading(stringResource(R.string.flow_register_title), stringResource(R.string.flow_register_description))
        if (state.completed) {
            Text(stringResource(if (state.demoOnly) R.string.flow_register_demo else R.string.flow_register_success))
            AuthPrimaryButton(text = stringResource(R.string.flow_back_login), loading = false, onClick = onLogin)
        } else {
            AuthProviderButton { providerInfo = true }
            AuthDivider(stringResource(R.string.flow_register_separator))
            AuthField(
                value = state.name, onValueChange = { onIntent(RegisterUiIntent.NameChanged(it)) }, label = stringResource(R.string.flow_name),
                error = state.validationAttempted && state.name.trim().length < 2, enabled = !state.loading,
            )
            AuthField(
                value = state.email, onValueChange = { onIntent(RegisterUiIntent.EmailChanged(it)) }, label = stringResource(R.string.flow_email),
                error = state.validationAttempted && !state.email.trim().matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")),
                enabled = !state.loading, keyboardType = KeyboardType.Email,
            )
            AuthField(
                value = state.password, onValueChange = { onIntent(RegisterUiIntent.PasswordChanged(it)) }, label = stringResource(R.string.flow_password),
                error = state.validationAttempted && state.password.length < 8, enabled = !state.loading,
                password = true, visible = state.passwordVisible,
                onToggleVisibility = { onIntent(RegisterUiIntent.TogglePassword) },
                onSubmit = { onIntent(RegisterUiIntent.Submit) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val strength = listOf(state.password.length >= 8, state.password.any(Char::isUpperCase), state.password.any(Char::isDigit), state.password.any { !it.isLetterOrDigit() }).count { it }
                repeat(4) { index ->
                    Box(Modifier.weight(1f).height(4.dp).background(
                        if (index < strength) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.shapes.small,
                    ))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().toggleable(
                    value = state.termsAccepted,
                    enabled = !state.loading,
                    role = Role.Checkbox,
                    onValueChange = { onIntent(RegisterUiIntent.TermsChanged(it)) },
                ).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = state.termsAccepted, onCheckedChange = null, enabled = !state.loading)
                Text(
                    text = stringResource(R.string.flow_accept_terms),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = { legalInfo = true }) { Text(stringResource(R.string.flow_terms_link), color = MaterialTheme.colorScheme.onSurface) }
            state.error?.let { error ->
                Text(stringResource(when (error) {
                    RegisterError.InvalidInput -> R.string.flow_register_invalid
                    RegisterError.TermsRequired -> R.string.flow_terms_required
                    RegisterError.Network -> R.string.flow_network_error
                    RegisterError.Unavailable -> R.string.flow_unavailable
                }), color = MaterialTheme.colorScheme.error)
            }
            AuthPrimaryButton(text = stringResource(R.string.flow_create_account), loading = state.loading) { onIntent(RegisterUiIntent.Submit) }
            AccountLink(
                text = stringResource(R.string.flow_already_account),
                action = stringResource(R.string.flow_login_button),
                onClick = onLogin,
                enabled = !state.loading,
            )
        }
    }
    if (providerInfo) AuthInfoDialog(stringResource(R.string.flow_provider_info)) { providerInfo = false }
    if (legalInfo) AuthInfoDialog(stringResource(R.string.flow_terms_info)) { legalInfo = false }
}

@Preview(showBackground = true)
@Composable
private fun RegisterLightPreview() { BaseTheme(false) { RegisterScreen(state = RegisterUiState(), onIntent = {}, onLogin = {}) } }

@Preview(showBackground = true)
@Composable
private fun RegisterDarkPreview() { BaseTheme(true) { RegisterScreen(state = RegisterUiState(), onIntent = {}, onLogin = {}) } }
