package com.devpaul.features.resume.importing

import androidx.annotation.StringRes

internal class ResumeImportException(@StringRes val messageRes: Int) : IllegalArgumentException("Resume import failed")
