package com.devpaul.pagocontrol.features.home.data.datasource.remote

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.remote.model.PaymentCloudBatch
import com.devpaul.pagocontrol.features.home.data.mapper.FirestorePaymentCodec
import com.devpaul.pagocontrol.features.home.data.sync.PaymentConflictPolicy
import com.devpaul.pagocontrol.session.AuthRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

internal class FirebasePaymentCloudStore(
    private val firestore: FirebaseFirestore,
    private val auth: AuthRepository,
) : PaymentCloudStore {
    private fun payments(ownerId: String) =
        firestore.collection("users").document(ownerId).collection("payments")

    private fun metadata(ownerId: String) =
        firestore.collection("users").document(ownerId).collection("paymentSync").document("state")

    private fun checkOwner(ownerId: String) =
        check(auth.session.value?.id == ownerId) { "Account changed" }

    override suspend fun head(ownerId: String): Long {
        checkOwner(ownerId)
        return metadata(ownerId).get(Source.SERVER).await().getLong("revision") ?: 0L
    }

    override suspend fun changes(
        ownerId: String,
        after: Long,
        through: Long,
        limit: Int
    ): List<NotificationEntity> {
        checkOwner(ownerId)
        return payments(ownerId).whereGreaterThan("revision", after)
            .whereLessThanOrEqualTo("revision", through)
            .orderBy("revision").limit(limit.toLong()).get(Source.SERVER)
            .await().documents.map { document ->
                FirestorePaymentCodec.decode(
                    ownerId = ownerId,
                    documentId = document.id,
                    data = requireNotNull(document.data)
                )
            }
    }

    override suspend fun save(ownerId: String, rows: List<NotificationEntity>): PaymentCloudBatch {
        require(rows.size in 1..20 && rows.all { it.ownerId == ownerId && it.pendingWriteId != null })
        rows.forEach { FirestorePaymentCodec.encode(row = it, revision = 1) }
        checkOwner(ownerId)
        return firestore.runTransaction { transaction ->
            checkOwner(ownerId)
            val metadataRef = metadata(ownerId)
            val base = transaction.get(metadataRef).getLong("revision") ?: 0L
            val references =
                rows.map { payments(ownerId).document(FirestorePaymentCodec.documentId(it.id)) }
            // Firestore transactions require every read to precede every write.
            val existing = references.map { transaction.get(it) }
            var revision = base
            val result = rows.mapIndexed { index, row ->
                val snapshot = existing[index]
                val remote = snapshot.data?.let {
                    FirestorePaymentCodec.decode(
                        ownerId = ownerId,
                        documentId = snapshot.id,
                        data = it
                    )
                }
                when {
                    snapshot.getString("writeId") == row.pendingWriteId -> requireNotNull(remote)
                    remote != null && PaymentConflictPolicy.keepRemote(
                        local = row,
                        remote = remote
                    ) -> remote

                    else -> {
                        revision++
                        transaction.set(
                            references[index],
                            FirestorePaymentCodec.encode(
                                row = row,
                                revision = revision
                            ) + ("updatedAt" to FieldValue.serverTimestamp())
                        )
                        row.copy(pendingWriteId = null, remoteRevision = revision)
                    }
                }
            }
            if (revision > base) {
                transaction.set(
                    metadataRef,
                    mapOf(
                        "schemaVersion" to 1L,
                        "revision" to revision,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            PaymentCloudBatch(baseRevision = base, revision = revision, rows = result)
        }.await()
    }
}
