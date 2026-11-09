package com.devpaul.pagocontrol.features.auth.ui.login

import com.devpaul.pagocontrol.features.auth.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.AppearanceMenu
import com.devpaul.pagocontrol.designsystem.PagoBrand
import com.devpaul.pagocontrol.designsystem.PagoCard
import com.devpaul.pagocontrol.designsystem.PagoControlTheme


@Composable
fun LoginScreen(state: LoginUiState, onIntent: (LoginUiIntent) -> Unit, themeIndex: Int, onThemeSelected: (Int) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.Center) {
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PagoBrand(stringResource(R.string.auth_brand_subtitle), Modifier.weight(1f))
                    AppearanceMenu(themeIndex, onThemeSelected)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.auth_title), style = MaterialTheme.typography.headlineLarge)
                    Text(stringResource(R.string.auth_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                PagoCard {
                    Text(stringResource(R.string.auth_sign_in), style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(
                        value = state.email, onValueChange = { onIntent(LoginUiIntent.EmailChanged(it)) },
                        modifier = Modifier.fillMaxWidth(), enabled = !state.loading, singleLine = true,
                        label = { Text(stringResource(R.string.auth_email)) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null) },
                        isError = state.emailInvalid,
                        supportingText = if (state.emailInvalid) ({ Text(stringResource(R.string.auth_email_error)) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    OutlinedTextField(
                        value = state.password, onValueChange = { onIntent(LoginUiIntent.PasswordChanged(it)) },
                        modifier = Modifier.fillMaxWidth(), enabled = !state.loading, singleLine = true,
                        label = { Text(stringResource(R.string.auth_password)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { onIntent(LoginUiIntent.TogglePassword) }, enabled = !state.loading) {
                                Icon(
                                    if (state.passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    stringResource(if (state.passwordVisible) R.string.auth_hide_password else R.string.auth_show_password),
                                )
                            }
                        },
                        isError = state.passwordInvalid,
                        supportingText = if (state.passwordInvalid) ({ Text(stringResource(R.string.auth_password_error)) }) else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onIntent(LoginUiIntent.Submit) }),
                    )
                    state.error?.let { error ->
                        Text(
                            stringResource(if (error == LoginError.InvalidCredentials) R.string.auth_credentials_error else R.string.auth_unavailable),
                            color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Button(
                        onClick = { onIntent(LoginUiIntent.Submit) }, enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = MaterialTheme.shapes.medium,
                    ) {
                        if (state.loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        else Text(stringResource(R.string.auth_enter))
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.auth_demo_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.auth_demo_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onIntent(LoginUiIntent.FillDemo) }, enabled = !state.loading) {
                        Text(stringResource(R.string.auth_fill_demo), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Preview(name = "Auth claro", showBackground = true)
@Composable
private fun LoginLightPreview() { PagoControlTheme(false) { LoginScreen(LoginUiState(), {}, 1, {}) } }

@Preview(name = "Auth oscuro", showBackground = true)
@Composable
private fun LoginDarkPreview() { PagoControlTheme(true) { LoginScreen(LoginUiState(), {}, 2, {}) } }
