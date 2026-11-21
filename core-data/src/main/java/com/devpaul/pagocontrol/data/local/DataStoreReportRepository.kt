package com.devpaul.pagocontrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.ReportRepository
import com.devpaul.pagocontrol.domain.ReportSettings
import com.devpaul.pagocontrol.domain.SubscriptionRule
import java.time.LocalDate
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.reportDataStore by preferencesDataStore(name = "reports")

class DataStoreReportRepository(context: Context) : ReportRepository {
    private val store = context.applicationContext.reportDataStore
    override fun observe(userId: String) = store.data.map { decode(it[stringPreferencesKey(userId)] ?: "{}") }
    private suspend fun update(userId: String, transform: (ReportSettings) -> ReportSettings) {
        require(userId.isNotBlank())
        store.edit { prefs ->
            val key = stringPreferencesKey(userId)
            prefs[key] = encode(transform(decode(prefs[key] ?: "{}")))
        }
    }
    override suspend fun addCategory(userId: String, category: ExpenseCategory) {
        require(category.customName != null)
        update(userId) { settings ->
            require(settings.customCategories.none { it.name.equals(category.name, true) })
            settings.copy(customCategories = settings.customCategories + category)
        }
    }
    override suspend fun saveExpense(userId: String, expense: ManualExpense) {
        require(expense.cents in 1..99999999999L && expense.title.isNotBlank() && expense.title.length <= 80)
        require(expense.channel.isNotBlank() && expense.channel.length <= 40)
        require(!LocalDate.parse(expense.date).isAfter(LocalDate.now()))
        update(userId) { it.copy(expenses = if (it.expenses.any { item -> item.id == expense.id }) it.expenses.map { item -> if (item.id == expense.id) expense else item } else it.expenses + expense) }
    }
    override suspend fun removeExpense(userId: String, id: String) = update(userId) { it.copy(expenses = it.expenses.filterNot { item -> item.id == id }) }
    override suspend fun setCategory(userId: String, movementId: String, category: ExpenseCategory) = update(userId) { it.copy(categories = it.categories + (movementId to category)) }
    override suspend fun setSubscriptionHidden(userId: String, name: String, hidden: Boolean) = update(userId) {
        it.copy(hiddenSubscriptions = if (hidden) it.hiddenSubscriptions + name else it.hiddenSubscriptions - name)
    }
    override suspend fun saveRule(userId: String, rule: SubscriptionRule) {
        require(rule.name.trim().length in 2..60 && rule.keyword.trim().length in 3..80)
        update(userId) { it.copy(rules = it.rules.filterNot { item -> item.id == rule.id || item.keyword.equals(rule.keyword, true) } + rule, hiddenSubscriptions = it.hiddenSubscriptions - rule.name) }
    }
    override suspend fun removeRule(userId: String, id: String) = update(userId) { it.copy(rules = it.rules.filterNot { item -> item.id == id }) }

    private fun decode(raw: String): ReportSettings {
        val json = JSONObject(raw)
        val expenses = json.optJSONArray("expenses") ?: JSONArray()
        val rules = json.optJSONArray("rules") ?: JSONArray()
        val categories = json.optJSONObject("categories") ?: JSONObject()
        return ReportSettings(
            (0 until expenses.length()).map { index ->
                val item = expenses.getJSONObject(index)
                ManualExpense(item.getString("id"), item.getString("title"), item.getLong("cents"), item.getString("date"), item.getString("channel"), ExpenseCategory.valueOf(item.getString("category")))
            },
            (0 until rules.length()).map { index ->
                val item = rules.getJSONObject(index)
                SubscriptionRule(item.getString("id"), item.getString("name"), item.getString("keyword"))
            },
            categories.keys().asSequence().associateWith { ExpenseCategory.valueOf(categories.getString(it)) },
            json.optJSONArray("hidden")?.let { values -> (0 until values.length()).map { values.getString(it) }.toSet() } ?: emptySet(),
            json.optJSONArray("customCategories")?.let { values -> (0 until values.length()).map { ExpenseCategory.valueOf(values.getString(it)) } } ?: emptyList(),
        )
    }
    private fun encode(value: ReportSettings): String = JSONObject().apply {
        put("expenses", JSONArray().apply { value.expenses.forEach { item -> put(JSONObject().apply {
            put("id", item.id); put("title", item.title); put("cents", item.cents); put("date", item.date); put("channel", item.channel); put("category", item.category.name)
        }) } })
        put("rules", JSONArray().apply { value.rules.forEach { item -> put(JSONObject().apply {
            put("id", item.id); put("name", item.name); put("keyword", item.keyword)
        }) } })
        put("customCategories", JSONArray(value.customCategories.map { it.name }))
        put("hidden", JSONArray(value.hiddenSubscriptions.toList()))
        put("categories", JSONObject(value.categories.mapValues { it.value.name }))
    }.toString()
}
