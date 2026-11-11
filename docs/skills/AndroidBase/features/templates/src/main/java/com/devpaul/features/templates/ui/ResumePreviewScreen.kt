package com.devpaul.features.templates.ui

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import timber.log.Timber
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.pdf.ResumePdfExporter
import kotlin.math.ceil
import kotlinx.coroutines.launch

@Composable
fun ResumePreviewScreen(
    onBack: () -> Unit,
    onSave: suspend () -> Unit,
    resume: ResumeDocument,
    modifier: Modifier = Modifier,
    onExported: suspend () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val previewGraphicsLayer = rememberGraphicsLayer()
    var isExporting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var pendingPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val pdfSavedMessage = stringResource(R.string.ui_pdf_saved_successfully_e80449)
    val pdfGenerationError = stringResource(R.string.ui_the_pdf_couldn_t_be_generated_183c0d)
    val previewCaptureError = stringResource(R.string.ui_the_preview_couldn_t_be_captured_9102da)
    val resumeSaveError = stringResource(R.string.ui_the_resume_couldn_t_be_saved_8205c7)
    val createPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"),
    ) { destination ->
        if (destination == null) {
            pendingPreview = null
            isExporting = false
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val exportResult = runCatching {
                val bitmap = checkNotNull(pendingPreview) { previewCaptureError }
                ResumePdfExporter.export(context, destination, bitmap, resume)
            }
            pendingPreview = null
            isExporting = false
            if (exportResult.isSuccess) {
                AppAnalytics.outcome("export_pdf", "success")
                runCatching { onExported() }
                    .onFailure { error -> Timber.tag("ResumePreview").e(error, "%s", resumeSaveError) }
                snackbarHostState.showSnackbar(pdfSavedMessage)
            } else {
                val error = exportResult.exceptionOrNull()
                AppAnalytics.error("export_pdf", error)
                Timber.tag("ResumePreview").e(error, "%s", pdfGenerationError)
                snackbarHostState.showSnackbar(pdfGenerationError)
            }
        }
    }
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PreviewTopBar(onBack = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(R.string.ui_your_resume_is_ready_0d5cd6),
                    modifier = Modifier.padding(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                )
                ScaledResumeDocument(
                    resume = resume,
                    captureLayer = previewGraphicsLayer,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
            }
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = trackedClick("download_pdf") {
                        isExporting = true
                        scope.launch {
                            runCatching {
                                val captured = previewGraphicsLayer.toImageBitmap().asAndroidBitmap()
                                captured.copy(android.graphics.Bitmap.Config.ARGB_8888, false)
                                    ?: error("No se pudo convertir la vista previa")
                            }.onSuccess { bitmap ->
                                pendingPreview = bitmap
                                createPdf.launch(ResumePdfExporter.suggestedFileName(resume))
                            }.onFailure { error ->
                                isExporting = false
                                AppAnalytics.error("capture_resume_preview", error)
                                Timber.tag("ResumePreview").e(error, "%s", previewCaptureError)
                                snackbarHostState.showSnackbar(previewCaptureError)
                            }
                        }
                    },
                    enabled = !isExporting && !isSaving,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.Download, contentDescription = null)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (isExporting) stringResource(R.string.ui_generating_55c32a) else stringResource(R.string.ui_download_pdf_40863e))
                }
                Button(
                    onClick = trackedClick("save_resume") {
                        isSaving = true
                        scope.launch {
                            runCatching { onSave() }
                                .onFailure { error ->
                                    Timber.tag("ResumePreview").e(error, "%s", resumeSaveError)
                                    snackbarHostState.showSnackbar(resumeSaveError)
                                }
                            isSaving = false
                        }
                    },
                    enabled = !isExporting && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (isSaving) stringResource(R.string.ui_saving_92902e) else stringResource(R.string.ui_save_resume_8c58fc), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 76.dp),
        )
    }
}

@Composable
fun TemplateExamplePreviewScreen(
    templateId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val template = ResumeTemplateCatalog.find(templateId)
    val exampleResume = rememberPreviewDocument(template.id)
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        PreviewTopBar(
            onBack = onBack,
            title = template.localizedName(),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.ui_preview_with_sample_information_ab7559),
                modifier = Modifier.padding(start = 20.dp, top = 6.dp, end = 20.dp, bottom = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            ScaledResumeDocument(
                resume = exampleResume,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScaledResumeDocument(
    resume: ResumeDocument,
    captureLayer: GraphicsLayer? = null,
    modifier: Modifier = Modifier,
) {
    val designWidth = with(LocalDensity.current) { RESUME_DESIGN_WIDTH_DP.dp.roundToPx() }
    Layout(
        modifier = modifier,
        content = {
            val documentModifier = if (captureLayer == null) {
                Modifier.fillMaxWidth()
            } else {
                Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        captureLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(captureLayer)
                    }
            }
            ResumeTemplateDocument(
                resume = resume,
                modifier = documentModifier,
            )
        },
    ) { measurables, constraints ->
        val availableWidth = constraints.maxWidth
        val placeable = measurables.single().measure(
            Constraints(
                minWidth = designWidth,
                maxWidth = designWidth,
                minHeight = 0,
                maxHeight = Constraints.Infinity,
            ),
        )
        val scale = availableWidth.toFloat() / designWidth
        val scaledHeight = ceil(placeable.height * scale).toInt()
        layout(availableWidth, scaledHeight) {
            placeable.placeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
            }
        }
    }
}

@Composable
private fun PreviewTopBar(
    onBack: () -> Unit,
    title: String = stringResource(R.string.ui_preview_432932),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = trackedClick("back", onBack)) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.ui_back_84a715),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResumePreview() {
    CvFreeTheme { ResumePreviewScreen(onBack = {}, onSave = {}, resume = previewDocument()) }
}
