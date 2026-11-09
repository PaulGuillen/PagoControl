package com.example.androidbase.features.auth.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbase.features.auth.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(state, viewModel.isDemo, viewModel::accept)
}

@Composable
fun LoginContent(state: LoginUiState, isDemo: Boolean, onIntent: (LoginUiIntent) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.auth_title), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(if (isDemo) R.string.auth_demo_description else R.string.auth_description))
        Spacer(Modifier.height(24.dp))
        if (!isDemo) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.auth_email)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.auth_password)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = { onIntent(LoginUiIntent.SignIn(email, password)) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (isDemo) R.string.auth_continue_demo else R.string.auth_sign_in))
        }
        if (!isDemo) {
            OutlinedButton(
                onClick = { onIntent(LoginUiIntent.Register(email, password)) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.auth_register)) }
            TextButton(
                onClick = { onIntent(LoginUiIntent.ResetPassword(email)) },
                enabled = !state.isLoading,
            ) { Text(stringResource(R.string.auth_reset)) }
        }
        if (state.isLoading) CircularProgressIndicator()
        if (state.resetSent) Text(stringResource(R.string.auth_reset_sent))
        state.failure?.let { failure ->
            val message = when (failure) {
                LoginError.InvalidInput -> R.string.auth_invalid_input
                LoginError.InvalidCredentials -> R.string.auth_invalid_credentials
                LoginError.Network -> R.string.auth_network_error
                LoginError.Unavailable -> R.string.auth_generic_error
            }
            Text(stringResource(message), color = MaterialTheme.colorScheme.error)
        }
    }
}
