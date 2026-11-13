package com.devpaul.pagocontrol

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.data.notifications.PaymentDatabase
import com.devpaul.pagocontrol.data.notifications.NotificationEntity
import com.devpaul.pagocontrol.data.notifications.RoomPaymentRepository
import com.devpaul.pagocontrol.domain.PaymentNotification
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentNotificationsTest {
    @Test fun keepsUnknownExistingGmailPendingUntilConfirmed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository = RoomPaymentRepository(database)
            repeat(21) { index ->
                database.notifications().insert(NotificationEntity(
                    id = "old-$index", packageName = "com.google.android.gm", source = "Gmail",
                    title = "Banco", text = "Transferencia recibida S/ 10.00",
                    postedAt = System.currentTimeMillis(), suggestedAmountCents = 1000,
                ))
            }
            assertTrue(repository.observeGmailDashboard().first().dashboard.movements.isEmpty())
            assertEquals(21, repository.observeGmailPending().first().size)
            repeat(21) { repository.confirm("old-$it", 1000) }
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(21, dashboard.movements.size)
            assertEquals(21000L, dashboard.balanceCents)
            assertTrue(dashboard.movements.none { it.automatic })
            assertTrue(repository.observeGmailPending().first().isEmpty())
            repository.discard("old-0")
            val updated = repository.observeGmailDashboard().first().dashboard
            assertEquals(20000L, updated.incomeCents)
            assertEquals(20, updated.incomeCount)
            assertEquals(20000L, updated.weeklyIncomeFlow.sum())
            assertEquals(20000L, updated.monthlyIncomeFlow.sum())
        } finally {
            database.close()
        }
    }

    @Test fun bankAndMerchantReceiptsNeverAutomaticallyCountTwice() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository = RoomPaymentRepository(database)
            repository.setGmailEnabled(true)
            val bank = PaymentNotification("bank", "com.google.android.gm", "Gmail", "BCP", "Compra S/ 25.00", System.currentTimeMillis(), null)
            val merchant = bank.copy(id = "merchant", title = "Steam", text = "Receipt S/ 25.00")
            repository.capture(merchant)
            assertEquals(1, repository.observeGmailPending().first().size)
            repository.capture(bank)
            assertEquals(-2500L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            assertTrue(repository.observeGmailPending().first().single().possibleDuplicate)
            repository.discard(merchant.id)
            repository.capture(bank.copy(id = "bank-repeat"))
            assertTrue(repository.observeGmailPending().first().single().possibleDuplicate)
            assertEquals(-2500L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            // A manually confirmed merchant receipt arriving before the bank also prevents automatic double counting.
            repository.capture(merchant.copy(id = "merchant-2", text = "Receipt S/ 30.00"))
            repository.confirm("merchant-2", -3000)
            repository.capture(bank.copy(id = "bank-2", text = "Compra S/ 30.00"))
            assertEquals(-5500L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            assertEquals(2, repository.observeGmailPending().first().size)
        } finally { database.close() }
    }

    @Test fun recordsBcpConsumptionAsAutomaticExpenseWhenGmailIsEnabled() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository = RoomPaymentRepository(database)
            val notification = PaymentNotification(
                id = "automatic-bcp", packageName = "com.google.android.gm", source = "Gmail",
                title = "BCP Notificaciones",
                text = "Realizaste un consumo de S/ 25.78 con tu Tarjeta de Débito BCP en WL *STEAM PURCHASE.\nPor tu seguridad, te enviamos los datos de tu operación.\nTotal del consumo\nS/ 25.78",
                postedAt = System.currentTimeMillis(), suggestedAmountCents = null,
            )
            repository.capture(notification)
            assertTrue(repository.observeGmailPending().first().isEmpty())
            assertEquals(0L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            repository.setGmailEnabled(true)
            repository.capture(notification)
            repository.capture(notification)
            repository.confirm(notification.id, 9999)
            assertTrue(repository.observeGmailPending().first().isEmpty())
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(-2578L, dashboard.balanceCents)
            assertEquals(2578L, dashboard.expenseCents)
            assertEquals(1, dashboard.expenseCount)
            assertTrue(dashboard.movements.single().automatic)
            assertEquals("BCP - WL *STEAM PURCHASE", dashboard.movements.single().title)
            assertEquals(0L, repository.loadDashboard().dashboard.balanceCents)
            repository.capture(notification.copy(id = "another", text = "Compra realizada por S/ 10.00"))
            assertTrue(repository.observeGmailPending().first().isEmpty())
            assertEquals(-3578L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            repository.discard("another")
            repository.capture(notification.copy(id = "another", text = "Compra realizada por S/ 10.00"))
            assertEquals(-2578L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            repository.discard(notification.id)
            val deleted = repository.observeGmailDashboard().first().dashboard
            assertEquals(0L, deleted.balanceCents)
            assertEquals(0, deleted.expenseCount)
            assertTrue(deleted.movements.isEmpty())
            assertEquals(0L, deleted.weeklyFlow.sum())
            assertEquals(0L, deleted.monthlyFlow.sum())
        } finally {
            database.close()
        }
    }

    @Test fun persistsGmailOptInAndSeparatesItsPaymentsFromWallets() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "gmail-test-${System.nanoTime()}.db"
        var database = Room.databaseBuilder(context = context, klass = PaymentDatabase::class.java, name = databaseName).build()
        try {
            var repository = RoomPaymentRepository(database)
            val notification = PaymentNotification(
                id = "gmail-transfer", packageName = "com.google.android.gm", source = "Gmail",
                title = "BBVA", text = "Transferencia recibida S/ 100.00", postedAt = System.currentTimeMillis(), suggestedAmountCents = 10000,
            )
            repository.capture(notification)
            assertTrue(repository.observeGmailPending().first().isEmpty())
            repository.setGmailEnabled(true)
            repository.capture(notification)
            repository.capture(notification)
            repository.capture(notification.copy(id = "unrelated", text = "Reunión del equipo mañana"))
            assertEquals(0, repository.observeGmailPending().first().size)
            assertTrue(repository.observePending().first().isEmpty())
            assertTrue(repository.observeSources().first().isEmpty())
            repository.confirm(id = notification.id, amountCents = 10000)
            repository.capture(notification.copy(id = "gmail-expense", text = "Pago realizado S/ 25.00"))
            repository.confirm(id = "gmail-expense", amountCents = -2500)
            assertEquals(0L, repository.loadDashboard().dashboard.balanceCents)
            database.close()
            database = Room.databaseBuilder(context = context, klass = PaymentDatabase::class.java, name = databaseName).build()
            repository = RoomPaymentRepository(database)
            assertTrue(repository.observeGmailEnabled().first())
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(7500L, dashboard.balanceCents)
            assertEquals(10000L, dashboard.incomeCents)
            assertEquals(2500L, dashboard.expenseCents)
            repository.setGmailEnabled(false)
            repository.capture(notification.copy(id = "disabled"))
            assertTrue(repository.observeGmailPending().first().isEmpty())
            assertEquals(0L, repository.loadDashboard().dashboard.balanceCents)
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test fun recordsRecognizedYapeIncomeAutomaticallyOnlyOnceAndLeavesUnknownFormatsPending() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository = RoomPaymentRepository(database)
            val notification = PaymentNotification(
                id = "automatic-yape", packageName = "com.bcp.innovacxion.yapeapp", source = "Yape",
                title = "Confirmación de Pago", text = "Celia Gut* te envió un pago por S/ 1. El cód. de seguridad es: 854",
                postedAt = System.currentTimeMillis(), suggestedAmountCents = 100,
            )
            repository.capture(notification)
            assertEquals(0L, repository.loadDashboard().dashboard.balanceCents)
            repository.setSource(packageName = notification.packageName, enabled = true)
            repository.capture(notification)
            repository.capture(notification)
            repository.confirm(id = notification.id, amountCents = 9999)
            assertTrue(repository.observePending().first().isEmpty())
            val result = repository.observeDashboard().first()
            assertEquals(100L, result.dashboard.balanceCents)
            assertEquals(1, result.dashboard.incomeCount)
            assertTrue(result.dashboard.movements.single().automatic)
            assertEquals("Celia Gut*", result.dashboard.movements.single().title)
            repository.capture(notification.copy(id = "unrecognized", text = "Tu pago por S/ 1 fue rechazado"))
            assertEquals(1, repository.observePending().first().size)
            assertEquals(100L, repository.loadDashboard().dashboard.balanceCents)
        } finally {
            database.close()
        }
    }

    @Test fun persistsSelectionAndReviewWithoutCountingDuplicatesOrDiscardedNotifications() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "notification-test-${System.nanoTime()}.db"
        var database = Room.databaseBuilder(context = context, klass = PaymentDatabase::class.java, name = databaseName).build()
        try {
            var repository = RoomPaymentRepository(database)
            val notification = PaymentNotification(
                id = "payment-1", packageName = "test.bank", source = "Banco de prueba",
                title = "Recibiste un pago", text = "S/ 25.00", postedAt = System.currentTimeMillis(),
                suggestedAmountCents = 2500L,
            )
            repository.capture(notification)
            assertTrue(repository.observePending().first().isEmpty())
            repository.setSource(packageName = "test.bank", enabled = true)
            repository.capture(notification)
            repository.capture(notification)
            assertEquals(1, repository.observePending().first().size)
            assertEquals(0L, repository.loadDashboard().dashboard.balanceCents)
            repository.confirm(id = notification.id, amountCents = 2500L)
            repository.confirm(id = notification.id, amountCents = 9000L)
            repository.capture(notification)
            assertTrue(repository.observePending().first().isEmpty())
            assertEquals(2500L, repository.loadDashboard().dashboard.balanceCents)
            repository.capture(notification.copy(id = "payment-2"))
            repository.confirm(id = "payment-2", amountCents = -500L)
            repository.capture(notification.copy(id = "payment-3"))
            repository.discard("payment-3")
            repository.capture(notification.copy(id = "payment-3"))
            assertTrue(repository.observePending().first().isEmpty())
            repository.capture(notification.copy(id = "gmail", packageName = "com.google.android.gm"))
            assertTrue(repository.observePending().first().isEmpty())
            database.close()
            database = Room.databaseBuilder(context = context, klass = PaymentDatabase::class.java, name = databaseName).build()
            repository = RoomPaymentRepository(database)
            assertEquals(setOf("test.bank"), repository.observeSources().first())
            val dashboard = repository.loadDashboard().dashboard
            assertEquals(2000L, dashboard.balanceCents)
            assertEquals(2500L, dashboard.incomeCents)
            assertEquals(500L, dashboard.expenseCents)
            assertEquals(2, dashboard.movements.size)
            assertEquals(2500L, dashboard.weeklyIncomeFlow.sum())
            assertEquals(2500L, dashboard.monthlyIncomeFlow.sum())
            assertEquals(2500L, dashboard.receivedMovements.single().amountCents)
            repository.setSource(packageName = "test.bank", enabled = false)
            repository.capture(notification.copy(id = "disabled"))
            assertTrue(repository.observePending().first().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
