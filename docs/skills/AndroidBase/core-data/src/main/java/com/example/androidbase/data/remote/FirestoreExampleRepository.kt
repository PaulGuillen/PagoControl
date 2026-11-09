package com.example.androidbase.data.remote

import com.example.androidbase.domain.AppResult
import com.example.androidbase.domain.ExampleItem
import com.example.androidbase.domain.ExampleRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreExampleRepository(private val firestore: FirebaseFirestore) : ExampleRepository {
    override fun observe(ownerId: String): Flow<List<ExampleItem>> = callbackFlow {
        val registration = collection(ownerId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                trySend(snapshot.documents.map { document ->
                    ExampleItem(document.id, document.getString("title").orEmpty())
                }.sortedBy { it.title })
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun add(ownerId: String, title: String): AppResult<Unit> = dataCall {
        collection(ownerId).document().set(mapOf("title" to title)).await()
        Unit
    }

    private fun collection(ownerId: String) = firestore.collection("users").document(ownerId).collection("items")
}
