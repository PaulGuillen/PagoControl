package com.devpaul.features.resume.ui.links

import androidx.annotation.StringRes
import com.devpaul.features.resume.R

internal enum class LinkPreset(@StringRes val label: Int, val url: String) {
    LinkedIn(R.string.link_preset_linkedin, "https://linkedin.com/in/"),
    GitHub(R.string.link_preset_github, "https://github.com/"),
    Portfolio(R.string.ui_portfolio_8a4833, "https://"),
}
