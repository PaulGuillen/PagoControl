package com.devpaul.features.resume.ui

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.core.platform.analytics.TrackAnalyticsView
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.core.designsystem.component.CvBottomNavigation
import com.devpaul.core.designsystem.component.CvMainDestination
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.domain.resume.ResumeContact
import com.devpaul.core.domain.resume.ResumeEducation
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.core.domain.resume.ResumeStorageState
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.resume.R
import com.devpaul.features.resume.importing.ResumeImportPolicy
import com.devpaul.features.resume.importing.ResumeAiAvailability
import com.devpaul.features.resume.importing.ResumeAiQuota
import com.devpaul.features.resume.ui.model.CvFilter
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateRenderer
import com.devpaul.features.templates.ui.localizedName
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCvsScreen(
    modifier: Modifier = Modifier,
    importPolicy: ResumeImportPolicy = ResumeImportPolicy(),
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onCreateCv: () -> Unit,
    onImportPdf: (Uri, Boolean) -> Unit = { _, _ -> },
    resumes: List<ResumeDocument> = emptyList(),
    onEditResume: (ResumeDocument) -> Unit = {},
    onViewResume: (ResumeDocument) -> Unit = {},
    onDeleteResume: (ResumeDocument) -> Unit = {},
    userProfile: UserProfileUiState = UserProfileUiState(isLoading = false),
    isPremium: Boolean = false,
    onNavigateToNotifications: () -> Unit = {},
    onDuplicateResume: suspend (ResumeDocument) -> Unit = {},
    onChangeTemplate: (ResumeDocument) -> Unit = {},
    storageState: ResumeStorageState = ResumeStorageState(),
    onRetrySync: () -> Unit = {},
    onMigrateLocalResumes: suspend () -> Unit = {},
) {
    var showMigrationConfirmation by remember { mutableStateOf(false) }
    val migrationError = stringResource(R.string.resume_cloud_migration_error)
    var selectedFilterName by rememberSaveable { mutableStateOf(CvFilter.All.name) }
    var showCreateOptions by rememberSaveable { mutableStateOf(false) }
    var pendingImportAction by remember { mutableStateOf<ImportAction?>(null) }
    var useAiForPendingImport by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<ResumeDocument?>(null) }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var gridView by rememberSaveable { mutableStateOf(false) }
    var selectedResumeId by rememberSaveable { mutableStateOf<String?>(null) }
    var isDuplicating by remember { mutableStateOf(false) }
    var pendingQuickActionName by rememberSaveable { mutableStateOf<String?>(null) }
    val duplicateSuccess = stringResource(R.string.cvs_duplicate_success)
    val duplicateError = stringResource(R.string.cvs_duplicate_error)
    val selectedFilter = CvFilter.valueOf(selectedFilterName)
    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    if (showMigrationConfirmation) {
        AlertDialog(
            onDismissRequest = { showMigrationConfirmation = false },
            title = { Text(stringResource(R.string.resume_cloud_import_local)) },
            text = { Text(stringResource(R.string.resume_cloud_migration_confirm, userProfile.email)) },
            confirmButton = {
                TextButton(onClick = {
                    showMigrationConfirmation = false
                    coroutineScope.launch {
                        try {
                            onMigrateLocalResumes()
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            snackbarHostState.showSnackbar(migrationError)
                        }
                    }
                }) { Text(stringResource(R.string.resume_cloud_import_local)) }
            },
            dismissButton = {
                TextButton(onClick = { showMigrationConfirmation = false }) {
                    Text(stringResource(R.string.resume_cloud_later))
                }
            },
        )
    }
    val aiQuota by ResumeAiQuota.status.collectAsState()
    val canUseAi = aiQuota.canUseAi(importPolicy)
    LaunchedEffect(showCreateOptions, importPolicy.aiEnhancementEnabled) {
        if (showCreateOptions && importPolicy.aiEnhancementEnabled) {
            ResumeAiQuota.refresh(context)
        }
    }
    fun performQuickAction(action: CvQuickAction, resume: ResumeDocument) {
        pendingQuickActionName = null
        selectedResumeId = resume.id
        when (action) {
            CvQuickAction.Template -> onChangeTemplate(resume)
            CvQuickAction.Duplicate -> {
                if (isDuplicating) return
                isDuplicating = true
                coroutineScope.launch {
                    try {
                        onDuplicateResume(resume)
                        snackbarHostState.showSnackbar(duplicateSuccess)
                    } catch (cancelled: kotlinx.coroutines.CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        AppAnalytics.error("duplicate_resume", error)
                        snackbarHostState.showSnackbar(duplicateError)
                    } finally {
                        isDuplicating = false
                    }
                }
            }
        }
    }

    fun requestQuickAction(action: CvQuickAction) {
        val onlyResume = resumes.singleOrNull()
        if (onlyResume != null) {
            performQuickAction(action, onlyResume)
        } else if (resumes.isNotEmpty()) {
            pendingQuickActionName = action.name
        }
    }

    val scannedPdfError = stringResource(R.string.ui_the_scanned_pdf_couldn_t_be_created_13dfd7)
    val scannerUnavailableError = stringResource(R.string.ui_the_scanner_isn_t_available_on_this_device_fcd110)
    val scannerOpenError = stringResource(R.string.ui_the_scanner_couldn_t_be_opened_try_again_0cdc7e)
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }.onFailure { AppAnalytics.error("persist_import_permission", it) }
            onImportPdf(uri, useAiForPendingImport)
        }
    }
    val scanner = remember {
        GmsDocumentScanning.getClient(
            GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(true)
                .setPageLimit(12)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                .build(),
        )
    }
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scannedPdf = GmsDocumentScanningResult
                .fromActivityResultIntent(result.data)
                ?.pdf
                ?.uri
            if (scannedPdf != null) {
                onImportPdf(scannedPdf, useAiForPendingImport)
            } else {
                AppAnalytics.error("scan_resume", code = "missing_pdf")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(scannedPdfError)
                }
            }
        }
    }

    fun launchScanner() {
        val hostActivity = activity
        if (hostActivity == null) {
            AppAnalytics.error("scan_resume", code = "missing_activity")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(scannerUnavailableError)
            }
            return
        }
        scanner.getStartScanIntent(hostActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { error ->
                AppAnalytics.error("open_document_scanner", error)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(scannerOpenError)
                }
            }
    }

    fun launchImport(action: ImportAction, useAi: Boolean) {
        useAiForPendingImport = useAi
        showCreateOptions = false
        when (action) {
            ImportAction.Pdf -> pdfPicker.launch(arrayOf("application/pdf"))
            ImportAction.Scan -> launchScanner()
        }
    }

    fun requestImport(action: ImportAction) {
        if (canUseAi) {
            pendingImportAction = action
        } else {
            launchImport(action, useAi = false)
        }
    }
    val visibleResumes = resumes
        .filter {
            searchQuery.isBlank() || it.displayName.contains(searchQuery.trim(), ignoreCase = true) ||
                ResumeTemplateCatalog.find(it.templateName).displayName.contains(searchQuery.trim(), ignoreCase = true)
        }
        .let { filtered ->
            when (selectedFilter) {
                CvFilter.All -> filtered
                CvFilter.Recent -> filtered.sortedByDescending(ResumeDocument::updatedAt)
                CvFilter.Template -> filtered.sortedBy(ResumeDocument::templateName)
            }
        }
    val background = MaterialTheme.colorScheme.background

    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Scaffold(
        modifier = modifier,
        containerColor = background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CvBottomNavigation(
                selectedDestination = CvMainDestination.MyCvs,
                onDestinationSelected = { destination ->
                    when (destination) {
                         CvMainDestination.Home -> onNavigateToHome()
                        CvMainDestination.MyCvs -> Unit
                        CvMainDestination.Settings -> onNavigateToSettings()
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = trackedClick("open_create_options") {
                    ResumeAiQuota.markChecking()
                    showCreateOptions = true
                },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                containerColor = Color.Transparent,
                contentColor = Color.White,
            ) {
                Box(
                    Modifier.fillMaxSize().background(Brush.linearGradient(listOf(CvsNavy, CvsSapphire, CvsBlue))),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.ui_create_or_import_resume_cf411e),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 96.dp,
            ),
        ) {
            item {
                CvsHeader(
                    profile = userProfile,
                    isPremium = isPremium,
                    onSearch = {
                        searchVisible = !searchVisible
                        if (!searchVisible) searchQuery = ""
                    },
                    onNotifications = onNavigateToNotifications,
                )
                ResumeStorageBanner(
                    state = storageState,
                    onRetry = onRetrySync,
                    onMigrate = { showMigrationConfirmation = true },
                )
                Spacer(Modifier.height(16.dp))
                if (searchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.cvs_search)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.ui_my_resumes_29f302),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        modifier = Modifier.size(26.dp),
                        shape = CircleShape,
                        color = CvsBlue,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = resumes.size.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    CvsViewToggle(gridView = gridView, onChange = { gridView = it })
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CvFilter.entries.forEach { filter ->
                        FilterChip(
                            filter = filter,
                            selected = filter == selectedFilter,
                            onClick = trackedClick("filter_resumes") {
                                AppAnalytics.selection("resume_filter", filter.name.lowercase())
                                selectedFilterName = filter.name
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (visibleResumes.isEmpty()) {
                item {
                    if (resumes.isNotEmpty()) {
                        Text(
                            stringResource(R.string.cvs_no_results),
                            Modifier.padding(vertical = 32.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else EmptyCvsState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                    )
                }
            } else if (gridView) {
                items(visibleResumes.chunked(2), key = { it.first().id }) { row ->
                    Row(
                        modifier = Modifier.padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { resume ->
                            Box(Modifier.weight(1f)) {
                                ResumeCard(
                                    resume = resume,
                                    isSelected = selectedResumeId == resume.id,
                                    onSelect = { selectedResumeId = resume.id },
                                    onEdit = { onEditResume(resume) },
                                    onView = { onViewResume(resume) },
                                    onDelete = { pendingDelete = resume },
                                    compact = true,
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(
                    items = visibleResumes,
                    key = ResumeDocument::id,
                ) { resume ->
                    Spacer(modifier = Modifier.height(14.dp))
                    ResumeCard(
                        resume = resume,
                        isSelected = selectedResumeId == resume.id,
                        onSelect = { selectedResumeId = resume.id },
                        onEdit = { onEditResume(resume) },
                        onView = { onViewResume(resume) },
                        onDelete = { pendingDelete = resume },
                    )
                }
            }
            if (resumes.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(14.dp))
                    CvsQuickActions(
                        enabled = !isDuplicating,
                        onDuplicate = { requestQuickAction(CvQuickAction.Duplicate) },
                        onTemplate = { requestQuickAction(CvQuickAction.Template) },
                        resumeName = resumes.singleOrNull()?.displayName,
                    )
                }
            }
        }
    }

    pendingQuickActionName?.let { actionName ->
        val action = CvQuickAction.valueOf(actionName)
        AlertDialog(
            onDismissRequest = { pendingQuickActionName = null },
            title = {
                Text(stringResource(
                    if (action == CvQuickAction.Duplicate) R.string.cvs_choose_duplicate else R.string.cvs_choose_template,
                ))
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    resumes.sortedByDescending(ResumeDocument::updatedAt).forEach { resume ->
                        Surface(
                            onClick = { performQuickAction(action, resume) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(resume.displayName, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    stringResource(
                                        R.string.cvs_choose_details,
                                        ResumeTemplateCatalog.find(resume.templateName).localizedName(),
                                        formatResumeDate(resume.updatedAt),
                                    ),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pendingQuickActionName = null }) {
                    Text(stringResource(R.string.ui_cancel_c111e0))
                }
            },
        )
    }

    if (showCreateOptions) {
        TrackAnalyticsView("create_resume_sheet")
        ModalBottomSheet(
            onDismissRequest = { showCreateOptions = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            ) {
                Text(
                    text = stringResource(R.string.ui_create_resume_1ba285),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ui_choose_how_you_want_to_begin_c950b6),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (importPolicy.aiEnhancementEnabled) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when (aiQuota.availability) {
                            ResumeAiAvailability.Checking -> stringResource(R.string.ai_quota_checking)
                            ResumeAiAvailability.Available -> stringResource(
                                if (importPolicy.isFreePlan) R.string.ai_quota_available_free else R.string.ai_quota_available,
                            )
                            ResumeAiAvailability.Limited -> stringResource(
                                R.string.ai_quota_used_until,
                                java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
                                    .format(Date(checkNotNull(aiQuota.nextAvailableAtMillis))),
                            )
                            ResumeAiAvailability.Busy -> stringResource(R.string.ai_quota_busy)
                            ResumeAiAvailability.Unavailable -> stringResource(R.string.ai_quota_unverified)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                CreateResumeOption(
                    icon = Icons.Outlined.EditNote,
                    title = stringResource(R.string.ui_start_from_scratch_fad4a9),
                    description = stringResource(R.string.ui_complete_each_section_step_by_step_af5b50),
                    onClick = trackedClick("create_resume") {
                        showCreateOptions = false
                        onCreateCv()
                    },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
                CreateResumeOption(
                    icon = Icons.Outlined.DocumentScanner,
                    title = stringResource(R.string.ui_scan_document_c873c8),
                    description = stringResource(R.string.ui_capture_crop_and_enhance_multiple_pages_automatically_5952c0),
                    onClick = trackedClick("scan_resume") { requestImport(ImportAction.Scan) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outline,
                )
                CreateResumeOption(
                    icon = Icons.Outlined.UploadFile,
                    title = stringResource(R.string.ui_import_pdf_23e4c6),
                    description = if (canUseAi) {
                        stringResource(R.string.ui_analyze_and_organize_the_content_with_ai_assistance_7b0063)
                    } else {
                        stringResource(R.string.ui_extract_and_organize_the_content_directly_on_the_device_1ea11b)
                    },
                    onClick = trackedClick("import_pdf") { requestImport(ImportAction.Pdf) },
                )
            }
        }
    }

    pendingImportAction?.let { action ->
        TrackAnalyticsView("ai_import_confirmation")
        AlertDialog(
            onDismissRequest = trackedClick("dismiss_ai_import") { pendingImportAction = null },
            title = { Text(stringResource(R.string.ui_smart_processing_a52050)) },
            text = {
                Text(
                    stringResource(R.string.ui_to_organize_the_resume_the_recognized_text_will_be_sent_secu_4ee2d9) +
                        if (importPolicy.isFreePlan) "\n\n" + stringResource(R.string.ai_weekly_free_notice) else "",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = trackedClick("confirm_ai_import") {
                        pendingImportAction = null
                        launchImport(action, useAi = true)
                    },
                ) {
                    Text(stringResource(R.string.ui_continue_b8cb6f))
                }
            },
            dismissButton = {
                TextButton(onClick = trackedClick("cancel_ai_import") { pendingImportAction = null }) {
                    Text(stringResource(R.string.ui_cancel_c111e0))
                }
            },
        )
    }

    pendingDelete?.let { resume ->
        TrackAnalyticsView("delete_resume_dialog")
        AlertDialog(
            onDismissRequest = trackedClick("dismiss_delete_resume") { pendingDelete = null },
            title = { Text(stringResource(R.string.ui_delete_resume_daf230)) },
            text = {
                Text(
                    stringResource(R.string.resume_delete_confirmation, resume.displayName),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = trackedClick("confirm_delete_resume") {
                        pendingDelete = null
                        onDeleteResume(resume)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.ui_delete_54e140))
                }
            },
            dismissButton = {
                TextButton(onClick = trackedClick("cancel_delete_resume") { pendingDelete = null }) {
                    Text(stringResource(R.string.ui_cancel_c111e0))
                }
            },
        )
    }
}

private enum class ImportAction {
    Pdf,
    Scan,
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun CreateResumeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(25.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ResumeCard(
    resume: ResumeDocument,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit,
    onDelete: () -> Unit,
    compact: Boolean = false,
) {
    var showActionsMenu by remember { mutableStateOf(false) }
    val template = ResumeTemplateCatalog.find(resume.templateName)

    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth().semantics { selected = isSelected },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) CvsBlue else MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                ResumeThumbnail(renderer = template.renderer)
                if (compact) {
                    Spacer(Modifier.weight(1f))
                } else {
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        ResumeCardMetadata(resume, template.localizedName(), isSelected)
                    }
                }
                Box {
                    IconButton(
                        onClick = trackedClick("open_resume_menu") { showActionsMenu = true },
                        modifier = Modifier.size(if (compact) 32.dp else 36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.resume_more_options, resume.displayName),
                        )
                    }
                    DropdownMenu(
                        expanded = showActionsMenu,
                        onDismissRequest = { showActionsMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ui_delete_resume_daf230), color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = trackedClick("delete_resume") {
                                showActionsMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }
            if (compact) {
                Spacer(Modifier.height(12.dp))
                ResumeCardMetadata(resume, template.localizedName(), isSelected)
            }
            Spacer(modifier = Modifier.height(12.dp))
            CvsProfileStrength(resume = resume, compact = compact)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = trackedClick("edit_resume") {
                        onSelect()
                        onEdit()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CvsBlue, contentColor = Color.White),
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = if (compact) stringResource(R.string.ui_edit_2b5f23) else null, modifier = Modifier.size(16.dp))
                    if (!compact) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_edit_2b5f23), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                FilledTonalButton(
                    onClick = trackedClick("view_resume") {
                        onSelect()
                        onView()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = if (compact) stringResource(R.string.ui_view_49b372) else null, modifier = Modifier.size(16.dp))
                    if (!compact) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ui_view_49b372), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeCardMetadata(resume: ResumeDocument, templateName: String, isSelected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                templateName,
                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isSelected) {
            Surface(shape = RoundedCornerShape(5.dp), color = Color(0xFF10B981).copy(alpha = 0.1f)) {
                Text(
                    stringResource(R.string.cvs_active),
                    Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    color = if (LocalAppDarkTheme.current) Color(0xFF6EE7B7) else Color(0xFF047857),
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    Text(
        text = resume.displayName,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    Spacer(Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Schedule, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.resume_updated, formatResumeDate(resume.updatedAt)),
            Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ResumeThumbnail(renderer: ResumeTemplateRenderer) {
    Surface(
        modifier = Modifier.size(width = 56.dp, height = 76.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
    ) {
        if (renderer == ResumeTemplateRenderer.TokyoCompact) {
            TokyoResumeThumbnail()
        } else if (renderer == ResumeTemplateRenderer.PragueEditorial) {
            PragueResumeThumbnail()
        } else {
            Row(modifier = Modifier.padding(6.dp)) {
                if (renderer == ResumeTemplateRenderer.Budapest) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(androidx.compose.ui.graphics.Color.White)
                        .padding(5.dp),
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (index == 0) 0.55f else 1f)
                                .height(if (index == 0) 5.dp else 2.dp)
                                .background(
                                    if (index == 0) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color(0xFFCBD5E1),
                                    CircleShape,
                                ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    if (renderer == ResumeTemplateRenderer.Riga) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(Modifier.weight(0.3f).fillMaxHeight())
                            Box(
                                Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(androidx.compose.ui.graphics.Color(0xFF414143)),
                            )
                            Box(Modifier.weight(0.7f).fillMaxHeight())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TokyoResumeThumbnail() {
    val ink = androidx.compose.ui.graphics.Color(0xFF30333A)
    val muted = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
    val section = androidx.compose.ui.graphics.Color(0xFFEAF2FF)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .background(androidx.compose.ui.graphics.Color.White)
            .padding(horizontal = 5.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth(0.65f).height(6.dp).background(ink))
        Spacer(Modifier.height(3.dp))
        Box(Modifier.fillMaxWidth(0.38f).height(3.dp).background(muted))
        Spacer(Modifier.height(5.dp))
        repeat(2) { index ->
            Box(Modifier.fillMaxWidth().height(7.dp).background(section))
            Spacer(Modifier.height(4.dp))
            repeat(if (index == 1) 3 else 2) { line ->
                Box(
                    Modifier
                        .fillMaxWidth(if (line == 0) 1f else 0.8f)
                        .height(if (line == 0) 2.5.dp else 2.dp)
                        .background(if (line == 0) ink else muted),
                )
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun PragueResumeThumbnail() {
    val ink = androidx.compose.ui.graphics.Color(0xFF414143)
    val muted = androidx.compose.ui.graphics.Color(0xFFCBD5E1)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp)
            .background(androidx.compose.ui.graphics.Color.White)
            .padding(5.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(0.43f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(5.dp).background(ink))
                Box(Modifier.fillMaxWidth().height(1.dp).background(ink))
                Box(Modifier.fillMaxWidth(0.7f).height(2.dp).background(muted))
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(ink))
            Column(
                modifier = Modifier
                    .weight(0.57f)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Box(Modifier.fillMaxWidth(0.5f).height(3.dp).background(ink))
                repeat(3) { Box(Modifier.fillMaxWidth().height(2.dp).background(muted)) }
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(ink))
        Spacer(Modifier.height(5.dp))
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .weight(0.29f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(5) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 2 == 0) 1f else 0.72f)
                            .height(if (index % 2 == 0) 3.dp else 2.dp)
                            .background(if (index % 2 == 0) ink else muted),
                    )
                }
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(ink))
            Column(
                modifier = Modifier
                    .weight(0.71f)
                    .padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(6) { index ->
                    Box(
                        Modifier
                            .fillMaxWidth(if (index % 3 == 0) 0.72f else 1f)
                            .height(if (index % 3 == 0) 3.dp else 2.dp)
                            .background(if (index % 3 == 0) ink else muted),
                    )
                }
            }
        }
    }
}

private enum class CvQuickAction { Duplicate, Template }

private fun formatResumeDate(value: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(value))

@Composable
private fun FilterChip(
    filter: CvFilter,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (selected) 3.dp else 0.dp,
        color = if (selected) CvsBlue else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        border = if (selected) null else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(filter.label),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyCvsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier.size(68.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.ui_you_don_t_have_any_resumes_yet_5c79fc),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.ui_create_your_first_professional_resume_with_the_button_f321a0),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "My CVs Light", showBackground = true, heightDp = 860)
@Composable
private fun MyCvsLightPreview() {
    CvFreeTheme(darkTheme = false) {
        MyCvsScreen(
            onNavigateToHome = {},
            onNavigateToSettings = {},
            onCreateCv = {},
            resumes = listOf(previewResume()),
            userProfile = UserProfileUiState(isLoading = false, name = "Paul Guillen"),
            isPremium = true,
        )
    }
}

@Preview(name = "My CVs Dark", showBackground = true, heightDp = 860)
@Composable
private fun MyCvsDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        MyCvsScreen(
            onNavigateToHome = {},
            onNavigateToSettings = {},
            onCreateCv = {},
            resumes = listOf(previewResume()),
            userProfile = UserProfileUiState(isLoading = false, name = "Paul Guillen"),
            isPremium = true,
        )
    }
}

private fun previewResume() = ResumeDocument(
    id = "preview",
    updatedAt = 1792713600000L,
    templateName = ResumeTemplateCatalog.TOKYO_COMPACT_ID,
    contact = ResumeContact(name = "Paul", lastName = "Guillen", professionalTitle = "Ingeniero de Sistemas", email = "paul@example.com"),
    experiences = listOf(ResumeExperience(position = "Desarrollador Android", company = "Empresa")),
    education = listOf(ResumeEducation(institution = "Universidad", degree = "Ingeniería de Sistemas")),
    skills = listOf("Kotlin", "Android"),
    summary = "Desarrollo de aplicaciones móviles.",
)
