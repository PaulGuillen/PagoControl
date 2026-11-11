package com.devpaul.features.resume

import com.devpaul.features.resume.R

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpaul.core.platform.profile.UserProfileUiState
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.devpaul.core.domain.resume.ResumeOptionalSection
import com.devpaul.core.domain.resume.ResumeRepository
import com.devpaul.features.resume.importing.ResumeImportPolicy
import com.devpaul.features.resume.ui.MyCvsScreen
import com.devpaul.features.resume.ui.contact.ContactScreen
import com.devpaul.features.resume.ui.courses.CoursesScreen
import com.devpaul.features.resume.ui.education.EducationScreen
import com.devpaul.features.resume.ui.experience.ExperienceScreen
import com.devpaul.features.resume.ui.importing.ResumeImportScreen
import com.devpaul.features.resume.ui.languages.LanguagesScreen
import com.devpaul.features.resume.ui.links.LinksScreen
import com.devpaul.features.resume.ui.references.ReferencesScreen
import com.devpaul.features.resume.ui.sections.SectionSelectionScreen
import com.devpaul.features.resume.ui.skills.SkillsScreen
import com.devpaul.features.resume.ui.summary.SummaryScreen
import com.devpaul.features.templates.ui.ResumePreviewScreen
import com.devpaul.features.templates.ui.TemplateSelectionScreen
import com.devpaul.navigation.route.ResumeRoute
import kotlinx.coroutines.launch
import androidx.core.net.toUri

fun NavGraphBuilder.resumeGraph(
    navController: NavHostController,
    repository: ResumeRepository,
    draftState: ResumeDraftState,
    importPolicy: ResumeImportPolicy = ResumeImportPolicy(),
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onResumeSaved: () -> Unit,
    userProfile: StateFlow<UserProfileUiState>? = null,
    isPremium: Boolean = false,
    onNavigateToNotifications: () -> Unit = {},
) {
    fun selectedSections() = draftState.document.optionalSections

    fun nextOptionalSection(after: ResumeOptionalSection?): ResumeOptionalSection? {
        val orderedSections = ResumeOptionalSection.entries
        val startIndex = after?.let { orderedSections.indexOf(it) + 1 } ?: 0
        return orderedSections.drop(startIndex).firstOrNull(selectedSections()::contains)
    }

    fun routeFor(section: ResumeOptionalSection): Any = when (section) {
        ResumeOptionalSection.Courses -> ResumeRoute.Courses
        ResumeOptionalSection.Links -> ResumeRoute.Links
        ResumeOptionalSection.Languages -> ResumeRoute.Languages
        ResumeOptionalSection.References -> ResumeRoute.References
    }

    fun navigateAfter(section: ResumeOptionalSection?) {
        val nextSection = nextOptionalSection(section)
        navController.navigate(nextSection?.let(::routeFor) ?: ResumeRoute.Summary)
    }

    fun totalSteps() = 5 + selectedSections().size

    fun optionalStep(section: ResumeOptionalSection): Int {
        val selectedInOrder = ResumeOptionalSection.entries.filter(selectedSections()::contains)
        return 5 + selectedInOrder.indexOf(section)
    }

    navigation<ResumeRoute.Graph>(
        startDestination = ResumeRoute.ResumeList,
    ) {
        composable<ResumeRoute.ResumeList> {
        val resumes by repository.resumes.collectAsState(initial = emptyList())
        val storageState by repository.storageState.collectAsState(initial = com.devpaul.core.domain.resume.ResumeStorageState(isLoading = true))
        val coroutineScope = rememberCoroutineScope()

        MyCvsScreen(
            storageState = storageState,
            onRetrySync = repository::retrySync,
            onMigrateLocalResumes = repository::migrateLocalResumes,
            userProfile = userProfile?.collectAsStateWithLifecycle()?.value ?: UserProfileUiState(isLoading = false),
            isPremium = isPremium,
            onNavigateToNotifications = onNavigateToNotifications,
            onDuplicateResume = { resume ->
                repository.save(resume.copy(id = UUID.randomUUID().toString(), updatedAt = System.currentTimeMillis()))
            },
            onChangeTemplate = { resume ->
                draftState.load(resume)
                navController.navigate(ResumeRoute.Template)
            },
            resumes = resumes,
            importPolicy = importPolicy,
            onCreateCv = {
                draftState.startNew(emptySet())
                navController.navigate(ResumeRoute.SectionSelection)
            },
            onImportPdf = { uri, useAi ->
                navController.navigate(ResumeRoute.Importing(uri.toString(), useAi))
            },
            onEditResume = { resume ->
                draftState.load(resume)
                navController.navigate(ResumeRoute.SectionSelection)
            },
            onViewResume = { resume ->
                draftState.load(resume)
                navController.navigate(ResumeRoute.Preview)
            },
            onDeleteResume = { resume ->
                coroutineScope.launch { draftState.delete(resume.id) }
            },
            onNavigateToHome = onNavigateToHome,
            onNavigateToSettings = onNavigateToSettings,
        )
        }

        composable<ResumeRoute.Importing> { backStackEntry ->
        val route = backStackEntry.toRoute<ResumeRoute.Importing>()
        ResumeImportScreen(
            documentUri = route.uri.toUri(),
            importPolicy = importPolicy.copy(aiEnhancementEnabled = importPolicy.aiEnhancementEnabled && route.useAi),
            onBack = { navController.popBackStack() },
            onComplete = { importedResume ->
                draftState.importResume(importedResume)
                navController.navigate(ResumeRoute.SectionSelection) {
                    popUpTo(ResumeRoute.ResumeList) { inclusive = false }
                }
            },
        )
        }

        composable<ResumeRoute.SectionSelection> {
        KeepResumeDraft(draftState, ResumeDraftSection.Sections)
        SectionSelectionScreen(
            onDraftChange = { sections ->
                draftState.updatePending(ResumeDraftSection.Sections, draftState.document.copy(optionalSections = sections))
            },
            initialSections = draftState.document.optionalSections,
            onBack = { navController.popBackStack() },
            onConfirm = { sections ->
                draftState.updateOptionalSections(sections)
                navController.navigate(ResumeRoute.Contact)
            },
        )
        }

        composable<ResumeRoute.Contact> {
        KeepResumeDraft(draftState, ResumeDraftSection.Contact)
        ContactScreen(
            onDraftChange = { contact ->
                draftState.updatePending(ResumeDraftSection.Contact, draftState.document.copy(contact = contact))
            },
            totalSteps = totalSteps(),
            initialData = draftState.document.contact,
            onBack = { navController.popBackStack() },
            onContinue = { contact ->
                draftState.updateContact(contact)
                navController.navigate(ResumeRoute.Experience)
            },
        )
        }

        composable<ResumeRoute.Experience> {
        KeepResumeDraft(draftState, ResumeDraftSection.Experience)
        ExperienceScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.Experience,
                    pending?.let { entry ->
                        draftState.document.copy(
                            experiences = draftState.document.experiences.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.Experience) },
            totalSteps = totalSteps(),
            experiences = draftState.document.experiences,
            onExperiencesChange = draftState::updateExperiences,
            onBack = { navController.popBackStack() },
            onContinue = {
                navController.navigate(ResumeRoute.Education)
            },
        )
        }

        composable<ResumeRoute.Education> {
        KeepResumeDraft(draftState, ResumeDraftSection.Education)
        EducationScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.Education,
                    pending?.let { entry ->
                        draftState.document.copy(
                            education = draftState.document.education.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.Education) },
            totalSteps = totalSteps(),
            education = draftState.document.education,
            onEducationChange = draftState::updateEducation,
            onBack = { navController.popBackStack() },
            onContinue = {
                navController.navigate(ResumeRoute.Skills)
            },
        )
        }

        composable<ResumeRoute.Skills> {
        KeepResumeDraft(draftState, ResumeDraftSection.Skills)
        SkillsScreen(
            onDraftChange = { skills ->
                draftState.updatePending(ResumeDraftSection.Skills, draftState.document.copy(skills = skills))
            },
            totalSteps = totalSteps(),
            initialSkills = draftState.document.skills,
            onBack = { navController.popBackStack() },
            onContinue = { skills ->
                draftState.updateSkills(skills)
                navigateAfter(null)
            },
        )
        }

        composable<ResumeRoute.Courses> {
        KeepResumeDraft(draftState, ResumeDraftSection.Courses)
        CoursesScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.Courses,
                    pending?.let { entry ->
                        draftState.document.copy(
                            courses = draftState.document.courses.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.Courses) },
            step = optionalStep(ResumeOptionalSection.Courses),
            totalSteps = totalSteps(),
            actionText = stringResource(R.string.ui_continue_4ad2b3),
            courses = draftState.document.courses,
            onCoursesChange = draftState::updateCourses,
            onBack = { navController.popBackStack() },
            onContinue = { navigateAfter(ResumeOptionalSection.Courses) },
        )
        }

        composable<ResumeRoute.Links> {
        KeepResumeDraft(draftState, ResumeDraftSection.Links)
        LinksScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.Links,
                    pending?.let { entry ->
                        draftState.document.copy(
                            links = draftState.document.links.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.Links) },
            step = optionalStep(ResumeOptionalSection.Links),
            totalSteps = totalSteps(),
            actionText = stringResource(R.string.ui_continue_4ad2b3),
            links = draftState.document.links,
            onLinksChange = draftState::updateLinks,
            onBack = { navController.popBackStack() },
            onContinue = { navigateAfter(ResumeOptionalSection.Links) },
        )
        }

        composable<ResumeRoute.Languages> {
        KeepResumeDraft(draftState, ResumeDraftSection.Languages)
        LanguagesScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.Languages,
                    pending?.let { entry ->
                        draftState.document.copy(
                            languages = draftState.document.languages.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.Languages) },
            step = optionalStep(ResumeOptionalSection.Languages),
            totalSteps = totalSteps(),
            actionText = stringResource(R.string.ui_continue_4ad2b3),
            selectedLanguages = draftState.document.languages,
            onLanguagesChange = draftState::updateLanguages,
            onBack = { navController.popBackStack() },
            onContinue = { navigateAfter(ResumeOptionalSection.Languages) },
        )
        }

        composable<ResumeRoute.References> {
        KeepResumeDraft(draftState, ResumeDraftSection.References)
        ReferencesScreen(
            onDraftChange = { pending ->
                draftState.updatePending(
                    ResumeDraftSection.References,
                    pending?.let { entry ->
                        draftState.document.copy(
                            references = draftState.document.references.withDraft(entry) { it.id },
                        )
                    },
                )
            },
            onDraftFinished = { draftState.commitPending(ResumeDraftSection.References) },
            step = optionalStep(ResumeOptionalSection.References),
            totalSteps = totalSteps(),
            actionText = stringResource(R.string.ui_continue_4ad2b3),
            references = draftState.document.references,
            onReferencesChange = draftState::updateReferences,
            onBack = { navController.popBackStack() },
            onContinue = { navigateAfter(ResumeOptionalSection.References) },
        )
        }

        composable<ResumeRoute.Summary> {
        KeepResumeDraft(draftState, ResumeDraftSection.Summary)
        SummaryScreen(
            onDraftChange = { summary, formatting ->
                draftState.updatePending(
                    ResumeDraftSection.Summary,
                    draftState.document.copy(summary = summary, summaryFormatting = formatting),
                )
            },
            step = totalSteps(),
            totalSteps = totalSteps(),
            initialSummary = draftState.document.summary,
            initialFormatting = draftState.document.summaryFormatting,
            onBack = { navController.popBackStack() },
            onContinue = { summary, formatting ->
                draftState.updateSummary(summary, formatting)
                navController.navigate(ResumeRoute.Template)
            },
        )
        }

        composable<ResumeRoute.Template> {
        KeepResumeDraft(draftState, ResumeDraftSection.Template)
        TemplateSelectionScreen(
            onDraftChange = { template ->
                draftState.updatePending(ResumeDraftSection.Template, draftState.document.copy(templateName = template))
            },
            initialTemplate = draftState.document.templateName,
            onBack = { navController.popBackStack() },
            onContinue = { template ->
                draftState.updateTemplate(template)
                navController.navigate(ResumeRoute.Preview)
            },
        )
        }

        composable<ResumeRoute.Preview> {
        ResumePreviewScreen(
            resume = draftState.document,
            onBack = { navController.popBackStack() },
            onSave = {
                draftState.save()
                onResumeSaved()
                navController.popBackStack(ResumeRoute.ResumeList, inclusive = false)
            },
            onExported = {
                draftState.save()
                onResumeSaved()
            },
        )
        }
    }
}

@Composable
private fun KeepResumeDraft(state: ResumeDraftState, section: ResumeDraftSection) {
    val session = state.sessionId
    DisposableEffect(state, section, session) {
        onDispose {
            state.commitPending(section, session)
            state.flushAutoSave()
        }
    }
}

private fun <Entry> List<Entry>.withDraft(entry: Entry, idOf: (Entry) -> String): List<Entry> =
    if (any { idOf(it) == idOf(entry) }) {
        map { if (idOf(it) == idOf(entry)) entry else it }
    } else {
        listOf(entry) + this
    }
