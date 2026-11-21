package com.devpaul.pagocontrol.domain

import kotlinx.coroutines.flow.Flow

@ConsistentCopyVisibility
data class ExpenseCategory private constructor(val name: String) : java.io.Serializable {
    val customName: String? get() = name.takeIf { it.startsWith("custom:") }?.removePrefix("custom:")
    companion object {
        val Food = ExpenseCategory("Food")
        val Services = ExpenseCategory("Services")
        val Shopping = ExpenseCategory("Shopping")
        val Subscriptions = ExpenseCategory("Subscriptions")
        val Transport = ExpenseCategory("Transport")
        val Games = ExpenseCategory("Games")
        val Other = ExpenseCategory("Other")
        val entries = listOf(Food, Services, Shopping, Subscriptions, Transport, Games, Other)
        fun custom(name: String): ExpenseCategory {
            val clean = name.trim().replace(Regex("\\s+"), " ")
            require(clean.length in 2..40)
            return ExpenseCategory("custom:$clean")
        }
        fun valueOf(name: String): ExpenseCategory = entries.find { it.name == name }
            ?: if (name.startsWith("custom:")) custom(name.removePrefix("custom:")) else Other
    }
}

data class ManualExpense(val id: String, val title: String, val cents: Long, val date: String, val channel: String, val category: ExpenseCategory)
data class SubscriptionRule(val id: String, val name: String, val keyword: String)
data class ReportSettings(
    val expenses: List<ManualExpense> = emptyList(),
    val rules: List<SubscriptionRule> = emptyList(),
    val categories: Map<String, ExpenseCategory> = emptyMap(),
    val hiddenSubscriptions: Set<String> = emptySet(),
    val customCategories: List<ExpenseCategory> = emptyList(),
)
interface ReportRepository {
    fun observe(userId: String): Flow<ReportSettings>
    suspend fun addCategory(userId: String, category: ExpenseCategory)
    suspend fun saveExpense(userId: String, expense: ManualExpense)
    suspend fun removeExpense(userId: String, id: String)
    suspend fun setCategory(userId: String, movementId: String, category: ExpenseCategory)
    suspend fun setSubscriptionHidden(userId: String, name: String, hidden: Boolean)
    suspend fun saveRule(userId: String, rule: SubscriptionRule)
    suspend fun removeRule(userId: String, id: String)
}
