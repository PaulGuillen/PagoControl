package com.devpaul.features.resume.importing

import androidx.annotation.StringRes
import com.devpaul.features.resume.R

internal enum class ResumeImportStage(
    @StringRes val title: Int,
    @StringRes val detail: Int,
) {
    Preparing(
        title = R.string.ui_preparing_your_resume_01c08d,
        detail = R.string.ui_opening_the_document_securely_fa62fa,
    ),
    Reading(
        title = R.string.ui_analyzing_your_resume_8996d7,
        detail = R.string.ui_reading_the_pdf_content_0e0b88,
    ),
    Extracting(
        title = R.string.ui_identifying_your_information_066784,
        detail = R.string.ui_extracting_experience_education_and_skills_8001be,
    ),
    Enhancing(
        title = R.string.ui_reviewing_with_artificial_intelligence_60b58d,
        detail = R.string.ui_validating_sections_and_correcting_the_document_reading_8f81bc,
    ),
    Building(
        title = R.string.ui_building_your_resume_8dbe55,
        detail = R.string.ui_organizing_the_data_in_your_forms_b94983,
    ),
    Complete(
        title = R.string.ui_your_resume_is_ready_0d5cd6,
        detail = R.string.ui_review_and_improve_each_section_before_saving_it_764e99,
    ),
}
