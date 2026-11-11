package com.devpaul.features.templates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.devpaul.features.templates.R
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateDefinition

@Composable
fun ResumeTemplateDefinition.localizedName(): String = when (id) {
    ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> stringResource(R.string.ui_prague_4a9ba9)
    ResumeTemplateCatalog.TOKYO_COMPACT_ID -> stringResource(R.string.ui_tokyo_08bc81)
    else -> displayName
}

@Composable
fun ResumeTemplateDefinition.localizedDescription(): String = stringResource(when (id) {
    ResumeTemplateCatalog.BUDAPEST_ID -> R.string.ui_professional_two_column_design_with_uppercase_headings_42bfe8
    ResumeTemplateCatalog.RIGA_ID -> R.string.ui_black_and_white_editorial_style_with_columns_and_fine_divide_a05aa9
    ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> R.string.ui_clean_design_with_a_split_header_and_structured_sections_2fd903
    else -> R.string.ui_compact_single_column_layout_with_light_blue_highlighted_sec_f13561
})
