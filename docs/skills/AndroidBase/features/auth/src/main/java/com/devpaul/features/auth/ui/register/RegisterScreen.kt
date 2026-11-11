package com.devpaul.features.auth.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.devpaul.features.auth.ui.components.AuthSecurityHeader
import com.devpaul.features.auth.ui.login.LoginBrandHeader
import com.devpaul.features.auth.ui.login.LoginCredentialField
import com.devpaul.features.auth.ui.login.LoginFooter
import com.devpaul.features.auth.ui.login.LoginSubmitButton

@Composable
fun RegisterScreen(
    onRegister: (name: String, email: String, password: String) -> Unit,
    onNavigateToLogin: () -> Unit,
    uiState: RegisterUiState = RegisterUiState(),
    onInputChanged: () -> Unit = {},
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.Spanish,
    onLanguageSelected: (AppLanguage) -> Unit = {},
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val darkTheme = LocalAppDarkTheme.current
    val background = MaterialTheme.colorScheme.background
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val submit = trackedClick("register") {
        if (!uiState.isLoading) {
            focusManager.clearFocus()
            onRegister(name.trim(), email.trim(), password)
        }
    }
    SetStatusBarColor(color = background, darkIcons = !darkTheme)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                if (darkTheme) listOf(background, MaterialTheme.colorScheme.surface, background)
                else listOf(Color(0xFFF3F7FD), Color(0xFFF8FAFD), Color(0xFFF0F4FA)),
            ))
            .drawBehind {
                val center = Offset(size.width / 2f, 0f)
                val radius = size.width * 0.7f
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF60A5FA).copy(alpha = if (darkTheme) 0.08f else 0.14f), Color.Transparent),
                        center,
                        radius,
                    ),
                    radius,
                    center,
                )
            }
            .statusBarsPadding().navigationBarsPadding().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthSecurityHeader(onBack = onNavigateToLogin)
                Spacer(Modifier.height(8.dp))
                LoginBrandHeader(isPremium = isPremium, compact = true, subtitle = stringResource(R.string.register_brand_subtitle))
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.ui_create_account_11bd80),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.ui_get_started_for_free_and_create_your_first_professional_resu_873328),
                    modifier = Modifier.widthIn(max = 280.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)),
                    shadowElevation = 8.dp,
                ) {
                    Column(Modifier.padding(20.dp)) {
                        LoginCredentialField(
                            value = name,
                            onValueChange = {
                                name = it
                                onInputChanged()
                            },
                            label = stringResource(R.string.ui_full_name_cccece),
                            placeholder = stringResource(R.string.register_name_placeholder),
                            enabled = !uiState.isLoading,
                            trailingIcon = Icons.Outlined.PersonOutline,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                        )
                        Spacer(Modifier.height(12.dp))
                        LoginCredentialField(
                            value = email,
                            onValueChange = {
                                email = it
                                onInputChanged()
                            },
                            modifier = Modifier.focusRequester(emailFocus),
                            label = stringResource(R.string.ui_email_59a167),
                            placeholder = "email@example.com",
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                        )
                        Spacer(Modifier.height(12.dp))
                        LoginCredentialField(
                            value = password,
                            onValueChange = {
                                password = it
                                onInputChanged()
                            },
                            modifier = Modifier.focusRequester(passwordFocus),
                            label = stringResource(R.string.ui_password_5a6d1c),
                            placeholder = stringResource(R.string.ui_at_least_8_characters_53cf13),
                            enabled = !uiState.isLoading,
                            isPassword = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.register_password_hint),
                            Modifier.padding(horizontal = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        uiState.errorMessage?.let { error ->
                            Spacer(Modifier.height(12.dp))
                            AuthErrorMessage(message = stringResource(error))
                        }
                        Spacer(Modifier.height(18.dp))
                        LoginSubmitButton(onClick = submit, isLoading = uiState.isLoading, text = stringResource(R.string.ui_sign_up_be32fb))
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                stringResource(R.string.ui_already_have_an_account_65b418),
                                Modifier.weight(1f, fill = false),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                            )
                            TextButton(
                                onClick = trackedClick("open_login", onNavigateToLogin),
                                enabled = !uiState.isLoading,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            ) {
                                Text(stringResource(R.string.ui_sign_in_9c18b1), fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                LoginFooter(appLanguage = appLanguage, onLanguageSelected = onLanguageSelected, enabled = !uiState.isLoading)
            }
        }
    }
}

@Preview(name = "Register Light", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun RegisterLightPreview() {
    CvFreeTheme(darkTheme = false) {
        RegisterScreen(onRegister = { _, _, _ -> }, onNavigateToLogin = {}, isPremium = true)
    }
}

@Preview(name = "Register Dark", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun RegisterDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        RegisterScreen(onRegister = { _, _, _ -> }, onNavigateToLogin = {})
    }
}
