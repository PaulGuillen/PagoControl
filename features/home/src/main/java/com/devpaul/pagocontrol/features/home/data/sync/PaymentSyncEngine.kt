package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.remote.PaymentCloudStore

/** One serialized sync: the UI never waits for the network and never reads Firestore. */
internal class PaymentSyncEngine(
    private val local: PaymentSyncLocalStore,
    private val cloud: PaymentCloudStore,
    private val isCurrentOwner: (String) -> Boolean,
) {
    suspend fun sync(ownerId: String, checkRemote: Boolean) {
        checkOwner(ownerId)
        local.prepare(ownerId)
        if (checkRemote || !local.checkpoint(ownerId).initialDownloadComplete) {
            pull(ownerId = ownerId, through = cloud.head(ownerId))
            checkOwner(ownerId)
            local.complete(ownerId)
        }
        while (true) {
            checkOwner(ownerId)
            val pending = local.pending(ownerId)
            if (pending.isEmpty()) return
            val result = cloud.save(ownerId = ownerId, rows = pending)
            checkOwner(ownerId)
            require(result.rows.size == pending.size)
            pending.zip(result.rows)
                .forEach { (sent, remote) -> local.acknowledge(sent = sent, remote = remote) }
            val checkpoint = local.checkpoint(ownerId)
            if (checkpoint.revision == result.baseRevision) {
                // All intervening revisions were ours: acknowledge without downloading our writes again.
                local.applyPage(ownerId = ownerId, rows = emptyList(), revision = result.revision)
            } else {
                pull(ownerId = ownerId, through = result.revision)
            }
        }
    }

    private suspend fun pull(ownerId: String, through: Long) {
        while (true) {
            checkOwner(ownerId)
            val after = local.checkpoint(ownerId).revision
            if (after >= through) return
            val page =
                cloud.changes(ownerId = ownerId, after = after, through = through, limit = 100)
            checkOwner(ownerId)
            require(page.size <= 100 && page.all { it.ownerId == ownerId && it.remoteRevision > after && it.remoteRevision <= through })
            require(
                page.zipWithNext()
                    .all { (first, second) -> first.remoteRevision < second.remoteRevision })
            // Revisions overwritten while paginating may be absent; a later sync fetches their newer revision.
            val revision = if (page.size < 100) through else page.last().remoteRevision
            local.applyPage(ownerId = ownerId, rows = page, revision = revision)
        }
    }

    private fun checkOwner(ownerId: String) = check(isCurrentOwner(ownerId)) { "Account changed" }
}
