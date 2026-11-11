package com.devpaul.features.resume.ui.importing

import com.devpaul.core.platform.analytics.trackedClick
import android.net.Uri
import android.widget.Toast
import java.text.DateFormat
import java.util.Date
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.importing.ResumeImportException
import com.devpaul.features.resume.importing.ResumeImportPolicy
import com.devpaul.features.resume.importing.ResumeImportStage
import com.devpaul.features.resume.importing.ResumePdfImporter
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException

@Composable
fun ResumeImportScreen(
    documentUri: Uri,
    importPolicy: ResumeImportPolicy = ResumeImportPolicy(),
    onComplete: (ResumeDocument) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val background = MaterialTheme.colorScheme.background
    var progress by remember { mutableIntStateOf(0) }
    var stage by remember { mutableStateOf(ResumeImportStage.Preparing) }
    var errorMessage by remember { mutableStateOf<Int?>(null) }
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 550),
        label = "Progreso de importación",
    )

    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    LaunchedEffect(documentUri, attempt) {
        errorMessage = null
        progress = 0
        stage = ResumeImportStage.Preparing
        AppAnalytics.outcome("import_resume", "started")
        try {
            ResumePdfImporter.import(
                context,
                documentUri,
                importPolicy,
                onAiEnhanced = {
                    Toast.makeText(context, context.getString(R.string.ai_import_enhanced), Toast.LENGTH_LONG).show()
                },
                onAiSkipped = { nextAvailableAtMillis ->
                    val message = if (nextAvailableAtMillis != null) {
                        context.getString(
                            R.string.ai_weekly_limit_reached,
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                                .format(Date(nextAvailableAtMillis)),
                        )
                    } else {
                        context.getString(R.string.ai_unavailable_local_import)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                },
            ) { nextProgress, nextStage ->
                progress = nextProgress
                stage = nextStage
            }
                .also {
                    AppAnalytics.outcome("import_resume", "success")
                    onComplete(it)
                }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            AppAnalytics.error("import_resume", error)
            errorMessage = (error as? ResumeImportException)?.messageRes ?: R.string.ui_we_couldn_t_analyze_this_pdf_bac5b0
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        IconButton(
            onClick = trackedClick("cancel_import", onBack),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.ui_cancel_import_a0f648),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        if (errorMessage == null) {
            ImportingContent(
                stage = stage,
                progress = animatedProgress,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
            )
        } else {
            ImportErrorContent(
                message = stringResource(errorMessage!!),
                onRetry = { attempt += 1 },
                onBack = onBack,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
            )
        }
    }
}

@Composable
private fun ImportingContent(
    stage: ResumeImportStage,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ResumeScanAnimation(progress)
        Spacer(Modifier.height(42.dp))
        Text(
            text = stringResource(stage.title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(stage.detail),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(26.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "${(progress * 100).roundToInt()}%",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun ResumeScanAnimation(progress: Float) {
    val transition = rememberInfiniteTransition(label = "Escáner de currículum")
    val scanOffset by transition.animateFloat(
        initialValue = -52f,
        targetValue = 52f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Línea de escaneo",
    )

    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 5.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer,
        )
        Surface(
            modifier = Modifier.size(width = 142.dp, height = 174.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
            ),
            shadowElevation = 6.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.size(62.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(3.dp)
                        .graphicsLayer { translationY = scanOffset }
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun ImportErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.ui_we_couldn_t_import_the_resume_035bea),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = trackedClick("back", onBack), modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ui_back_84a715))
            }
            Button(onClick = trackedClick("retry_import", onRetry), modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.ui_try_again_adec7b))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResumeImportPreview() {
    CvFreeTheme {
        ImportingContent(
            stage = ResumeImportStage.Extracting,
            progress = 0.62f,
        )
    }
}
