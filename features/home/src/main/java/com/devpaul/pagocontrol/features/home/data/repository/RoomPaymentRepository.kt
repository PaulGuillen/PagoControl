package com.devpaul.pagocontrol.features.home.data.repository

import com.devpaul.pagocontrol.domain.MovementHistoryRepository
import com.devpaul.pagocontrol.domain.RecordedMovement
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationSourceEntity
import com.devpaul.pagocontrol.features.home.data.mapper.asPaymentNotification
import com.devpaul.pagocontrol.features.home.domain.entity.Dashboard
import com.devpaul.pagocontrol.features.home.domain.entity.DashboardResult
import com.devpaul.pagocontrol.features.home.domain.entity.Movement
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.domain.recognition.AutomaticBankWalletPayment
import com.devpaul.pagocontrol.features.home.domain.recognition.AutomaticBcpExpense
import com.devpaul.pagocontrol.features.home.domain.recognition.AutomaticGmailPayment
import com.devpaul.pagocontrol.features.home.domain.recognition.OwnAccountTransfer
import com.devpaul.pagocontrol.features.home.domain.recognition.AutomaticYapePayment
import com.devpaul.pagocontrol.features.home.domain.repository.GmailMonitoringRepository
import com.devpaul.pagocontrol.features.home.domain.repository.HomeRepository
import com.devpaul.pagocontrol.features.home.domain.repository.MovementNotifier
import com.devpaul.pagocontrol.features.home.domain.repository.PaymentNotificationRepository
import com.devpaul.pagocontrol.features.home.domain.validation.GmailTransactionCandidate
import com.devpaul.pagocontrol.features.home.domain.validation.WalletTransactionCandidate
import com.devpaul.pagocontrol.session.AuthRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@OptIn(ExperimentalCoroutinesApi::class)
@Single(binds = [HomeRepository::class, PaymentNotificationRepository::class, GmailMonitoringRepository::class, MovementHistoryRepository::class])
class RoomPaymentRepository(
    database: PaymentDatabase,
    private val notifier: MovementNotifier,
    private val auth: AuthRepository
) :
    HomeRepository, MovementHistoryRepository,
    PaymentNotificationRepository, GmailMonitoringRepository {
    private val dao = database.notifications()

    private fun ownerId(): String = checkNotNull(auth.session.value?.id) { "Session required" }

    private fun <T> forAccount(empty: T, query: (String) -> Flow<T>): Flow<T> =
        auth.session.map { it?.id }.distinctUntilChanged().flatMapLatest { uid ->
            if (uid == null) flowOf(empty) else flow {
                dao.claimLegacy(uid)
                emitAll(query(uid))
            }
        }

    override suspend fun loadDashboard(): DashboardResult {
        val uid = auth.session.value?.id ?: return DashboardResult.SessionRequired
        dao.claimLegacy(uid)
        return dashboard(dao.confirmed(ownerId = uid))
    }

    override fun observeDashboard(): Flow<DashboardResult.Success> =
        forAccount(dashboard(emptyList())) { uid ->
            dao.observeConfirmed(ownerId = uid).map { dashboard(it) }
        }

    override fun observeMovementHistory(): Flow<List<RecordedMovement>> =
        forAccount(emptyList()) { uid ->
            combine(
                dao.observeConfirmed(ownerId = uid),
                dao.observeConfirmed(mail = true, ownerId = uid),
            ) { wallet, gmail ->
                val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-PE"))
                (wallet + gmail).filterNot {
                    OwnAccountTransfer.matches(title = it.title, text = it.text)
                }.sortedByDescending { it.postedAt }.map { row ->
                    RecordedMovement(
                        id = row.id,
                        title = row.toMovement(formatter).title,
                        source = row.source,
                        packageName = row.packageName,
                        amountCents = row.amountCents ?: 0,
                        postedAt = row.postedAt,
                        notificationTitle = row.title,
                        notificationText = row.text,
                    )
                }
            }
        }

    override fun observePending() = forAccount(emptyList<PaymentNotification>()) { uid ->
        dao.observePending(ownerId = uid).map { rows ->
            rows.filter {
                it.status == "pending" || OwnAccountTransfer.matches(
                    title = it.title,
                    text = it.text
                )
            }.map { it.toNotification() }.filter(WalletTransactionCandidate::matches)
        }
    }

    override fun observeSources() = forAccount(emptyList<String>()) { uid ->
        dao.observeSources(ownerId = uid).map { sources ->
            sources.filterNot { it == GmailTransactionCandidate.PACKAGE_NAME }
        }
    }

    override fun observeGmailDashboard(): Flow<DashboardResult.Success> =
        forAccount(dashboard(emptyList())) { uid ->
            dao.observeConfirmed(mail = true, ownerId = uid)
                .map { dashboard(it) }
        }

    override fun observeGmailPending() = forAccount(emptyList<PaymentNotification>()) { uid ->
        combine(
            dao.observePending(mail = true, ownerId = uid),
            dao.observeConfirmed(mail = true, ownerId = uid)
        ) { rows, recorded ->
            rows.filter {
                it.status == "pending" || OwnAccountTransfer.matches(
                    title = it.title,
                    text = it.text
                )
            }.map { row ->
                row.toNotification().copy(possibleDuplicate = recorded.any {
                    AutomaticGmailPayment.possibleDuplicate(
                        row.toNotification(),
                        it.toNotification(),
                        it.amountCents
                    )
                })
            }
        }
    }

    override fun observeGmailEnabled() =
        forAccount(false) { uid ->
            dao.observeSources(ownerId = uid).map { GmailTransactionCandidate.PACKAGE_NAME in it }
        }

    override suspend fun setGmailEnabled(enabled: Boolean) {
        if (enabled) dao.enable(
            NotificationSourceEntity(
                packageName = GmailTransactionCandidate.PACKAGE_NAME,
                ownerId = ownerId()
            )
        )
        else dao.disable(packageName = GmailTransactionCandidate.PACKAGE_NAME, ownerId = ownerId())
    }

    override suspend fun setSource(packageName: String, enabled: Boolean) {
        require(packageName != "com.google.android.gm")
        if (enabled) dao.enable(
            NotificationSourceEntity(
                packageName = packageName,
                ownerId = ownerId()
            )
        ) else dao.disable(packageName = packageName, ownerId = ownerId())
    }

    override suspend fun capture(notification: PaymentNotification) {
        val uid = auth.session.value?.id ?: return
        dao.claimLegacy(uid)
        if (notification.packageName != GmailTransactionCandidate.PACKAGE_NAME && !WalletTransactionCandidate.matches(
                notification
            )
        ) return
        if (notification.packageName == GmailTransactionCandidate.PACKAGE_NAME && !AutomaticGmailPayment.isCandidate(
                notification
            )
        ) return
        val automaticPayment = AutomaticYapePayment.recognize(notification)
        val automaticBankWallet = AutomaticBankWalletPayment.recognize(notification)
        val automaticMail = AutomaticGmailPayment.recognize(notification)
        val ownTransfer =
            OwnAccountTransfer.matches(title = notification.title, text = notification.text)
        val automaticAmount = if (ownTransfer) AutomaticGmailPayment.suggestedAmount(notification)
        else automaticPayment?.amountCents ?: automaticBankWallet?.amountCents ?: automaticMail?.amountCents
        val recorded = dao.captureIfEnabled(
            NotificationEntity(
                id = notification.id,
                ownerId = uid,
                packageName = notification.packageName,
                source = notification.source,
                title = notification.title,
                text = notification.text,
                postedAt = notification.postedAt,
                suggestedAmountCents = if (notification.packageName == GmailTransactionCandidate.PACKAGE_NAME) AutomaticGmailPayment.suggestedAmount(
                    notification
                ) else WalletTransactionCandidate.suggestedAmount(notification),
                amountCents = automaticAmount,
                status = when {
                    ownTransfer && automaticAmount != null -> "transfer"; automaticAmount != null -> "automatic"; else -> "pending"
                },
            )
        )
        recorded?.let { notifyRecorded(it) }
    }

    override suspend fun confirm(id: String, amountCents: Long) {
        require(amountCents != 0L && amountCents in -999_999_999_99L..999_999_999_99L)
        dao.confirmMovement(id = id, amountCents = amountCents, ownerId = ownerId())
            ?.let { notifyRecorded(it) }
    }

    override suspend fun markOwnTransfer(id: String, amountCents: Long) {
        require(amountCents != 0L && amountCents in -99_999_999_999L..99_999_999_999L)
        val uid = ownerId()
        val row = dao.find(id = id, ownerId = uid) ?: return
        require(OwnAccountTransfer.matches(title = row.title, text = row.text))
        dao.markOwnTransfer(id = id, amountCents = amountCents, ownerId = uid)
    }

    private suspend fun notifyRecorded(row: NotificationEntity) {
        if (row.status == "transfer") return
        if (auth.session.value?.id != row.ownerId) return
        try {
            notifier.notify(
                row.toMovement(
                    SimpleDateFormat(
                        "dd MMM, HH:mm",
                        Locale.forLanguageTag("es-PE")
                    )
                )
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // An unavailable notification must not fail an already committed movement.
        }
    }

    override suspend fun discard(id: String) = dao.discard(id = id, ownerId = ownerId())

    private fun dashboard(
        rows: List<NotificationEntity>
    ): DashboardResult.Success {
        val financialRows =
            rows.filter { !OwnAccountTransfer.matches(title = it.title, text = it.text) }
        val now = Calendar.getInstance()
        val monthRows = financialRows.filter { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            date.get(Calendar.YEAR) == now.get(Calendar.YEAR) && date.get(Calendar.MONTH) == now.get(
                Calendar.MONTH
            )
        }
        val income = monthRows.filter { (it.amountCents ?: 0) > 0 }
        val expenses = monthRows.filter { (it.amountCents ?: 0) < 0 }
        val weekStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(
            Calendar.MILLISECOND,
            0
        )
            add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        val weekly = MutableList(7) { 0L }
        val monthly = MutableList(12) { 0L }
        val weeklyIncome = MutableList(7) { 0L }
        val monthlyIncome = MutableList(12) { 0L }
        financialRows.forEach { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            val end = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
            if (row.postedAt >= weekStart.timeInMillis && row.postedAt < end.timeInMillis) {
                val index = (date.get(Calendar.DAY_OF_WEEK) + 5) % 7
                weekly[index] += row.amountCents ?: 0
                weeklyIncome[index] += (row.amountCents ?: 0).coerceAtLeast(0)
            }
        }
        financialRows.forEach { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            if (date.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                val index = date.get(Calendar.MONTH)
                monthly[index] += row.amountCents ?: 0
                monthlyIncome[index] += (row.amountCents ?: 0).coerceAtLeast(0)
            }
        }
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-PE"))
        return DashboardResult.Success(
            Dashboard(
                balanceCents = financialRows.sumOf { it.amountCents ?: 0 },
                incomeCents = income.sumOf { it.amountCents ?: 0 },
                expenseCents = -expenses.sumOf { it.amountCents ?: 0 },
                incomeCount = income.size, expenseCount = expenses.size,
                weeklyFlow = weekly, monthlyFlow = monthly,
                movements = financialRows.map {
                    it.toMovement(
                        formatter
                    )
                },
                weeklyIncomeFlow = weeklyIncome, monthlyIncomeFlow = monthlyIncome,
                receivedMovements = financialRows.filter { (it.amountCents ?: 0) > 0 }
                    .map { it.toMovement(formatter) },
            )
        )
    }

    private fun NotificationEntity.toMovement(formatter: SimpleDateFormat) = Movement(
        id = id,
        title = AutomaticBcpExpense.recognize(toNotification())?.let { "BCP - ${it.merchant}" }
            ?: if (status == "automatic") {
                AutomaticYapePayment.recognize(toNotification())?.sender
                    ?: AutomaticBankWalletPayment.recognize(toNotification())?.sender
                    ?: AutomaticGmailPayment.recognize(toNotification())?.title ?: title
            } else title.ifBlank { source },
        detail = formatter.format(Date(postedAt)),
        source = source,
        amountCents = amountCents ?: 0,
        packageName = packageName,
        notificationTitle = title,
        notificationText = text,
        automatic = status == "automatic",
        ownTransfer = OwnAccountTransfer.matches(title = title, text = text),
    )

    private fun NotificationEntity.toNotification() = PaymentNotification(
        id = id,
        packageName = packageName,
        source = source,
        title = title,
        text = text,
        postedAt = postedAt,
        suggestedAmountCents = suggestedAmountCents ?: AutomaticGmailPayment.suggestedAmount(
            asPaymentNotification()
        ),
    )
}
