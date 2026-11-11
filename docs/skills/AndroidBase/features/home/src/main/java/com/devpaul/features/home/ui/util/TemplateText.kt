package com.devpaul.features.home.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.devpaul.features.home.R
import com.devpaul.features.templates.ResumeTemplateCatalog
import com.devpaul.features.templates.ResumeTemplateDefinition

@Composable
internal fun ResumeTemplateDefinition.localizedName(): String = when (id) {
    ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> stringResource(R.string.template_prague_name)
    ResumeTemplateCatalog.TOKYO_COMPACT_ID -> stringResource(R.string.template_tokyo_name)
    else -> displayName
}

@Composable
internal fun ResumeTemplateDefinition.localizedDescription(): String = stringResource(
    when (id) {
        ResumeTemplateCatalog.BUDAPEST_ID -> R.string.template_budapest_description
        ResumeTemplateCatalog.RIGA_ID -> R.string.template_riga_description
        ResumeTemplateCatalog.PRAGUE_EDITORIAL_ID -> R.string.template_prague_description
        ResumeTemplateCatalog.TOKYO_COMPACT_ID -> R.string.template_tokyo_description
        else -> R.string.template_budapest_description
    },
)
