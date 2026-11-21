package com.devpaul.pagocontrol.features.reports.ui.reports

import androidx.lifecycle.viewModelScope
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.MovementHistoryRepository
import com.devpaul.pagocontrol.domain.ReportRepository
import com.devpaul.pagocontrol.features.reports.export.ReportExporter
import com.devpaul.pagocontrol.platform.BaseViewModel
import com.devpaul.pagocontrol.session.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ReportsViewModel(
    private val exporter: ReportExporter,
    private val history: MovementHistoryRepository,
    private val repository: ReportRepository,
    private val auth: AuthRepository,
) :
    BaseViewModel<ReportsUiState>(ReportsUiState()) {
    private var observation: Job? = null
    init { observe() }

    private fun observe() {
        observation?.cancel()
        observation = viewModelScope.launch {
            auth.session.collectLatest { session ->
                updateState { ReportsUiState() }
                if (session == null) {
                    updateState { it.copy(loading = false) }
                    return@collectLatest
                }
                try {
                    combine(history.observeMovementHistory(), repository.observe(session.id)) { movements, settings -> movements to settings }
                        .collect { (movements, settings) ->
                            withContext(Dispatchers.Default) { state.value.copy(movements = movements, settings = settings).report }
                            updateState { it.copy(movements = movements, settings = settings, loading = false, error = false) }
                        }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { updateState { it.copy(loading = false, error = true) } }
            }
        }
    }

    private fun save(action: suspend (String) -> Unit) {
        val userId = auth.session.value?.id ?: return
        if (state.value.saving) return
        updateState { it.copy(saving = true) }
        viewModelScope.launch {
            try {
                action(userId)
                if (auth.session.value?.id == userId) updateState { it.copy(dialog = null) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { if (auth.session.value?.id == userId) updateState { it.copy(notice = ReportsNotice.SaveError) } }
            finally { if (auth.session.value?.id == userId) updateState { it.copy(saving = false) } }
        }
    }

    fun onIntent(intent: ReportsUiIntent) {
        when (intent) {
            ReportsUiIntent.DismissNotice -> updateState { it.copy(notice = null) }
            ReportsUiIntent.Retry -> observe()
            is ReportsUiIntent.Open -> updateState { it.copy(dialog = intent.dialog) }
            is ReportsUiIntent.SaveExpense -> save { repository.saveExpense(it, intent.expense) }
            is ReportsUiIntent.DeleteExpense -> save { repository.removeExpense(it, intent.id) }
            is ReportsUiIntent.AddCategory -> save { repository.addCategory(it, ExpenseCategory.custom(intent.name)) }
            is ReportsUiIntent.Category -> save { userId ->
                repository.setCategory(userId, intent.id, intent.category)
                if (intent.category == ExpenseCategory.Subscriptions) {
                    val raw = state.value.movements.find { it.id == intent.id }
                    val name = raw?.let { SubscriptionCatalog.detect("${it.title} ${it.notificationTitle} ${it.notificationText}", state.value.settings.rules) }
                        ?: state.value.report.expenses.find { it.id == intent.id }?.title
                    if (name != null) repository.setSubscriptionHidden(userId, name, false)
                }
            }
            is ReportsUiIntent.AddEmailSubscription -> save { userId ->
                repository.setCategory(userId, intent.movementId, ExpenseCategory.Subscriptions)
                repository.setSubscriptionHidden(userId, intent.name, false)
            }
            is ReportsUiIntent.SubscriptionVisibility -> save { repository.setSubscriptionHidden(it, intent.name, intent.hidden) }
            is ReportsUiIntent.SaveSubscription -> save { repository.saveRule(it, intent.rule) }
            is ReportsUiIntent.DeleteSubscription -> save { repository.removeRule(it, intent.id) }
            is ReportsUiIntent.Period -> updateState {
                if (intent.value == ReportPeriod.Custom) it.copy(customDialog = true)
                else it.copy(period = intent.value, selectedPoint = 0)
            }

            is ReportsUiIntent.CustomRange -> if (intent.firstMonth in 1..12 && intent.lastMonth in intent.firstMonth..12) updateState {
                it.copy(
                    period = ReportPeriod.Custom,
                    firstMonth = intent.firstMonth,
                    lastMonth = intent.lastMonth,
                    selectedPoint = 0,
                    customDialog = false
                )
            }

            is ReportsUiIntent.Point -> if (intent.index in state.value.report.points.indices) updateState {
                it.copy(
                    selectedPoint = intent.index
                )
            }

            is ReportsUiIntent.Format -> updateState { it.copy(format = intent.value) }
            ReportsUiIntent.PrepareExport -> if (state.value.pendingExport == null && !state.value.exporting) updateState {
                it.copy(
                    pendingExport = ReportExport(it.report, it.format)
                )
            }

            is ReportsUiIntent.Export -> export(intent.uri)
            ReportsUiIntent.Dismiss -> updateState { it.copy(customDialog = false, dialog = null, notice = null) }
        }
    }

    private fun export(uri: String?) {
        if (state.value.exporting) return
        val pending = state.value.pendingExport ?: return
        if (uri == null) {
            updateState { it.copy(pendingExport = null) }; return
        }
        updateState { it.copy(exporting = true) }
        viewModelScope.launch {
            try {
                exporter.write(uri, pending)
                updateState { it.copy(notice = ReportsNotice.ExportSaved) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(notice = ReportsNotice.ExportError) }
            } finally {
                updateState { it.copy(exporting = false, pendingExport = null) }
            }
        }
    }
}
