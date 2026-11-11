package com.devpaul.features.resume.ui.model

import androidx.annotation.StringRes
import com.devpaul.features.resume.R

internal enum class CvFilter(@StringRes val label: Int) {
    All(R.string.ui_all_a4114a),
    Recent(R.string.ui_recent_7ed1ec),
    Template(R.string.ui_template_00e6b7),
}
