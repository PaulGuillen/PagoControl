package com.devpaul.features.splash.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.features.splash.R
import kotlinx.coroutines.delay

private const val SplashDurationMillis = 1_600L
private val SplashBackground = Color(0xFFF8FAFC)
private val SplashBlue = Color(0xFF2563EB)

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    isPremium: Boolean = false,
    versionName: String = "",
) {
    val darkTheme = LocalAppDarkTheme.current
    val backgroundColors = if (darkTheme) {
        listOf(Color(0xFF070E1E), Color(0xFF091B38), Color(0xFF040914))
    } else {
        listOf(SplashBackground, Color(0xFFF1F5F9), Color(0xFFE0E7FF))
    }
    val currentOnFinished by rememberUpdatedState(onFinished)
    SetStatusBarColor(color = backgroundColors.first(), darkIcons = !darkTheme)
    LaunchedEffect(Unit) {
        delay(SplashDurationMillis)
        currentOnFinished()
    }

    val transition = rememberInfiniteTransition(label = "splash")
    val floatOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(3_000), RepeatMode.Reverse),
        label = "logo_float",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_250), RepeatMode.Reverse),
        label = "badge_pulse",
    )
    val progress by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_800, easing = LinearEasing)),
        label = "loading_progress",
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
            .background(Brush.verticalGradient(backgroundColors))
            .drawBehind {
                val radius = size.width * 0.8f
                listOf(
                    Offset(size.width * 0.05f, size.height * 0.05f) to
                        if (darkTheme) SplashBlue.copy(alpha = 0.2f) else Color(0xFFBFDBFE).copy(alpha = 0.4f),
                    Offset(size.width, size.height * 0.46f) to
                        if (darkTheme) Color(0xFF6366F1).copy(alpha = 0.15f) else Color(0xFFC7D2FE).copy(alpha = 0.3f),
                    Offset(0f, size.height * 0.8f) to
                        if (darkTheme) Color(0xFF3B82F6).copy(alpha = 0.1f) else Color(0xFFDBEAFE).copy(alpha = 0.5f),
                ).forEach { (center, color) ->
                    drawCircle(Brush.radialGradient(listOf(color, Color.Transparent), center, radius), radius, center)
                }
            },
    ) {
        val shortScreen = maxHeight < 640.dp
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(if (shortScreen) 88.dp else 112.dp)
                            .graphicsLayer { translationY = floatOffset.dp.toPx() }
                            .drawBehind {
                                val radius = size.width * 0.95f
                                drawCircle(
                                    Brush.radialGradient(
                                        listOf(if (darkTheme) SplashBlue.copy(alpha = 0.4f) else Color(0xFF60A5FA).copy(alpha = 0.25f), Color.Transparent),
                                        center,
                                        radius,
                                    ),
                                    radius,
                                )
                            },
                        shape = RoundedCornerShape(28.dp),
                        color = Color.Transparent,
                        shadowElevation = 16.dp,
                    ) {
                        Image(
                            painter = painterResource(com.devpaul.core.designsystem.R.drawable.app_logo),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.height(if (shortScreen) 24.dp else 32.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "CV Free",
                            color = if (darkTheme) Color.White else Color(0xFF0F172A),
                            fontSize = 36.sp,
                            lineHeight = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1.2).sp,
                        )
                        if (isPremium) {
                            Text(
                                "PRO",
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(Brush.horizontalGradient(listOf(SplashBlue, Color(0xFF4F46E5))))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.splash_tagline),
                        modifier = Modifier.width(270.dp),
                        color = if (darkTheme) Color(0xFFCBD5E1) else Color(0xFF475569),
                        fontSize = 14.sp,
                        lineHeight = 23.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(if (shortScreen) 16.dp else 24.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (darkTheme) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, if (darkTheme) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFFE2E8F0).copy(alpha = 0.9f)),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Box(Modifier.size(6.dp).graphicsLayer { alpha = pulse }.background(if (darkTheme) Color(0xFF34D399) else Color(0xFF10B981), CircleShape))
                            Text(
                                stringResource(R.string.splash_badge),
                                color = if (darkTheme) Color(0xFFCBD5E1) else Color(0xFF334155),
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(Modifier.height(if (shortScreen) 24.dp else 36.dp))
                    val loadingDescription = stringResource(R.string.splash_loading)
                    Canvas(
                        Modifier.width(144.dp).height(6.dp).clip(CircleShape)
                            .background((if (darkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)).copy(alpha = 0.8f))
                            .semantics {
                                contentDescription = loadingDescription
                                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                            },
                    ) {
                        val center = Offset(size.width * (progress + 0.3f), size.height / 2f)
                        drawRect(Brush.radialGradient(listOf(if (darkTheme) Color(0xFF3B82F6) else SplashBlue, Color.Transparent), center, size.width * 0.3f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "POWERED BY CV FREE",
                color = if (darkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.2.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (versionName.isBlank()) stringResource(R.string.splash_suite)
                else stringResource(R.string.splash_version, versionName),
                color = if (darkTheme) Color(0xFF64748B) else Color(0xFF94A3B8),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Preview(name = "Splash", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SplashScreenPreview() {
    CvFreeTheme(darkTheme = false) { SplashScreen(onFinished = {}, isPremium = true, versionName = "1.0.7") }
}

@Preview(name = "Splash Dark", showBackground = true, widthDp = 412, heightDp = 892)
@Composable
private fun SplashScreenDarkPreview() {
    CvFreeTheme(darkTheme = true) { SplashScreen(onFinished = {}, isPremium = true, versionName = "1.0.7") }
}

@Preview(name = "Splash compact", showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun SplashScreenCompactPreview() {
    CvFreeTheme { SplashScreen(onFinished = {}) }
}
