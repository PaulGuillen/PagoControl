package com.devpaul.pagocontrol.data.notifications

import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.AutomaticYapePayment
import com.devpaul.pagocontrol.domain.DashboardResult
import com.devpaul.pagocontrol.domain.HomeRepository
import com.devpaul.pagocontrol.domain.GmailMonitoringRepository
import com.devpaul.pagocontrol.domain.GmailTransactionCandidate
import com.devpaul.pagocontrol.domain.Movement
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.domain.PaymentNotificationRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single(binds = [HomeRepository::class, PaymentNotificationRepository::class, GmailMonitoringRepository::class])
class RoomPaymentRepository(database: PaymentDatabase) : HomeRepository, PaymentNotificationRepository, GmailMonitoringRepository {
    private val dao = database.notifications()

    override suspend fun loadDashboard() = dashboard(dao.confirmed())

    override fun observeDashboard() = dao.observeConfirmed().map(::dashboard)

    override fun observePending() = dao.observePending().map { rows ->
        rows.map { it.toNotification() }
    }

    override fun observeSources() = dao.observeSources().map { sources -> sources.filterNot { it == GmailTransactionCandidate.PACKAGE_NAME }.toSet() }

    override fun observeGmailDashboard() = dao.observeConfirmed(mail = true).map(::dashboard)

    override fun observeGmailPending() = dao.observePending(mail = true).map { rows -> rows.map { it.toNotification() } }

    override fun observeGmailEnabled() = dao.observeSources().map { GmailTransactionCandidate.PACKAGE_NAME in it }

    override suspend fun setGmailEnabled(enabled: Boolean) {
        if (enabled) dao.enable(NotificationSourceEntity(GmailTransactionCandidate.PACKAGE_NAME))
        else dao.disable(GmailTransactionCandidate.PACKAGE_NAME)
    }

    override suspend fun setSource(packageName: String, enabled: Boolean) {
        require(packageName != "com.google.android.gm")
        if (enabled) dao.enable(NotificationSourceEntity(packageName)) else dao.disable(packageName)
    }

    override suspend fun capture(notification: PaymentNotification) {
        if (notification.packageName == GmailTransactionCandidate.PACKAGE_NAME && !GmailTransactionCandidate.matches(notification)) return
        val automaticPayment = AutomaticYapePayment.recognize(notification)
        dao.captureIfEnabled(NotificationEntity(
            id = notification.id, packageName = notification.packageName, source = notification.source,
            title = notification.title, text = notification.text, postedAt = notification.postedAt,
            suggestedAmountCents = notification.suggestedAmountCents,
            amountCents = automaticPayment?.amountCents,
            status = if (automaticPayment != null) "automatic" else "pending",
        ))
    }

    override suspend fun confirm(id: String, amountCents: Long) {
        require(amountCents != 0L && amountCents in -999_999_999_99L..999_999_999_99L)
        dao.confirm(id, amountCents)
    }

    override suspend fun discard(id: String) = dao.discard(id)

    private fun dashboard(rows: List<NotificationEntity>): DashboardResult.Success {
        val now = Calendar.getInstance()
        val monthRows = rows.filter { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            date.get(Calendar.YEAR) == now.get(Calendar.YEAR) && date.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        }
        val income = monthRows.filter { (it.amountCents ?: 0) > 0 }
        val expenses = monthRows.filter { (it.amountCents ?: 0) < 0 }
        val weekStart = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
        }
        val weekly = MutableList(7) { 0L }
        val monthly = MutableList(5) { 0L }
        val weeklyIncome = MutableList(7) { 0L }
        val monthlyIncome = MutableList(5) { 0L }
        rows.forEach { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            val end = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 7) }
            if (row.postedAt >= weekStart.timeInMillis && row.postedAt < end.timeInMillis) {
                val index = (date.get(Calendar.DAY_OF_WEEK) + 5) % 7
                weekly[index] += row.amountCents ?: 0
                weeklyIncome[index] += (row.amountCents ?: 0).coerceAtLeast(0)
            }
        }
        monthRows.forEach { row ->
            val date = Calendar.getInstance().apply { timeInMillis = row.postedAt }
            monthly[(date.get(Calendar.DAY_OF_MONTH) - 1) / 7] += row.amountCents ?: 0
            monthlyIncome[(date.get(Calendar.DAY_OF_MONTH) - 1) / 7] += (row.amountCents ?: 0).coerceAtLeast(0)
        }
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.forLanguageTag("es-PE"))
        return DashboardResult.Success(Dashboard(
            balanceCents = rows.sumOf { it.amountCents ?: 0 },
            incomeCents = income.sumOf { it.amountCents ?: 0 },
            expenseCents = -expenses.sumOf { it.amountCents ?: 0 },
            incomeCount = income.size, expenseCount = expenses.size,
            weeklyFlow = weekly, monthlyFlow = monthly,
            movements = rows.take(20).map { it.toMovement(formatter) },
            weeklyIncomeFlow = weeklyIncome, monthlyIncomeFlow = monthlyIncome,
            receivedMovements = rows.filter { (it.amountCents ?: 0) > 0 }.take(20).map { it.toMovement(formatter) },
        ))
    }

    private fun NotificationEntity.toMovement(formatter: SimpleDateFormat) = Movement(
        id = id, title = if (status == "automatic") {
            AutomaticYapePayment.recognize(toNotification())?.sender ?: title
        } else title.ifBlank { source },
        detail = formatter.format(Date(postedAt)), source = source,
        amountCents = amountCents ?: 0, automatic = status == "automatic",
    )

    private fun NotificationEntity.toNotification() = PaymentNotification(
        id = id, packageName = packageName, source = source, title = title,
        text = text, postedAt = postedAt, suggestedAmountCents = suggestedAmountCents,
    )
}
