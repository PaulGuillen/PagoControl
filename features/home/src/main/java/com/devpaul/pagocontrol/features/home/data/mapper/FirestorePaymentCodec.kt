package com.devpaul.pagocontrol.features.home.data.mapper

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import java.security.MessageDigest

internal object FirestorePaymentCodec {
    fun documentId(id: String): String = MessageDigest.getInstance("SHA-256")
        .digest(id.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    fun encode(row: NotificationEntity, revision: Long): Map<String, Any?> {
        validate(row)
        require(revision > 0 && !row.pendingWriteId.isNullOrBlank())
        return mapOf(
            "schemaVersion" to 1L,
            "id" to row.id,
            "packageName" to row.packageName,
            "source" to row.source,
            "title" to row.title,
            "text" to row.text,
            "postedAt" to row.postedAt,
            "suggestedAmountCents" to row.suggestedAmountCents,
            "amountCents" to row.amountCents,
            "status" to row.status,
            "revision" to revision,
            "writeId" to row.pendingWriteId,
        )
    }

    fun decode(ownerId: String, documentId: String, data: Map<String, Any?>): NotificationEntity {
        require(data["schemaVersion"] == 1L) { "Unsupported payment schema" }
        fun string(key: String) = requireNotNull(data[key] as? String) { "Invalid payment field" }
        fun amount(key: String): Long? {
            val value = data[key] ?: return null
            return requireNotNull(value as? Long) { "Invalid amount" }
        }

        val row = NotificationEntity(
            id = string("id"),
            packageName = string("packageName"),
            source = string("source"),
            title = string("title"),
            text = string("text"),
            postedAt = requireNotNull(data["postedAt"] as? Long),
            suggestedAmountCents = amount("suggestedAmountCents"),
            amountCents = amount("amountCents"),
            status = string("status"),
            ownerId = ownerId,
            pendingWriteId = null,
            remoteRevision = requireNotNull(data["revision"] as? Long),
        )
        validate(row)
        require(row.remoteRevision > 0 && documentId(row.id) == documentId)
        require(string("writeId").length in 1..100)
        return row
    }

    private fun validate(row: NotificationEntity) {
        require(row.id.length in 1..200 && row.packageName.length in 1..200)
        require(row.source.length <= 200 && row.title.length <= 200 && row.text.length <= 4000)
        require(row.postedAt >= 0)
        require(row.status in setOf("pending", "automatic", "confirmed", "discarded", "transfer"))
        require(row.amountCents == null || row.amountCents in -99_999_999_999L..99_999_999_999L)
        require(row.suggestedAmountCents == null || row.suggestedAmountCents in -99_999_999_999L..99_999_999_999L)
        if (row.status == "confirmed" || row.status == "automatic" || row.status == "transfer") require(
            row.amountCents != null && row.amountCents != 0L
        )
    }
}
