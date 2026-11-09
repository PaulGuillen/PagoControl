package com.example.androidbase.features.auth.ui.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidbase.designsystem.BaseTheme
import com.example.androidbase.features.auth.R
import com.example.androidbase.features.auth.ui.components.*

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState, onIntent: (ForgotPasswordUiIntent) -> Unit, onLogin: () -> Unit,
    themeIndex: Int = 0, onThemeSelected: (Int) -> Unit = {},
) {
    AuthLayout(onBack = onLogin, themeIndex = themeIndex, onThemeSelected = onThemeSelected) {
        AuthHeading(title = stringResource(R.string.flow_forgot_title), description = stringResource(R.string.flow_forgot_description), security = true)
        if (state.completed) {
            Text(stringResource(if (state.demoOnly) R.string.flow_forgot_demo else R.string.flow_forgot_success))
        } else {
            AuthField(
                value = state.email, onValueChange = { onIntent(ForgotPasswordUiIntent.EmailChanged(it)) }, label = stringResource(R.string.flow_email),
                error = state.error == ForgotPasswordError.InvalidEmail, enabled = !state.loading,
                keyboardType = KeyboardType.Email, onSubmit = { onIntent(ForgotPasswordUiIntent.Submit) },
            )
            state.error?.let { error ->
                Text(stringResource(when (error) {
                    ForgotPasswordError.InvalidEmail -> R.string.flow_email_invalid
                    ForgotPasswordError.Network -> R.string.flow_network_error
                    ForgotPasswordError.Unavailable -> R.string.flow_unavailable
                }), color = MaterialTheme.colorScheme.error)
            }
            AuthPrimaryButton(text = stringResource(R.string.flow_send_instructions), loading = state.loading) { onIntent(ForgotPasswordUiIntent.Submit) }
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(imageVector = Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.flow_account_security), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.flow_security_description), style = MaterialTheme.typography.bodySmall)
            }
        }
        TextButton(onClick = onLogin, enabled = !state.loading) { Text(stringResource(R.string.flow_back_login), color = MaterialTheme.colorScheme.onSurface) }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotLightPreview() { BaseTheme(false) { ForgotPasswordScreen(state = ForgotPasswordUiState(), onIntent = {}, onLogin = {}) } }

@Preview(showBackground = true)
@Composable
private fun ForgotDarkPreview() { BaseTheme(true) { ForgotPasswordScreen(state = ForgotPasswordUiState(), onIntent = {}, onLogin = {}) } }
