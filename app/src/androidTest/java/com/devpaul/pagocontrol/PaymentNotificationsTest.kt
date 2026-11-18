package com.devpaul.pagocontrol

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.repository.RoomPaymentRepository
import com.devpaul.pagocontrol.features.home.domain.entity.DashboardResult
import com.devpaul.pagocontrol.features.home.domain.entity.Movement
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.domain.repository.MovementNotifier
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentNotificationsTest {
    @Test
    fun automaticOwnTransferRequiresPhraseAndOldIncorrectLabelsDoNotHideExpenses() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val notices = mutableListOf<Movement>()
            val repository = RoomPaymentRepository(
                database = database,
                auth = auth,
                notifier = MovementNotifier { notices += it })
            repository.setGmailEnabled(true)
            repository.capture(
                PaymentNotification(
                    id = "own",
                    packageName = "com.google.android.gm",
                    source = "Gmail",
                    title = "BCP Notificaciones",
                    text = "Transferencia entre mis cuentas S/ 100.00",
                    postedAt = System.currentTimeMillis(),
                    suggestedAmountCents = 10000,
                )
            )
            assertEquals("transfer", database.notifications().find("own")?.status)
            assertTrue(notices.isEmpty())
            database.notifications().insert(
                NotificationEntity(
                    id = "old-wrong-label",
                    packageName = "com.google.android.gm",
                    source = "Gmail",
                    title = "BCP",
                    text = "Compra OPENAI S/ 89.72",
                    postedAt = System.currentTimeMillis(),
                    suggestedAmountCents = 8972,
                    amountCents = -8972,
                    status = "transfer",
                )
            )
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(-8972L, dashboard.balanceCents)
            assertEquals(8972L, dashboard.expenseCents)
            assertEquals(0, dashboard.movements.count { it.ownTransfer })
            assertEquals(
                false,
                dashboard.movements.single { it.id == "old-wrong-label" }.ownTransfer
            )
        } finally {
            database.close()
        }
    }

    @Test
    fun ownTransfersRemainVisibleButDoNotAffectTotalsChartsOrEmitAlerts() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val notices = mutableListOf<Movement>()
            val repository = RoomPaymentRepository(
                database = database,
                auth = auth,
                notifier = MovementNotifier { notices += it })

            suspend fun insert(id: String, amount: Long, status: String = "confirmed") {
                database.notifications().insert(
                    NotificationEntity(
                        id = id,
                        packageName = "com.google.android.gm",
                        source = "Gmail",
                        title = id,
                        text = if (id.startsWith("transfer")) "Transferencia entre mis cuentas" else "Receipt",
                        postedAt = System.currentTimeMillis(),
                        suggestedAmountCents = amount,
                        amountCents = amount,
                        status = status,
                    )
                )
            }
            insert(id = "income", amount = 10000)
            insert(id = "expense", amount = -1000)
            insert(id = "transfer-out", amount = -50000)
            insert(id = "transfer-in", amount = 50000, status = "pending")
            repository.markOwnTransfer(id = "transfer-out", amountCents = -50000)
            repository.markOwnTransfer(id = "transfer-in", amountCents = 50000)
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(9000L, dashboard.balanceCents)
            assertEquals(10000L, dashboard.incomeCents)
            assertEquals(1000L, dashboard.expenseCents)
            assertEquals(1, dashboard.incomeCount)
            assertEquals(1, dashboard.expenseCount)
            assertEquals(9000L, dashboard.weeklyFlow.sum())
            assertEquals(9000L, dashboard.monthlyFlow.sum())
            assertEquals(10000L, dashboard.weeklyIncomeFlow.sum())
            assertEquals(0, dashboard.movements.count { it.ownTransfer })
            assertEquals(2, dashboard.movements.size)
            assertEquals(2, repository.observeGmailPending().first().size)
            assertTrue(notices.isEmpty())
            assertTrue(database.notifications().pendingWrites("").all { it.pendingWriteId != null })
            repository.confirm(id = "transfer-in", amountCents = 50000)
            assertEquals(10000L, repository.observeGmailDashboard().first().dashboard.incomeCents)
            repository.discard("transfer-out")
            assertEquals(2, repository.observeGmailDashboard().first().dashboard.movements.size)
        } finally {
            database.close()
        }
    }

    private val auth = object : AuthRepository {
        override val session = MutableStateFlow<UserSession?>(
            UserSession(
                id = "",
                name = "Test",
                email = "test@example.com"
            )
        )

        override suspend fun signIn(email: String, password: String) = SignInResult.Success
        override suspend fun signOut() {
            session.value = null
        }
    }

    @Test
    fun notifiesOnlyNewRecordedMovementsAndNeverPendingOrDuplicateCaptures() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val notices = mutableListOf<Movement>()
            val repository = RoomPaymentRepository(
                auth = auth,
                database = database,
                notifier = MovementNotifier { notices += it })
            val bank = PaymentNotification(
                id = "bank-alert", packageName = "com.google.android.gm", source = "Gmail",
                title = "BCP", text = "Compra S/ 25.00", postedAt = System.currentTimeMillis(),
                suggestedAmountCents = null,
            )
            repository.capture(bank)
            assertTrue(notices.isEmpty())
            repository.setGmailEnabled(true)
            repository.capture(bank)
            repository.capture(bank)
            repository.capture(bank.copy(id = "bank-duplicate"))
            assertEquals(listOf(-2500L), notices.map { it.amountCents })
            repository.capture(bank.copy(id = "review", title = "Steam", text = "Receipt S/ 30.00"))
            assertEquals(1, notices.size)
            repository.confirm(id = "review", amountCents = -3000)
            repository.confirm(id = "review", amountCents = -3000)
            repository.confirm(id = "missing", amountCents = 1000)
            assertEquals(listOf(-2500L, -3000L), notices.map { it.amountCents })
            repository.capture(
                bank.copy(
                    id = "income-review",
                    title = "Steam",
                    text = "Receipt S/ 40.00"
                )
            )
            repository.confirm(id = "income-review", amountCents = 4000)
            assertEquals(4000L, notices.last().amountCents)
            repository.discard("review")
            assertEquals(3, notices.size)
            RoomPaymentRepository(
                auth = auth,
                database = database,
                notifier = MovementNotifier { notices += it })
                .observeGmailDashboard().first()
            assertEquals(3, notices.size)
        } finally {
            database.close()
        }
    }

    @Test
    fun notificationFailureDoesNotUndoMovementOrRepeatConfirmation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            var attempts = 0
            val repository =
                RoomPaymentRepository(
                    database = database,
                    auth = auth,
                    notifier = MovementNotifier {
                        attempts++
                        error("Notifications unavailable")
                    })
            database.notifications().insert(
                NotificationEntity(
                    id = "review", packageName = "com.google.android.gm", source = "Gmail",
                    title = "Receipt", text = "S/ 10.00", postedAt = System.currentTimeMillis(),
                    suggestedAmountCents = 1000,
                )
            )
            repository.confirm(id = "review", amountCents = 1000)
            repository.confirm(id = "review", amountCents = 1000)
            assertEquals(1000L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            assertEquals(1, attempts)
        } finally {
            database.close()
        }
    }

    @Test
    fun monthlyChartGroupsCurrentYearByMonthAndExcludesPriorYears() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            suspend fun insert(id: String, targetYear: Int, month: Int, amount: Long) {
                val time = Calendar.getInstance()
                    .apply { clear(); set(targetYear, month, 14, 12, 0) }.timeInMillis
                database.notifications().insert(
                    NotificationEntity(
                        id,
                        "com.google.android.gm",
                        "Gmail",
                        "Banco",
                        "Operacion",
                        time,
                        null,
                        amount,
                        "confirmed"
                    )
                )
            }
            insert("january", year, 0, 10000)
            insert("november", year, 10, -656)
            insert("prior-year", year - 1, 0, 50000)
            val dashboard =
                RoomPaymentRepository(
                    auth = auth,
                    database = database,
                    notifier = MovementNotifier {}).observeGmailDashboard().first().dashboard
            assertEquals(12, dashboard.monthlyFlow.size)
            assertEquals(10000L, dashboard.monthlyFlow[0])
            assertEquals(-656L, dashboard.monthlyFlow[10])
            assertEquals(10000L, dashboard.monthlyIncomeFlow.sum())
            assertEquals(9344L, dashboard.monthlyFlow.sum())
        } finally {
            database.close()
        }
    }

    @Test
    fun keepsUnknownExistingGmailPendingUntilConfirmed() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository =
                RoomPaymentRepository(
                    database = database,
                    auth = auth,
                    notifier = MovementNotifier {})
            repeat(21) { index ->
                database.notifications().insert(
                    NotificationEntity(
                        id = "old-$index", packageName = "com.google.android.gm", source = "Gmail",
                        title = "Banco", text = "Transferencia recibida S/ 10.00",
                        postedAt = System.currentTimeMillis(), suggestedAmountCents = 1000,
                    )
                )
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

    @Test
    fun bankAndMerchantReceiptsNeverAutomaticallyCountTwice() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository =
                RoomPaymentRepository(
                    database = database,
                    auth = auth,
                    notifier = MovementNotifier {})
            repository.setGmailEnabled(true)
            val bank = PaymentNotification(
                "bank",
                "com.google.android.gm",
                "Gmail",
                "BCP",
                "Compra S/ 25.00",
                System.currentTimeMillis(),
                null
            )
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
        } finally {
            database.close()
        }
    }

    @Test
    fun recordsBcpConsumptionAsAutomaticExpenseWhenGmailIsEnabled() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val repository =
                RoomPaymentRepository(
                    database = database,
                    auth = auth,
                    notifier = MovementNotifier {})
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
            assertEquals(
                0L,
                (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
            )
            repository.capture(
                notification.copy(
                    id = "another",
                    text = "Compra realizada por S/ 10.00"
                )
            )
            assertTrue(repository.observeGmailPending().first().isEmpty())
            assertEquals(-3578L, repository.observeGmailDashboard().first().dashboard.balanceCents)
            repository.discard("another")
            repository.capture(
                notification.copy(
                    id = "another",
                    text = "Compra realizada por S/ 10.00"
                )
            )
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

    @Test
    fun persistsGmailOptInAndSeparatesItsPaymentsFromWallets() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "gmail-test-${System.nanoTime()}.db"
        var database = Room.databaseBuilder(
            context = context,
            klass = PaymentDatabase::class.java,
            name = databaseName
        ).build()
        try {
            var repository =
                RoomPaymentRepository(
                    database = database,
                    auth = auth,
                    notifier = MovementNotifier {})
            val notification = PaymentNotification(
                id = "gmail-transfer",
                packageName = "com.google.android.gm",
                source = "Gmail",
                title = "BBVA",
                text = "Transferencia recibida S/ 100.00",
                postedAt = System.currentTimeMillis(),
                suggestedAmountCents = 10000,
            )
            repository.capture(notification)
            assertTrue(repository.observeGmailPending().first().isEmpty())
            repository.setGmailEnabled(true)
            repository.capture(notification)
            repository.capture(notification)
            repository.capture(
                notification.copy(
                    id = "unrelated",
                    text = "Reunión del equipo mañana"
                )
            )
            assertEquals(0, repository.observeGmailPending().first().size)
            assertTrue(repository.observePending().first().isEmpty())
            assertTrue(repository.observeSources().first().isEmpty())
            repository.confirm(id = notification.id, amountCents = 10000)
            repository.capture(
                notification.copy(
                    id = "gmail-expense",
                    text = "Pago realizado S/ 25.00"
                )
            )
            repository.confirm(id = "gmail-expense", amountCents = -2500)
            assertEquals(
                0L,
                (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
            )
            database.close()
            database = Room.databaseBuilder(
                context = context,
                klass = PaymentDatabase::class.java,
                name = databaseName
            ).build()
            repository = RoomPaymentRepository(
                database = database,
                auth = auth,
                notifier = MovementNotifier {})
            assertTrue(repository.observeGmailEnabled().first())
            val dashboard = repository.observeGmailDashboard().first().dashboard
            assertEquals(7500L, dashboard.balanceCents)
            assertEquals(10000L, dashboard.incomeCents)
            assertEquals(2500L, dashboard.expenseCents)
            repository.setGmailEnabled(false)
            repository.capture(notification.copy(id = "disabled"))
            assertTrue(repository.observeGmailPending().first().isEmpty())
            assertEquals(
                0L,
                (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
            )
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun recordsRecognizedYapeIncomeAutomaticallyOnlyOnceAndLeavesUnknownFormatsPending() =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val database =
                Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
            try {
                val repository =
                    RoomPaymentRepository(
                        database = database,
                        auth = auth,
                        notifier = MovementNotifier {})
                val notification = PaymentNotification(
                    id = "automatic-yape",
                    packageName = "com.bcp.innovacxion.yapeapp",
                    source = "Yape",
                    title = "Confirmación de Pago",
                    text = "Celia Gut* te envió un pago por S/ 1. El cód. de seguridad es: 854",
                    postedAt = System.currentTimeMillis(),
                    suggestedAmountCents = 100,
                )
                repository.capture(notification)
                assertEquals(
                    0L,
                    (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
                )
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
                repository.capture(
                    notification.copy(
                        id = "rejected",
                        text = "Tu pago por S/ 1 fue rechazado"
                    )
                )
                repository.capture(notification.copy(id = "no-amount", text = "Recibiste un pago"))
                repository.capture(
                    notification.copy(
                        id = "promotion",
                        text = "Oferta de compra por S/ 1"
                    )
                )
                assertTrue(repository.observePending().first().isEmpty())
                repository.capture(
                    notification.copy(
                        id = "unrecognized",
                        text = "Recibiste una transferencia por S/ 1"
                    )
                )
                assertEquals(1, repository.observePending().first().size)
                assertEquals(
                    100L,
                    repository.observePending().first().single().suggestedAmountCents
                )
                assertEquals(
                    100L,
                    (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
                )
            } finally {
                database.close()
            }
        }

    @Test
    fun persistsSelectionAndReviewWithoutCountingDuplicatesOrDiscardedNotifications() =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val databaseName = "notification-test-${System.nanoTime()}.db"
            var database = Room.databaseBuilder(
                context = context,
                klass = PaymentDatabase::class.java,
                name = databaseName
            ).build()
            try {
                var repository =
                    RoomPaymentRepository(
                        database = database,
                        auth = auth,
                        notifier = MovementNotifier {})
                val notification = PaymentNotification(
                    id = "payment-1",
                    packageName = "test.bank",
                    source = "Banco de prueba",
                    title = "Recibiste un pago",
                    text = "S/ 25.00",
                    postedAt = System.currentTimeMillis(),
                    suggestedAmountCents = 2500L,
                )
                repository.capture(notification)
                assertTrue(repository.observePending().first().isEmpty())
                repository.setSource(packageName = "test.bank", enabled = true)
                repository.capture(notification)
                repository.capture(notification)
                assertEquals(1, repository.observePending().first().size)
                assertEquals(
                    0L,
                    (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
                )
                repository.confirm(id = notification.id, amountCents = 2500L)
                repository.confirm(id = notification.id, amountCents = 9000L)
                repository.capture(notification)
                assertTrue(repository.observePending().first().isEmpty())
                assertEquals(
                    2500L,
                    (repository.loadDashboard() as DashboardResult.Success).dashboard.balanceCents
                )
                repository.capture(notification.copy(id = "payment-2"))
                repository.confirm(id = "payment-2", amountCents = -500L)
                repository.capture(notification.copy(id = "payment-3"))
                repository.discard("payment-3")
                repository.capture(notification.copy(id = "payment-3"))
                assertTrue(repository.observePending().first().isEmpty())
                repository.capture(
                    notification.copy(
                        id = "gmail",
                        packageName = "com.google.android.gm"
                    )
                )
                assertTrue(repository.observePending().first().isEmpty())
                database.close()
                database = Room.databaseBuilder(
                    context = context,
                    klass = PaymentDatabase::class.java,
                    name = databaseName
                ).build()
                repository =
                    RoomPaymentRepository(
                        database = database,
                        auth = auth,
                        notifier = MovementNotifier {})
                assertEquals(listOf("test.bank"), repository.observeSources().first())
                val dashboard = (repository.loadDashboard() as DashboardResult.Success).dashboard
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
