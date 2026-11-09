package com.example.androidbase.features.onboarding.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidbase.designsystem.EntryColors
import com.example.androidbase.designsystem.BaseTheme
import com.example.androidbase.features.onboarding.R
import com.example.androidbase.designsystem.AccountLink
import com.example.androidbase.features.onboarding.ui.onboarding.components.PrivacyIllustration
import com.example.androidbase.features.onboarding.ui.onboarding.components.ControlIllustration

@Composable
fun OnboardingScreen(state: OnboardingUiState, onIntent: (OnboardingUiIntent) -> Unit) {
    BackHandler(enabled = state.page > 0 && !state.saving) { onIntent(OnboardingUiIntent.Back) }
    val titles = listOf(R.string.onboarding_title_1, R.string.onboarding_title_2, R.string.onboarding_title_3)
    val descriptions = listOf(R.string.onboarding_description_1, R.string.onboarding_description_2, R.string.onboarding_description_3)
    Scaffold(
        containerColor = EntryColors.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val progress = stringResource(R.string.onboarding_progress, state.page + 1)
                    Row(
                        modifier = Modifier.weight(1f).semantics { stateDescription = progress },
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(3) { index ->
                            Box(Modifier.width(if (index == state.page) 36.dp else 12.dp).height(6.dp)
                                .background(if (index == state.page) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape))
                        }
                    }
                    TextButton(
                        onClick = { onIntent(OnboardingUiIntent.Finish(OnboardingDestination.Login)) },
                        enabled = !state.saving,
                    ) { Text(stringResource(R.string.onboarding_skip), color = MaterialTheme.colorScheme.onSurface) }
                }
                when (state.page) {
                    0 -> DetectionIllustration()
                    1 -> PrivacyIllustration()
                    else -> ControlIllustration()
                }
                Text(text = stringResource(titles[state.page]), style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Text(text = stringResource(descriptions[state.page]), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                if (state.failed) Text(stringResource(R.string.onboarding_error), color = MaterialTheme.colorScheme.error)
                Button(
                    onClick = { onIntent(OnboardingUiIntent.Next) },
                    modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !state.saving,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (state.saving) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else {
                        Text(stringResource(if (state.page == 2) R.string.onboarding_start else R.string.onboarding_next))
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 12.dp))
                    }
                }
                AccountLink(
                    text = stringResource(R.string.onboarding_login),
                    action = stringResource(R.string.onboarding_login_action),
                    onClick = { onIntent(OnboardingUiIntent.Finish(OnboardingDestination.Login)) },
                    enabled = !state.saving,
                )
            }
        }
    }
}

@Composable
private fun IllustrationCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = EntryColors.card,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, EntryColors.border),
        shadowElevation = if (EntryColors.isDark) 0.dp else 3.dp,
    ) {
        Box(Modifier.padding(14.dp)) { content() }
    }
}

@Composable
private fun DetectionIllustration() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ProviderPreview(
            provider = stringResource(R.string.onboarding_provider_yape),
            title = stringResource(R.string.onboarding_yape_name),
            detail = stringResource(R.string.onboarding_yape_detail),
            amount = "+ S/ 45.00",
            color = EntryColors.Purple,
            icon = Icons.Outlined.Bolt,
        )
        Surface(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            shape = CircleShape,
            color = EntryColors.card,
            border = BorderStroke(1.dp, EntryColors.border),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (EntryColors.isDark) EntryColors.Gold else Color(0xFF765B00),
                )
                Text(stringResource(R.string.onboarding_sync_preview), style = MaterialTheme.typography.labelMedium)
            }
        }
        ProviderPreview(
            provider = stringResource(R.string.onboarding_provider_plin),
            title = stringResource(R.string.onboarding_transfer),
            detail = stringResource(R.string.onboarding_plin_detail),
            amount = "+ S/ 120.00",
            color = EntryColors.Cyan,
            icon = Icons.Outlined.CurrencyExchange,
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (EntryColors.isDark) Color(0xFF171C25) else Color(0xFFE8EBE4),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, EntryColors.border),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(EntryColors.Gold, CircleShape))
                    Text(stringResource(R.string.onboarding_daily), style = MaterialTheme.typography.labelMedium)
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("S/ 165.00", style = MaterialTheme.typography.displaySmall)
                    Surface(
                        shape = CircleShape,
                        color = EntryColors.Green,
                        contentColor = Color.White,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White,
                            )
                            Text(stringResource(R.string.onboarding_example_count), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.onboarding_preview),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ProviderPreview(
    provider: String,
    title: String,
    detail: String,
    amount: String,
    color: Color,
    icon: ImageVector,
) {
    IllustrationCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = MaterialTheme.shapes.medium, color = color, contentColor = Color.White) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp).size(28.dp),
                    tint = Color.White,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(shape = CircleShape, color = color, contentColor = Color.White) {
                        Text(text = provider, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (EntryColors.isDark) EntryColors.Mint else EntryColors.Green,
                    )
                }
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Onboarding claro", showBackground = true)
@Composable
private fun OnboardingLightPreview() { BaseTheme(false) { OnboardingScreen(OnboardingUiState(), {}) } }

@Preview(name = "Onboarding oscuro", showBackground = true)
@Composable
private fun OnboardingDarkPreview() { BaseTheme(true) { OnboardingScreen(OnboardingUiState(), {}) } }

@Preview(name = "Privacidad claro", showBackground = true)
@Composable
private fun PrivacidadLightPreview() {
    BaseTheme(false) { OnboardingScreen(OnboardingUiState(page = 1), {}) }
}

@Preview(name = "Privacidad oscuro", showBackground = true)
@Composable
private fun PrivacidadDarkPreview() {
    BaseTheme(true) { OnboardingScreen(OnboardingUiState(page = 1), {}) }
}

@Preview(name = "Control claro", showBackground = true)
@Composable
private fun ControlLightPreview() {
    BaseTheme(false) { OnboardingScreen(OnboardingUiState(page = 2), {}) }
}

@Preview(name = "Control oscuro", showBackground = true)
@Composable
private fun ControlDarkPreview() {
    BaseTheme(true) { OnboardingScreen(OnboardingUiState(page = 2), {}) }
}
