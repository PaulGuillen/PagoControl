package com.devpaul.pagocontrol.features.auth.ui.forgotpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.AccountLink
import com.devpaul.pagocontrol.designsystem.EntryColors
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.auth.R
import com.devpaul.pagocontrol.features.auth.ui.components.AuthPrimaryButton
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.components.RecoveryEmblem
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.components.RecoveryNotice
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.components.RecoveryProtection
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.components.RecoverySecurityBar

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onIntent: (ForgotPasswordUiIntent) -> Unit,
    onLogin: () -> Unit,
) {
    Scaffold(
        containerColor = EntryColors.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                RecoverySecurityBar(onLogin)
                RecoveryEmblem()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.flow_forgot_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.flow_forgot_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.completed) {
                    Text(
                        text = stringResource(R.string.flow_forgot_success),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    RecoveryForm(state, onIntent)
                }
                RecoveryProtection()
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.flow_recovery_remembered),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    AccountLink(
                        text = stringResource(R.string.flow_back_login),
                        action = stringResource(R.string.flow_back_login),
                        onClick = onLogin,
                        enabled = !state.loading,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecoveryForm(state: ForgotPasswordUiState, onIntent: (ForgotPasswordUiIntent) -> Unit) {
    val dark = EntryColors.isDark
    val emailLabel = stringResource(R.string.flow_email)
    val fieldColor = if (dark) Color(0xFF171C25) else Color(0xFFEDEFE8)
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.flow_recovery_email_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(ForgotPasswordUiIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { contentDescription = emailLabel },
                enabled = !state.loading,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                isError = state.error == ForgotPasswordError.InvalidEmail,
                placeholder = { Text(stringResource(R.string.flow_recovery_email_hint)) },
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldColor,
                    unfocusedContainerColor = fieldColor,
                    disabledContainerColor = fieldColor,
                    focusedBorderColor = if (dark) EntryColors.Gold else Color(0xFF765B00),
                    unfocusedBorderColor = if (dark) EntryColors.border else Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onIntent(ForgotPasswordUiIntent.Submit) }),
            )
            state.error?.let { error ->
                Text(
                    text = stringResource(when (error) {
                        ForgotPasswordError.InvalidEmail -> R.string.flow_email_invalid
                        ForgotPasswordError.Network -> R.string.flow_network_error
                        ForgotPasswordError.Unavailable -> R.string.flow_unavailable
                        ForgotPasswordError.TooManyRequests -> R.string.flow_too_many_requests
                    }),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        RecoveryNotice()
        AuthPrimaryButton(
            text = stringResource(R.string.flow_send_instructions),
            loading = state.loading,
            onClick = { onIntent(ForgotPasswordUiIntent.Submit) },
        )
    }
}

@Preview(name = "Recuperar contraseña claro", showBackground = true)
@Composable
private fun ForgotLightPreview() {
    PagoControlTheme(false) { ForgotPasswordScreen(state = ForgotPasswordUiState(), onIntent = {}, onLogin = {}) }
}

@Preview(name = "Recuperar contraseña oscuro", showBackground = true)
@Composable
private fun ForgotDarkPreview() {
    PagoControlTheme(true) { ForgotPasswordScreen(state = ForgotPasswordUiState(), onIntent = {}, onLogin = {}) }
}
