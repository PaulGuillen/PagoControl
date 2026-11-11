package com.devpaul.features.auth.data.datasource.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Factory

@Factory
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {
    fun currentUser(): FirebaseUser? = auth.currentUser

    suspend fun currentUserName(userID: String): String =
        firestore.collection(USERS_COLLECTION)
            .document(userID)
            .get()
            .await()
            .getString(NAME_FIELD)
            .orEmpty()

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return requireNotNull(result.user) { "Firebase user is null" }
    }

    suspend fun register(name: String, email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = requireNotNull(result.user) { "Firebase user is null" }

        try {
            val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profile).await()

            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(
                    mapOf(
                        "userID" to user.uid,
                        "name" to name,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp(),
                    ),
                )
                .await()
            return user
        } catch (error: Exception) {
            runCatching { user.delete().await() }
            throw error
        }
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    suspend fun updateCurrentUserName(name: String): FirebaseUser {
        val user = checkNotNull(auth.currentUser) { "No authenticated user" }
        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profile).await()
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(mapOf(NAME_FIELD to name), SetOptions.merge())
            .await()
        return user
    }

    fun signOut() = auth.signOut()

    private companion object {
        const val USERS_COLLECTION = "users"
        const val NAME_FIELD = "name"
    }
}
