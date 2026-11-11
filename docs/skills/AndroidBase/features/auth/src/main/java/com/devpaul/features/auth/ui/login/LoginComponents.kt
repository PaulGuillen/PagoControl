package com.devpaul.features.auth.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.locale.AppLanguage
import com.devpaul.features.auth.R

private val LoginBlue = Color(0xFF2563EB)
private val LoginDeepBlue = Color(0xFF1D4ED8)

@Composable
internal fun LoginBrandHeader(
    isPremium: Boolean,
    compact: Boolean = false,
    subtitle: String = stringResource(R.string.ui_build_your_professional_future_today_6af353),
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(if (compact) 66.dp else 74.dp),
            shape = RoundedCornerShape(if (compact) 18.dp else 24.dp),
            color = Color.Transparent,
            shadowElevation = 10.dp,
        ) {
            Image(
                painter = painterResource(com.devpaul.core.designsystem.R.drawable.app_logo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("CV Free", color = MaterialTheme.colorScheme.onBackground, fontSize = if (compact) 24.sp else 30.sp, lineHeight = if (compact) 32.sp else 38.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp)
            if (isPremium) {
                Text(
                    "PRO",
                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = if (compact) 12.sp else 13.sp, lineHeight = if (compact) 18.sp else 20.sp, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun LoginCredentialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    trailingIcon: ImageVector = Icons.Outlined.AlternateEmail,
    labelAction: @Composable () -> Unit = {},
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            labelAction()
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().semantics { contentDescription = label },
            enabled = enabled,
            textStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
            placeholder = { Text(placeholder, fontSize = 14.sp, lineHeight = 20.sp) },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(
                        onClick = trackedClick("toggle_password_visibility") { passwordVisible = !passwordVisible },
                        enabled = enabled,
                    ) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            stringResource(if (passwordVisible) R.string.ui_hide_password_a0e898 else R.string.ui_show_password_3d04be),
                            Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else Icon(trailingIcon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            ),
        )
    }
}

@Composable
internal fun LoginSubmitButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    text: String = stringResource(R.string.ui_sign_in_9c18b1),
) {
    val shape = RoundedCornerShape(16.dp)
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp)
            .shadow(10.dp, shape, ambientColor = LoginDeepBlue.copy(alpha = 0.3f), spotColor = LoginDeepBlue.copy(alpha = 0.3f))
            .clip(shape),
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent, contentColor = Color.White),
    ) {
        Row(
            Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(LoginBlue, LoginDeepBlue))),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(16.dp), tint = Color.White)
            }
        }
    }
}

@Composable
internal fun LoginFooter(appLanguage: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit, enabled: Boolean) {
    var showLanguages by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Outlined.Lock, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.ui_secure_and_encrypted_ffc3eb), fontSize = 10.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(Modifier.size(3.dp).background(MaterialTheme.colorScheme.outline, CircleShape))
        Box {
            TextButton(onClick = { showLanguages = true }, enabled = enabled, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Outlined.Language, null, Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text(stringResource(if (appLanguage == AppLanguage.Spanish) R.string.ui_spanish_2001ca else R.string.login_english), fontSize = 10.sp, lineHeight = 14.sp)
                Spacer(Modifier.width(3.dp))
                Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(12.dp))
            }
            DropdownMenu(expanded = showLanguages, onDismissRequest = { showLanguages = false }) {
                AppLanguage.entries.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(stringResource(if (language == AppLanguage.Spanish) R.string.ui_spanish_2001ca else R.string.login_english)) },
                        onClick = trackedClick("select_login_language") {
                            showLanguages = false
                            onLanguageSelected(language)
                        },
                    )
                }
            }
        }
    }
}
