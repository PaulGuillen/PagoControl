package com.devpaul.pagocontrol.features.home.data.sync

import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentConflictPolicyTest {
    private val local = NotificationEntity(
        id = "p", packageName = "mail", source = "Mail", title = "Receipt", text = "Receipt",
        postedAt = 1, suggestedAmountCents = 100, amountCents = 100, status = "confirmed",
    )

    @Test
    fun discardIsTerminalAcrossDisconnectedDevices() {
        assertTrue(
            PaymentConflictPolicy.keepRemote(
                local = local,
                remote = local.copy(status = "discarded", remoteRevision = 10)
            )
        )
        assertFalse(
            PaymentConflictPolicy.keepRemote(
                local = local.copy(status = "discarded"),
                remote = local.copy(remoteRevision = 10)
            )
        )
    }

    @Test
    fun confirmationSurvivesLostAcknowledgementOfPendingUpload() {
        assertFalse(
            PaymentConflictPolicy.keepRemote(
                local = local,
                remote = local.copy(status = "pending", remoteRevision = 10)
            )
        )
    }

    @Test
    fun staleWritesCannotOverwriteNewerRemoteConfirmedAmounts() {
        assertTrue(
            PaymentConflictPolicy.keepRemote(
                local = local,
                remote = local.copy(amountCents = -200, remoteRevision = 10)
            )
        )
        assertFalse(
            PaymentConflictPolicy.keepRemote(
                local = local.copy(remoteRevision = 10),
                remote = local.copy(remoteRevision = 10)
            )
        )
    }
}
