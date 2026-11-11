package com.devpaul.core.domain.rating

const val MAX_RATING_COMMENT_LENGTH = 500
const val RATING_INTERVAL_MILLIS = 24 * 60 * 60 * 1000L

enum class RatingResult {
    Available,
    Submitted,
    DailyLimitReached,
}

interface RatingRepository {
    suspend fun availability(): RatingResult
    suspend fun submit(stars: Int, comment: String): RatingResult
}

fun isRatingOnCooldown(lastSubmittedAtMillis: Long?, nowMillis: Long): Boolean =
    lastSubmittedAtMillis != null && nowMillis - lastSubmittedAtMillis < RATING_INTERVAL_MILLIS
