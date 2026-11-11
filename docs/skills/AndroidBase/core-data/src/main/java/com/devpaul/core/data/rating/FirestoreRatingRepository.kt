package com.devpaul.core.data.rating

import com.devpaul.core.domain.rating.MAX_RATING_COMMENT_LENGTH
import com.devpaul.core.domain.rating.RatingRepository
import com.devpaul.core.domain.rating.RatingResult
import com.devpaul.core.domain.rating.isRatingOnCooldown
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single([RatingRepository::class])
class FirestoreRatingRepository(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
) : RatingRepository {
    private val store = FirestoreFeedbackStore(auth, firestore, "app_ratings")

    override suspend fun availability(): RatingResult = store.availability()

    override suspend fun submit(stars: Int, comment: String): RatingResult = store.submit(stars, comment)
}

internal class FirestoreFeedbackStore(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val collection: String,
) : RatingRepository {

    override suspend fun availability(): RatingResult {
        val uid = checkNotNull(auth.currentUser) { "No authenticated user" }.uid
        val latest = firestore.collection(collection).document(uid).get(Source.SERVER).await()
        return if (isRatingOnCooldown(latest.getTimestamp("submittedAt")?.toDate()?.time, System.currentTimeMillis())) {
            RatingResult.DailyLimitReached
        } else {
            RatingResult.Available
        }
    }

    override suspend fun submit(stars: Int, comment: String): RatingResult {
        require(stars in 1..5)
        val normalizedComment = comment.trim()
        require(normalizedComment.length <= MAX_RATING_COMMENT_LENGTH)
        val uid = checkNotNull(auth.currentUser) { "No authenticated user" }.uid
        val latestRef = firestore.collection(collection).document(uid)
        val submissionRef = latestRef.collection("submissions").document()

        return try {
            firestore.runTransaction { transaction ->
                val latest = transaction.get(latestRef)
                if (isRatingOnCooldown(latest.getTimestamp("submittedAt")?.toDate()?.time, System.currentTimeMillis())) {
                    RatingResult.DailyLimitReached
                } else {
                    val data = mapOf(
                        "uid" to uid,
                        "stars" to stars,
                        "comment" to normalizedComment,
                        "submittedAt" to FieldValue.serverTimestamp(),
                        "submissionId" to submissionRef.id,
                    )
                    transaction.set(latestRef, data)
                    transaction.set(submissionRef, data)
                    RatingResult.Submitted
                }
            }.await()
        } catch (error: FirebaseFirestoreException) {
            if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED &&
                availability() == RatingResult.DailyLimitReached
            ) {
                RatingResult.DailyLimitReached
            } else {
                throw error
            }
        }
    }
}
