package com.devpaul.features.home.ui

import com.devpaul.core.platform.analytics.trackedClick
import com.devpaul.core.platform.analytics.AppAnalytics
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devpaul.core.designsystem.component.CvBottomNavigation
import com.devpaul.core.designsystem.component.CvAppHeader
import com.devpaul.core.designsystem.component.CvHeaderAction
import com.devpaul.core.designsystem.component.CvMainDestination
import com.devpaul.core.domain.resume.ResumeDocument
import com.devpaul.core.platform.theme.CvFreeTheme
import com.devpaul.core.platform.profile.UserProfileUiState
import com.devpaul.core.platform.theme.SetStatusBarColor
import com.devpaul.core.platform.theme.LocalAppDarkTheme
import com.devpaul.features.home.R
import com.devpaul.features.home.ui.model.resumeProgress
import com.devpaul.features.home.ui.util.formatRecentEdit
import com.devpaul.features.home.ui.util.localizedDescription
import com.devpaul.features.home.ui.util.localizedMessage
import com.devpaul.features.home.ui.util.localizedName
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateDefinition
import com.devpaul.features.templates.ui.TemplateThumbnail

private val HomeNavy = Color(0xFF0B192C)
private val HomeSapphire = Color(0xFF1E3A8A)
private val HomeCobalt = Color(0xFF2563EB)
private val HomeEmerald = Color(0xFF10B981)

@Composable
fun HomeScreen(
    onNavigateToMyCvs: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onPreviewTemplate: (String) -> Unit,
    onContinueResume: (ResumeDocument) -> Unit,
    modifier: Modifier = Modifier,
    userProfile: UserProfileUiState = UserProfileUiState(isLoading = false),
    templates: List<ResumeTemplateDefinition> = ResumeTemplateCatalog.templates.take(3),
    recentResume: ResumeDocument? = null,
    onNavigateToNotifications: () -> Unit = {},
    isPremium: Boolean = false,
    onPreviewResume: (ResumeDocument) -> Unit = { onNavigateToMyCvs() },
) {
    val background = MaterialTheme.colorScheme.background
    SetStatusBarColor(background, darkIcons = !LocalAppDarkTheme.current)

    Scaffold(
        modifier = modifier,
        containerColor = background,
        bottomBar = {
            CvBottomNavigation(
                selectedDestination = CvMainDestination.Home,
                onDestinationSelected = { destination ->
                    when (destination) {
                        CvMainDestination.Home -> Unit
                        CvMainDestination.MyCvs -> onNavigateToMyCvs()
                        CvMainDestination.Settings -> onNavigateToSettings()
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .background(background),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                top = 12.dp,
                end = 20.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                HomeHeader(
                    profileState = userProfile,
                    onNotifications = onNavigateToNotifications,
                    isPremium = isPremium,
                )
            }
            item { AchievementBanner() }
            item {
                FeaturedTemplatesSection(
                    templates = templates,
                    onViewAll = onNavigateToTemplates,
                    onPreviewTemplate = onPreviewTemplate,
                )
            }
            item {
                RecentActivitySection(
                    resume = recentResume,
                    onContinue = onContinueResume,
                    onNavigateToMyCvs = onNavigateToMyCvs,
                    onPreview = onPreviewResume,
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    profileState: UserProfileUiState,
    onNotifications: () -> Unit,
    isPremium: Boolean,
) {
    Column {
        CvAppHeader(profile = profileState, isPremium = isPremium) {
            CvHeaderAction(
                icon = Icons.Outlined.Notifications,
                label = stringResource(R.string.home_notifications),
                onClick = trackedClick("open_notifications", onNotifications),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.clip(CircleShape)
                .background(HomeEmerald.copy(alpha = 0.08f))
                .border(1.dp, HomeEmerald.copy(alpha = 0.2f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(7.dp).background(HomeEmerald, CircleShape))
            Text(
                stringResource(R.string.home_workspace),
                color = if (LocalAppDarkTheme.current) Color(0xFF6EE7B7) else Color(0xFF047857),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(10.dp))
        if (profileState.isLoading) {
            val loadingDescription = stringResource(R.string.home_profile_loading)
            ProfileSkeleton(
                Modifier.fillMaxWidth(0.8f).height(44.dp)
                    .semantics {
                        contentDescription = loadingDescription
                        progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                    },
            )
        } else {
            Text(
                text = profileState.name.trim().takeIf(String::isNotEmpty)?.let {
                    stringResource(R.string.home_greeting_name, it)
                } ?: stringResource(R.string.home_greeting),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.7).sp,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.home_question),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun ProfileSkeleton(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "profile_loading")
    val alpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "profile_loading_alpha",
    )
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)))
}

@Preview(name = "Home Profile Loading", showBackground = true)
@Composable
private fun HomeProfileLoadingPreview() {
    CvFreeTheme {
        HomeScreen({}, {}, {}, {}, {}, userProfile = UserProfileUiState())
    }
}

@Preview(name = "Home Firebase Profile", showBackground = true)
@Composable
private fun HomeFirebaseProfilePreview() {
    CvFreeTheme {
        HomeScreen({}, {}, {}, {}, {}, userProfile = UserProfileUiState(isLoading = false, name = "Elena Vargas"))
    }
}

@Composable
private fun AchievementBanner() {
    var showTip by remember { mutableStateOf(false) }
    if (showTip) {
        AlertDialog(
            onDismissRequest = { showTip = false },
            icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) },
            title = { Text(stringResource(R.string.home_achievement_title)) },
            text = { Text(stringResource(R.string.home_impact_example)) },
            confirmButton = {
                TextButton(onClick = { showTip = false }) {
                    Text(stringResource(R.string.home_understood))
                }
            },
        )
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = HomeSapphire,
        shadowElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(listOf(Color(0xFF0F2744), HomeSapphire, HomeCobalt)),
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(98.dp),
                tint = Color.White.copy(alpha = 0.1f),
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Outlined.Star, null, Modifier.size(12.dp), tint = Color(0xFFFCD34D))
                    Text(
                        stringResource(R.string.home_impact_label),
                        color = Color(0xFFDBEAFE),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.home_achievement_title),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.home_achievement_description),
                    color = Color(0xFFDBEAFE),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = trackedClick("open_impact_tip") { showTip = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(stringResource(R.string.home_impact_action), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FeaturedTemplatesSection(
    templates: List<ResumeTemplateDefinition>,
    onViewAll: () -> Unit,
    onPreviewTemplate: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_featured_templates),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                )
                Text(
                    stringResource(R.string.home_templates_subtitle),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )
            }
            TextButton(onClick = trackedClick("view_all_templates", onViewAll)) {
                Text(stringResource(R.string.home_view_all), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onClick = trackedClick("preview_template") {
                        AppAnalytics.selection("preview_template", template.id)
                        onPreviewTemplate(template.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: ResumeTemplateDefinition,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(176.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
            shadowElevation = 1.dp,
        ) {
            Box(Modifier.padding(10.dp)) {
                TemplateThumbnail(
                    template = template,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(8.dp),
                )
                Text(
                    text = stringResource(
                        when (template.id) {
                            ResumeTemplateCatalog.BUDAPEST_ID -> R.string.home_template_executive
                            ResumeTemplateCatalog.RIGA_ID -> R.string.home_template_editorial
                            else -> R.string.home_template_classic
                        },
                    ),
                    modifier = Modifier.align(Alignment.TopEnd).padding(3.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (template.id == ResumeTemplateCatalog.RIGA_ID) HomeCobalt else HomeNavy.copy(alpha = 0.88f))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = template.localizedName(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = template.localizedDescription(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RecentActivitySection(
    resume: ResumeDocument?,
    onContinue: (ResumeDocument) -> Unit,
    onNavigateToMyCvs: () -> Unit,
    onPreview: (ResumeDocument) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.home_recent_activity),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (resume == null) {
            EmptyRecentActivityCard(onNavigateToMyCvs = onNavigateToMyCvs)
        } else {
            RecentResumeCard(
                resume = resume,
                onContinue = { onContinue(resume) },
                onNavigateToMyCvs = onNavigateToMyCvs,
                onPreview = { onPreview(resume) },
            )
        }
    }
}

@Composable
private fun RecentResumeCard(
    resume: ResumeDocument,
    onContinue: () -> Unit,
    onNavigateToMyCvs: () -> Unit,
    onPreview: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }
    val progress = resumeProgress(resume)
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resume.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = formatRecentEdit(context, resume.updatedAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                    )
                }
                Box {
                    IconButton(onClick = trackedClick("open_recent_resume_menu") { showActions = true }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = stringResource(R.string.home_more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = showActions,
                        onDismissRequest = { showActions = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_continue_editing)) },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = trackedClick("continue_recent_resume") {
                                showActions = false
                                onContinue()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_view_my_resumes)) },
                            leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                            onClick = trackedClick("view_recent_resume") {
                                showActions = false
                                onNavigateToMyCvs()
                            },
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Row {
                Text(
                    text = stringResource(R.string.home_profile_strength),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "${progress.percent}%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp)
                    .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress.percent / 100f, 0f..1f) },
            ) {
                if (progress.percent > 0) {
                    Box(
                        Modifier.fillMaxWidth(progress.percent / 100f).height(6.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(HomeSapphire, HomeCobalt, HomeEmerald))),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (progress.percent == 100) {
                    Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp), tint = HomeEmerald)
                }
                Text(
                    text = progress.localizedMessage(),
                    color = if (progress.percent == 100) {
                        if (LocalAppDarkTheme.current) Color(0xFF6EE7B7) else Color(0xFF047857)
                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = trackedClick("continue_recent_resume", onContinue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            ) {
                Text(stringResource(R.string.home_continue_editing), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = trackedClick("preview_recent_resume", onPreview),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeNavy, contentColor = Color.White),
            ) {
                Icon(Icons.Outlined.Visibility, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_preview_pdf), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Outlined.Download, null, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmptyRecentActivityCard(onNavigateToMyCvs: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_no_activity),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_no_activity_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(onClick = trackedClick("open_my_cvs", onNavigateToMyCvs)) {
                Text(stringResource(R.string.home_go_to_my_resumes))
            }
        }
    }
}

@Preview(name = "Home Light", showBackground = true, heightDp = 1200)
@Composable
private fun HomeLightPreview() {
    CvFreeTheme(darkTheme = false) {
        HomeScreen({}, {}, {}, {}, {})
    }
}

@Preview(name = "Home Dark", showBackground = true, heightDp = 1200)
@Composable
private fun HomeDarkPreview() {
    CvFreeTheme(darkTheme = true) {
        HomeScreen({}, {}, {}, {}, {})
    }
}
