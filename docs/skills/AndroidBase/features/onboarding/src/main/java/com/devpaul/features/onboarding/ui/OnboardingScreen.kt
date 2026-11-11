package com.devpaul.features.onboarding.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.onboarding.R
import kotlinx.coroutines.launch

private val OnboardingBlue = Color(0xFF2563EB)
private val OnboardingDeepBlue = Color(0xFF1D4ED8)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()
    val background = MaterialTheme.colorScheme.background
    val darkTheme = LocalAppDarkTheme.current
    LaunchedEffect(pagerState.settledPage) {
        AppAnalytics.selection("onboarding_page", pagerState.settledPage.toString())
    }
    SetStatusBarColor(color = background, darkIcons = !darkTheme)

    Column(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(
                if (darkTheme) listOf(background, MaterialTheme.colorScheme.surface, background)
                else listOf(Color(0xFFF0F6FF), Color.White, Color(0xFFF8FAFC)),
            ))
            .statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = trackedClick("skip_onboarding", onFinished)) {
                Text(
                    stringResource(R.string.ui_skip_2e5178),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val progressDescription = stringResource(R.string.onboarding_progress, pagerState.currentPage + 1, onboardingPages.size)
            Row(
                modifier = Modifier.padding(bottom = 20.dp).semantics {
                    contentDescription = progressDescription
                    progressBarRangeInfo = ProgressBarRangeInfo(pagerState.currentPage.toFloat(), 0f..onboardingPages.lastIndex.toFloat())
                },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(onboardingPages.size) { index ->
                    val indicatorWidth by animateDpAsState(if (index == pagerState.currentPage) 28.dp else 8.dp, label = "page_indicator")
                    Box(
                        Modifier.height(8.dp).width(indicatorWidth).background(
                            if (index == pagerState.currentPage) OnboardingDeepBlue
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                            CircleShape,
                        ),
                    )
                }
            }
            val shape = RoundedCornerShape(16.dp)
            Button(
                onClick = trackedClick("next_onboarding") {
                    if (pagerState.currentPage == onboardingPages.lastIndex) {
                        onFinished()
                    } else {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                enabled = !pagerState.isScrollInProgress,
                modifier = Modifier.fillMaxWidth().height(54.dp)
                    .shadow(8.dp, shape, ambientColor = OnboardingDeepBlue.copy(alpha = 0.25f), spotColor = OnboardingDeepBlue.copy(alpha = 0.25f))
                    .clip(shape),
                shape = shape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
            ) {
                Box(
                    Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(OnboardingDeepBlue, OnboardingBlue))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(if (pagerState.currentPage == onboardingPages.lastIndex) R.string.ui_get_started_8b1222 else R.string.ui_next_c4d83a),
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val compact = maxHeight < 560.dp
        Column(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = maxHeight)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            OnboardingIllustration(page = page, compact = compact)
            Spacer(Modifier.height(if (compact) 16.dp else 24.dp))
            Text(
                stringResource(page.title),
                Modifier.widthIn(max = 360.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(page.description),
                Modifier.widthIn(max = 300.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                FeatureTile(page.firstFeatureIcon, stringResource(page.firstFeature), Modifier.weight(1f))
                FeatureTile(page.secondFeatureIcon, stringResource(page.secondFeature), Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OnboardingIllustration(page: OnboardingPage, compact: Boolean) {
    val sheetWidth = if (compact) 164.dp else 192.dp
    val sheetHeight = if (compact) 204.dp else 240.dp
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier.fillMaxWidth().height(if (compact) 232.dp else 276.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(sheetWidth, sheetHeight).offset(x = 8.dp, y = 8.dp).rotate(-6f),
            shape = shape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        ) {}
        Surface(
            modifier = Modifier.size(sheetWidth, sheetHeight).offset(x = (-4).dp, y = 4.dp).rotate(3f),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        ) {}
        Box(Modifier.size(sheetWidth, sheetHeight)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = shape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = 10.dp,
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Surface(
                            modifier = Modifier.size(if (compact) 48.dp else 56.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(page.icon, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        listOf(1f, 0.75f, 0.88f).forEachIndexed { index, fraction ->
                            Box(
                                Modifier.fillMaxWidth(fraction).height(if (index == 0) 10.dp else 8.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = if (index == 0) 0.75f else 0.4f), CircleShape),
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Column(Modifier.padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.width(64.dp).height(6.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape))
                        Box(Modifier.width(80.dp).height(6.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape))
                    }
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 12.dp, y = 12.dp),
                shape = CircleShape,
                color = OnboardingDeepBlue,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                shadowElevation = 6.dp,
            ) {
                Row(
                    Modifier.background(Brush.horizontalGradient(listOf(OnboardingDeepBlue, OnboardingBlue)))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(page.badgeIcon, null, Modifier.size(12.dp), tint = Color(0xFFFCD34D))
                    Text(stringResource(page.badge), color = Color.White, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        shadowElevation = 2.dp,
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Preview(name = "Onboarding Light", showBackground = true, widthDp = 420, heightDp = 870)
@Composable
private fun OnboardingLightPreview() {
    CvFreeTheme(darkTheme = false) { OnboardingScreen(onFinished = {}) }
}

@Preview(name = "Onboarding Dark", showBackground = true, widthDp = 420, heightDp = 870)
@Composable
private fun OnboardingDarkPreview() {
    CvFreeTheme(darkTheme = true) { OnboardingScreen(onFinished = {}) }
}

@Preview(name = "Onboarding Profile", showBackground = true, widthDp = 420, heightDp = 720)
@Composable
private fun OnboardingProfilePreview() {
    CvFreeTheme(darkTheme = false) { OnboardingPageContent(onboardingPages[1]) }
}

@Preview(name = "Onboarding Export", showBackground = true, widthDp = 420, heightDp = 720)
@Composable
private fun OnboardingExportPreview() {
    CvFreeTheme(darkTheme = false) { OnboardingPageContent(onboardingPages[2]) }
}
