package com.devpaul.pagocontrol.features.auth.data.datasource.remote

import com.devpaul.pagocontrol.features.auth.data.datasource.AuthRemoteDataSource
import com.devpaul.pagocontrol.session.UserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Single

@Single(binds = [AuthRemoteDataSource::class])
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRemoteDataSource {
    private val mutableSession = MutableStateFlow(auth.currentUser?.toSession())
    override val session = mutableSession.asStateFlow()
    private val mutex = Mutex()
    @Volatile private var changingUser = false

    init {
        auth.addAuthStateListener { instance ->
            if (!changingUser) mutableSession.value = instance.currentUser?.toSession()
        }
    }

    override suspend fun signIn(email: String, password: String) = authenticate {
        requireNotNull(auth.signInWithEmailAndPassword(email, password).await().user)
    }

    override suspend fun register(name: String, email: String, password: String) = authenticate {
        val user = requireNotNull(auth.createUserWithEmailAndPassword(email, password).await().user)
        try {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        } catch (error: Exception) {
            throw ProfileSetupException(error)
        }
        user
    }

    private suspend fun authenticate(operation: suspend () -> FirebaseUser) = withContext(NonCancellable) {
        mutex.withLock {
            changingUser = true
            try {
                val user = operation()
                val name = try {
                    withTimeout(15_000) { ensureProfile(user) }
                } catch (error: Exception) {
                    throw ProfileSetupException(error)
                }
                mutableSession.value = user.toSession().copy(name = name)
            } catch (error: Exception) {
                auth.signOut()
                mutableSession.value = null
                throw error
            } finally {
                changingUser = false
            }
        }
    }

    private suspend fun ensureProfile(user: FirebaseUser): String {
        val document = firestore.collection("users").document(user.uid)
        val existing = document.get().await()
        if (existing.exists()) return existing.getString("name")?.takeIf { it.isNotBlank() } ?: user.toSession().name
        val name = user.toSession().name
        document.set(mapOf(
            "userID" to user.uid,
            "name" to name,
            "email" to user.email.orEmpty(),
            "createdAt" to FieldValue.serverTimestamp(),
        )).await()
        return name
    }

    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun refreshProfile(): UserSession? {
        val user = auth.currentUser ?: return null
        val name = withTimeout(15_000) {
            firestore.collection("users").document(user.uid).get().await().getString("name")
        }
        val refreshed = user.toSession().copy(name = name?.takeIf { it.isNotBlank() } ?: user.toSession().name)
        if (auth.currentUser?.uid == user.uid && !changingUser) mutableSession.value = refreshed
        return refreshed.takeIf { auth.currentUser?.uid == user.uid }
    }

    override suspend fun updateName(name: String) = withContext(NonCancellable) {
        mutex.withLock {
            val user = checkNotNull(auth.currentUser)
            withTimeout(15_000) {
                firestore.collection("users").document(user.uid)
                    .set(mapOf("name" to name), SetOptions.merge()).await()
            }
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
            mutableSession.value = user.toSession()
        }
    }

    override suspend fun signOut() = withContext(NonCancellable) {
        mutex.withLock {
            auth.signOut()
            mutableSession.value = null
        }
    }

    private fun FirebaseUser.toSession() = UserSession(
        id = uid,
        name = displayName?.trim()?.takeIf { it.length in 2..80 }
            ?: email?.substringBefore('@')?.take(80)?.takeIf { it.length >= 2 }
            ?: "Usuario",
        email = email.orEmpty(),
    )
}
