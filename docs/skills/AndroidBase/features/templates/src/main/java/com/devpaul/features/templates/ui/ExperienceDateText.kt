package com.devpaul.features.templates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.devpaul.core.domain.resume.ResumeExperience
import com.devpaul.features.templates.R

@Composable
fun ResumeExperience.localizedEndYear(): String =
    if (isOngoing) stringResource(R.string.experience_present) else endYear
