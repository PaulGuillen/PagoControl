package com.devpaul.core.data.rating

import com.devpaul.core.domain.rating.PremiumFeedbackRepository
import com.devpaul.core.domain.rating.RatingResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.annotation.Single

@Single([PremiumFeedbackRepository::class])
class FirestorePremiumFeedbackRepository(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
) : PremiumFeedbackRepository {
    private val store = FirestoreFeedbackStore(auth, firestore, "premium_plan_feedback")

    override suspend fun availability(): RatingResult = store.availability()

    override suspend fun submit(stars: Int, comment: String): RatingResult = store.submit(stars, comment)
}
