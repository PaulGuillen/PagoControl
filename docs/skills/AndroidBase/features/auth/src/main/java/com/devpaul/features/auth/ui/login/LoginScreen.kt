package com.devpaul.features.auth.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.auth.R
import com.devpaul.features.auth.ui.components.AuthErrorMessage

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToRegister: () -> Unit,
    uiState: LoginUiState = LoginUiState(),
    onInputChanged: () -> Unit = {},
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.Spanish,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    versionName: String = "",
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val background = MaterialTheme.colorScheme.background
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }
    val submit = trackedClick("login") {
        if (!uiState.isLoading) {
            focusManager.clearFocus()
            onLogin(email.trim(), password)
        }
    }
    SetStatusBarColor(color = background, darkIcons = !LocalAppDarkTheme.current)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(background)
            .statusBarsPadding().navigationBarsPadding().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            LoginBrandHeader(isPremium = isPremium)
            Spacer(Modifier.height(28.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                shadowElevation = 8.dp,
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        stringResource(R.string.ui_welcome_back_246d60),
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        lineHeight = 29.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        stringResource(R.string.login_welcome_subtitle),
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(24.dp))
                    LoginCredentialField(
                        value = email,
                        onValueChange = {
                            email = it
                            onInputChanged()
                        },
                        label = stringResource(R.string.ui_email_59a167),
                        placeholder = "email@example.com",
                        enabled = !uiState.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                    )
                    Spacer(Modifier.height(16.dp))
                    LoginCredentialField(
                        value = password,
                        onValueChange = {
                            password = it
                            onInputChanged()
                        },
                        modifier = Modifier.focusRequester(passwordFocus),
                        label = stringResource(R.string.ui_password_5a6d1c),
                        placeholder = "••••••••",
                        isPassword = true,
                        enabled = !uiState.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        labelAction = {
                            TextButton(
                                onClick = trackedClick("forgot_password", onForgotPassword),
                                enabled = !uiState.isLoading,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp),
                            ) {
                                Text(stringResource(R.string.ui_forgot_your_password_28ca3f), fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                    )
                    uiState.errorMessage?.let { error ->
                        Spacer(Modifier.height(14.dp))
                        AuthErrorMessage(message = stringResource(error))
                    }
                    Spacer(Modifier.height(22.dp))
                    LoginSubmitButton(onClick = submit, isLoading = uiState.isLoading)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.ui_don_t_have_an_account_0cb9ce),
                            Modifier.weight(1f, fill = false),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                        )
                        TextButton(
                            onClick = trackedClick("open_register", onNavigateToRegister),
                            enabled = !uiState.isLoading,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        ) {
                            Text(stringResource(R.string.ui_create_account_11bd80), fontSize = 11.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            LoginFooter(appLanguage = appLanguage, onLanguageSelected = onLanguageSelected, enabled = !uiState.isLoading)
            if (versionName.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.login_version, versionName),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "Login Light", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun LoginLightPreview() {
    CvFreeTheme(darkTheme = false) {
        LoginScreen(onLogin = { _, _ -> }, onForgotPassword = {}, onNavigateToRegister = {}, isPremium = true)
    }
}

@Preview(name = "Login Dark", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun LoginDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        LoginScreen(onLogin = { _, _ -> }, onForgotPassword = {}, onNavigateToRegister = {})
    }
}
