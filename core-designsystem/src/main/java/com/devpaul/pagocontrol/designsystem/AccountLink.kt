package com.devpaul.pagocontrol.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountLink(
    text: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    actionSpacing: TextUnit = 0.sp,
) {
    val dark = EntryColors.isDark
    val actionColor = if (dark) EntryColors.Gold else MaterialTheme.colorScheme.onSurface
    val styledText = buildAnnotatedString {
        append(text)
        val start = text.lastIndexOf(action)
        if (start >= 0 && action.isNotEmpty()) {
            if (start > 0 && text[start - 1].isWhitespace()) {
                addStyle(
                    style = SpanStyle(letterSpacing = actionSpacing),
                    start = start - 1,
                    end = start,
                )
            }
            addStyle(
                style = SpanStyle(
                    color = actionColor,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (dark) TextDecoration.None else TextDecoration.Underline,
                ),
                start = start,
                end = start + action.length,
            )
        }
    }
    TextButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = enabled,
    ) {
        Text(
            text = styledText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
