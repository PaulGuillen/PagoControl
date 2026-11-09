package com.devpaul.pagocontrol.features.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.AppearanceMenu
import com.devpaul.pagocontrol.designsystem.BrandMark
import com.devpaul.pagocontrol.features.auth.R

@Composable
fun AuthLayout(
    onBack: (() -> Unit)? = null,
    themeIndex: Int = 0,
    onThemeSelected: (Int) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.flow_back))
                    }
                    Box(Modifier.weight(1f))
                    AppearanceMenu(themeIndex, onThemeSelected)
                }
                content()
            }
        }
    }
}

@Composable
fun AuthHeading(title: String, description: String, security: Boolean = false) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (security) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(imageVector = Icons.Outlined.LockReset, contentDescription = null, modifier = Modifier.padding(24.dp).size(48.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        } else BrandMark()
        Text(text = title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
        Text(text = description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AuthField(
    value: String, onValueChange: (String) -> Unit, label: String,
    error: Boolean = false, enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false, visible: Boolean = false,
    onToggleVisibility: () -> Unit = {}, onSubmit: (() -> Unit)? = null,
) {
    TextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = enabled,
        leadingIcon = {
            Icon(if (password) Icons.Outlined.Lock else if (keyboardType == KeyboardType.Email) Icons.Outlined.Email else Icons.Outlined.Person, null)
        },
        label = { Text(label) }, isError = error, shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
        ),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (password) KeyboardType.Password else keyboardType,
            imeAction = if (onSubmit != null) ImeAction.Done else ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit?.invoke() }),
        trailingIcon = if (password) ({
            IconButton(onClick = onToggleVisibility, enabled = enabled) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    stringResource(if (visible) R.string.flow_hide_password else R.string.flow_show_password),
                )
            }
        }) else null,
        supportingText = if (error) ({ Text(stringResource(R.string.flow_field_error)) }) else null,
    )
}

@Composable
fun AuthPrimaryButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = !loading, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        else {
            Text(text)
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

@Composable
fun AuthProviderButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.medium, border = null,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) { Text(stringResource(R.string.flow_google)) }
}

@Composable
fun AuthDivider(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(Modifier.weight(1f))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.weight(1f))
    }
}

@Composable
fun AuthInfoDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.flow_information)) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.flow_understood), color = MaterialTheme.colorScheme.onSurface) } },
    )
}
