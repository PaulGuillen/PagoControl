package com.devpaul.features.settings.ui

import com.devpaul.core.domain.rating.PremiumFeedbackRepository
import org.koin.android.annotation.KoinViewModel

@KoinViewModel(binds = [PremiumFeedbackViewModel::class])
class PremiumFeedbackViewModel(repository: PremiumFeedbackRepository) : RatingViewModel(repository)
