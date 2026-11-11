package com.devpaul.features.home.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.devpaul.features.home.R
import com.devpaul.features.home.ui.model.ResumeProgress
import com.devpaul.features.home.ui.model.ResumeProgressSection

@Composable
internal fun ResumeProgress.localizedMessage(): String {
    val section = missingSection ?: return stringResource(R.string.home_profile_complete)
    val sectionName = stringResource(
        when (section) {
            ResumeProgressSection.Contact -> R.string.home_section_contact
            ResumeProgressSection.Experience -> R.string.home_section_experience
            ResumeProgressSection.Education -> R.string.home_section_education
            ResumeProgressSection.Skills -> R.string.home_section_skills
            ResumeProgressSection.Summary -> R.string.home_section_summary
        },
    )
    return stringResource(R.string.home_complete_section, sectionName, nextPercent)
}
