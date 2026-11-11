package com.devpaul.core.data.resume

import com.devpaul.core.domain.resume.ResumeDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

internal class FirebaseResumeCloudStore(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ResumeCloudStore {
    override val currentUid: String?
        get() = auth.currentUser?.uid

    override val userIds: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun observe(uid: String): Flow<ResumeCloudSnapshot> = callbackFlow {
        val listener = collection(uid).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                try {
                    val resumes = snapshot.documents.map { FirestoreResumeCodec.decode(it.id, it.data) }
                    trySend(ResumeCloudSnapshot(resumes, snapshot.metadata.hasPendingWrites(), snapshot.metadata.isFromCache))
                } catch (failure: Exception) {
                    close(failure)
                }
            }
        }
        awaitClose { listener.remove() }
    }

    override fun save(uid: String, resume: ResumeDocument, onFailure: (Exception) -> Unit) {
        val data = FirestoreResumeCodec.encode(resume) + ("syncedAt" to FieldValue.serverTimestamp())
        collection(uid).document(resume.id).set(data).addOnFailureListener(onFailure)
    }

    override fun delete(uid: String, resumeId: String, onFailure: (Exception) -> Unit) {
        FirestoreResumeCodec.requireValidId(resumeId)
        collection(uid).document(resumeId).delete().addOnFailureListener(onFailure)
    }

    override suspend fun importIfAbsent(uid: String, resume: ResumeDocument) {
        val data = FirestoreResumeCodec.encode(resume) + ("syncedAt" to FieldValue.serverTimestamp())
        val reference = collection(uid).document(resume.id)
        firestore.runTransaction { transaction ->
            check(currentUid == uid) { "Account changed during migration" }
            val existing = transaction.get(reference)
            if (!existing.exists()) {
                transaction.set(reference, data)
            } else {
                FirestoreResumeCodec.decode(existing.id, existing.data)
            }
            Unit
        }.await()
    }

    private fun collection(uid: String) = firestore.collection("users").document(uid).collection("resumes")
}
