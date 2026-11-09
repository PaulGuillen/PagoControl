package com.devpaul.pagocontrol.features.splash.ui.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.designsystem.EntryColors
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.designsystem.SplashSurface
import com.devpaul.pagocontrol.features.splash.R

@Composable
fun SplashScreen(state: SplashUiState, onIntent: (SplashUiIntent) -> Unit) {
    SplashSurface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight).padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    shape = CircleShape,
                    color = EntryColors.card,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, EntryColors.border),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(8.dp).background(if (EntryColors.isDark) EntryColors.Mint else EntryColors.Green, CircleShape))
                        Text(stringResource(R.string.splash_badge), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(
                    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    SplashBrandMark()
                    Text(
                        text = buildAnnotatedString {
                            append(stringResource(R.string.splash_brand))
                            withStyle(SpanStyle(color = if (EntryColors.isDark) EntryColors.Gold else MaterialTheme.colorScheme.onSurface)) {
                                append(stringResource(R.string.splash_brand_accent))
                            }
                        },
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.splash_description),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(R.string.splash_yape, R.string.splash_plin, R.string.splash_bcp, R.string.splash_interbank).forEach { resource ->
                            Surface(
                                shape = CircleShape,
                                color = EntryColors.card,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(1.dp, EntryColors.border),
                            ) {
                                Text(
                                    text = stringResource(resource),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    if (state.failed) {
                        Text(stringResource(R.string.splash_error), color = MaterialTheme.colorScheme.error)
                        Button(onClick = { onIntent(SplashUiIntent.Retry) }) { Text(stringResource(R.string.splash_retry)) }
                    } else CircularProgressIndicator(
                        color = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                        trackColor = EntryColors.Gold.copy(alpha = 0.2f),
                    )
                    Surface(
                        shape = CircleShape,
                        color = EntryColors.card,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(1.dp, EntryColors.border),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = if (EntryColors.isDark) EntryColors.Mint else EntryColors.Green,
                            )
                            Text(stringResource(R.string.splash_privacy), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashBrandMark() {
    Box(Modifier.size(160.dp).padding(bottom = 8.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(36.dp),
            color = EntryColors.card,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, EntryColors.Gold.copy(alpha = 0.35f)),
            shadowElevation = 16.dp,
        ) {
            Box(
                modifier = Modifier.background(
                    Brush.linearGradient(listOf(EntryColors.Gold.copy(alpha = 0.08f), Color.Transparent, EntryColors.Mint.copy(alpha = 0.12f))),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_splash_wallet),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    tint = Color.Unspecified,
                )
            }
        }
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            shape = CircleShape,
            color = EntryColors.Green,
            contentColor = Color.White,
            border = BorderStroke(1.dp, EntryColors.Mint),
        ) {
            Text(text = stringResource(R.string.splash_country), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(name = "Splash claro", showBackground = true)
@Composable
private fun SplashLightPreview() { PagoControlTheme(false) { SplashScreen(SplashUiState(), {}) } }

@Preview(name = "Splash oscuro", showBackground = true)
@Composable
private fun SplashDarkPreview() { PagoControlTheme(true) { SplashScreen(SplashUiState(), {}) } }
