package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity

internal object PaymentConflictPolicy {
    fun keepRemote(local: NotificationEntity, remote: NotificationEntity): Boolean {
        if (remote.status == "discarded") return true
        if (local.status == "discarded") return false
        // A lost acknowledgement of the pending receipt must not cancel a later local confirmation.
        if (remote.status == "pending" && local.status in setOf(
                "confirmed",
                "automatic"
            )
        ) return false
        return remote.remoteRevision > local.remoteRevision
    }
}
