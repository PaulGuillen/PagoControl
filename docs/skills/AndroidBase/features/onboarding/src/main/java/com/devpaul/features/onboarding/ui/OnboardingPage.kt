package com.devpaul.features.onboarding.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.devpaul.features.onboarding.R

internal data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val firstFeatureIcon: ImageVector,
    @StringRes val firstFeature: Int,
    val secondFeatureIcon: ImageVector,
    @StringRes val secondFeature: Int,
    val badgeIcon: ImageVector,
    @StringRes val badge: Int,
)

internal val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.Description,
        title = R.string.ui_choose_a_professional_template_c2bf47,
        description = R.string.ui_enter_your_details_choose_a_template_and_download_your_resum_4c7b40,
        firstFeatureIcon = Icons.Outlined.AutoAwesome,
        firstFeature = R.string.ui_customizable_designs_7da30c,
        secondFeatureIcon = Icons.Outlined.PictureAsPdf,
        secondFeature = R.string.ui_high_quality_pdf_95ca8f,
        badgeIcon = Icons.Outlined.Star,
        badge = R.string.ui_recommended_fef9d9,
    ),
    OnboardingPage(
        icon = Icons.Outlined.Edit,
        title = R.string.ui_complete_your_profile_easily_31abf9,
        description = R.string.ui_organize_your_experience_education_and_skills_step_by_step_9162c7,
        firstFeatureIcon = Icons.Outlined.Edit,
        firstFeature = R.string.ui_easy_editing_917c49,
        secondFeatureIcon = Icons.Outlined.VerifiedUser,
        secondFeature = R.string.ui_protected_data_35294e,
        badgeIcon = Icons.Outlined.CheckCircle,
        badge = R.string.onboarding_step_by_step,
    ),
    OnboardingPage(
        icon = Icons.Outlined.Download,
        title = R.string.ui_your_next_job_starts_here_46ad25,
        description = R.string.ui_keep_your_resume_updated_and_ready_to_share_whenever_you_nee_f2a3f6,
        firstFeatureIcon = Icons.Outlined.AutoAwesome,
        firstFeature = R.string.ui_professional_result_b29d95,
        secondFeatureIcon = Icons.Outlined.Download,
        secondFeature = R.string.ui_instant_download_ba9628,
        badgeIcon = Icons.Outlined.Download,
        badge = R.string.onboarding_ready_to_share,
    ),
)
