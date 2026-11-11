package com.devpaul.features.auth.ui.forgot

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
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
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onSend: (String) -> Unit,
    onInputChanged: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.Spanish,
    onLanguageSelected: (AppLanguage) -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    val background = MaterialTheme.colorScheme.background
    val darkTheme = LocalAppDarkTheme.current
    val focusManager = LocalFocusManager.current
    val submit = trackedClick("send_password_reset") {
        if (!uiState.isLoading) {
            focusManager.clearFocus()
            onSend(email.trim())
        }
    }
    SetStatusBarColor(background, darkIcons = !darkTheme)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(background)
            .drawBehind {
                val center = Offset(size.width / 2f, size.height * 0.08f)
                val radius = size.width * 0.9f
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0xFF60A5FA).copy(alpha = if (darkTheme) 0.08f else 0.16f), Color.Transparent),
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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthSecurityHeader(onBack = onNavigateBack)
                Spacer(Modifier.height(8.dp))
                LoginBrandHeader(isPremium = isPremium, compact = true, subtitle = stringResource(R.string.register_brand_subtitle))
                Spacer(Modifier.height(20.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    shadowElevation = 8.dp,
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            stringResource(R.string.ui_reset_your_password_560101),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.6).sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.ui_enter_the_email_associated_with_your_account_and_we_ll_send_b6b33f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                        )
                        Spacer(Modifier.height(22.dp))
                        LoginCredentialField(
                            value = email,
                            onValueChange = {
                                email = it
                                onInputChanged()
                            },
                            label = stringResource(R.string.ui_email_59a167),
                            placeholder = "email@example.com",
                            enabled = !uiState.isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                        )
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        ) {
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.Info, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.recovery_inbox_hint), fontSize = 10.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        uiState.errorMessage?.let { error ->
                            Spacer(Modifier.height(12.dp))
                            AuthErrorMessage(message = stringResource(error))
                        }
                        Spacer(Modifier.height(16.dp))
                        LoginSubmitButton(onClick = submit, isLoading = uiState.isLoading, text = stringResource(R.string.recovery_send_link))
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.recovery_remembered),
                                Modifier.weight(1f, fill = false),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                            )
                            TextButton(
                                onClick = trackedClick("back_to_login", onNavigateBack),
                                enabled = !uiState.isLoading,
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            ) {
                                Text(stringResource(R.string.ui_sign_in_9c18b1), fontSize = 10.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Column(Modifier.padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                TextButton(
                    onClick = trackedClick("open_recovery_help") { showHelp = true },
                    contentPadding = PaddingValues(horizontal = 0.dp),
                ) {
                    Text(stringResource(R.string.recovery_help_link), fontSize = 10.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LoginFooter(appLanguage = appLanguage, onLanguageSelected = onLanguageSelected, enabled = !uiState.isLoading)
            }
        }
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.recovery_help_title)) },
            text = { Text(stringResource(R.string.recovery_help_body)) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text(stringResource(R.string.recovery_help_close)) }
            },
        )
    }
}

@Preview(name = "Recovery Light", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun ForgotPasswordPreview() {
    CvFreeTheme(darkTheme = false) {
        ForgotPasswordScreen(ForgotPasswordUiState(), {}, {}, {}, isPremium = true)
    }
}

@Preview(name = "Recovery Dark", showBackground = true, widthDp = 390, heightDp = 884)
@Composable
private fun ForgotPasswordDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        ForgotPasswordScreen(ForgotPasswordUiState(), {}, {}, {})
    }
}
