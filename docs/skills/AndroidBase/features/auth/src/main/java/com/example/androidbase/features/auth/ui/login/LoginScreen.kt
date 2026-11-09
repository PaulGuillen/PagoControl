package com.example.androidbase.features.auth.ui.login

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.androidbase.designsystem.BaseTheme
import com.example.androidbase.features.auth.R
import com.example.androidbase.features.auth.ui.components.*

@Composable
fun LoginScreen(
    state: LoginUiState, onIntent: (LoginUiIntent) -> Unit, themeIndex: Int, onThemeSelected: (Int) -> Unit,
    onRegister: () -> Unit = {}, onForgotPassword: () -> Unit = {},
) {
    var providerInfo by rememberSaveable { mutableStateOf(false) }
    AuthLayout(themeIndex = themeIndex, onThemeSelected = onThemeSelected) {
        AuthHeading(stringResource(R.string.flow_login_title), stringResource(R.string.flow_login_description))
        AuthProviderButton { providerInfo = true }
        AuthDivider(stringResource(R.string.flow_login_separator))
        AuthField(
            value = state.email, onValueChange = { onIntent(LoginUiIntent.EmailChanged(it)) }, label = stringResource(R.string.flow_email),
            error = state.emailInvalid, enabled = !state.loading, keyboardType = KeyboardType.Email,
        )
        AuthField(
            value = state.password, onValueChange = { onIntent(LoginUiIntent.PasswordChanged(it)) }, label = stringResource(R.string.flow_password),
            error = state.passwordInvalid, enabled = !state.loading,
            password = true, visible = state.passwordVisible,
            onToggleVisibility = { onIntent(LoginUiIntent.TogglePassword) }, onSubmit = { onIntent(LoginUiIntent.Submit) },
        )
        TextButton(onClick = onForgotPassword, enabled = !state.loading) {
            Text(stringResource(R.string.flow_forgot_link), color = MaterialTheme.colorScheme.onSurface)
        }
        state.error?.let {
            Text(stringResource(if (it == LoginError.InvalidCredentials) R.string.flow_credentials_error else R.string.flow_unavailable), color = MaterialTheme.colorScheme.error)
        }
        AuthPrimaryButton(text = stringResource(R.string.flow_login_button), loading = state.loading) { onIntent(LoginUiIntent.Submit) }
        TextButton(onClick = { providerInfo = true }, enabled = !state.loading) {
            Text(stringResource(R.string.flow_fingerprint), color = MaterialTheme.colorScheme.onSurface)
        }
        TextButton(onClick = onRegister, enabled = !state.loading) {
            Text(stringResource(R.string.flow_register_link), color = MaterialTheme.colorScheme.onSurface)
        }
        if (state.demo) {
            Text(stringResource(R.string.flow_demo_title), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.flow_demo_description), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { onIntent(LoginUiIntent.FillDemo) }, enabled = !state.loading) {
                Text(stringResource(R.string.flow_demo_fill), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
    if (providerInfo) AuthInfoDialog(stringResource(R.string.flow_provider_info)) { providerInfo = false }
}

@Preview(name = "Login claro", showBackground = true)
@Composable
private fun LoginLightPreview() { BaseTheme(false) { LoginScreen(state = LoginUiState(), onIntent = {}, themeIndex = 1, onThemeSelected = {}) } }

@Preview(name = "Login oscuro", showBackground = true)
@Composable
private fun LoginDarkPreview() { BaseTheme(true) { LoginScreen(state = LoginUiState(), onIntent = {}, themeIndex = 2, onThemeSelected = {}) } }
