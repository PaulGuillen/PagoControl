package com.devpaul.pagocontrol.features.home.data.mapper

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FirestorePaymentCodecTest {
    private val row = NotificationEntity(
        id = "payment/unsafe-original-id", packageName = "com.google.android.gm", source = "Gmail",
        title = "Bank", text = "Receipt", postedAt = 123, suggestedAmountCents = 100,
        amountCents = -100, status = "confirmed", ownerId = "alice", pendingWriteId = "write-1",
    )

    @Test
    fun roundTripUsesSafeDeterministicDocumentIdAndRemoteOwner() {
        val data = FirestorePaymentCodec.encode(row = row, revision = 42)
        val result = FirestorePaymentCodec.decode(
            ownerId = "alice",
            documentId = FirestorePaymentCodec.documentId(row.id),
            data = data
        )
        assertEquals(row.copy(remoteRevision = 42, pendingWriteId = null), result)
        assertEquals(64, FirestorePaymentCodec.documentId(row.id).length)
    }

    @Test
    fun rejectsCorruptIdentitySchemaAndAmounts() {
        val id = FirestorePaymentCodec.documentId(row.id)
        val data = FirestorePaymentCodec.encode(row = row, revision = 1)
        assertThrows(IllegalArgumentException::class.java) {
            FirestorePaymentCodec.decode(
                ownerId = "alice",
                documentId = "bad",
                data = data
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            FirestorePaymentCodec.decode(
                ownerId = "alice",
                documentId = id,
                data = data + ("schemaVersion" to 2L)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            FirestorePaymentCodec.decode(
                ownerId = "alice",
                documentId = id,
                data = data + ("amountCents" to 1.2)
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            FirestorePaymentCodec.encode(
                row = row.copy(
                    text = "x".repeat(4001)
                ), revision = 1
            )
        }
    }
}
