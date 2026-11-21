package com.devpaul.pagocontrol

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.data.local.DataStoreReportRepository
import com.devpaul.pagocontrol.domain.ExpenseCategory
import com.devpaul.pagocontrol.domain.ManualExpense
import com.devpaul.pagocontrol.domain.SubscriptionRule
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportStorageTest {
    @Test
    fun expensesRulesAndCategoriesSurviveRecreationAndStayWithTheirOwner() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreReportRepository(context)
        val owner = "reports-test-${UUID.randomUUID()}"
        val other = "reports-test-${UUID.randomUUID()}"
        val expense = ManualExpense("expense", "Netflix", 4490, LocalDate.now().toString(), "BCP", ExpenseCategory.Subscriptions)
        val rule = SubscriptionRule("rule", "Custom service", "custom-service")
        val custom = ExpenseCategory.custom("Mascotas")
        repository.addCategory(owner, custom)
        repository.setCategory(owner, "custom-movement", custom)
        repository.saveExpense(owner, expense)
        repository.saveRule(owner, rule)
        repository.setCategory(owner, "recorded", ExpenseCategory.Food)
        repository.setSubscriptionHidden(owner, "Netflix", true)
        val recreated = DataStoreReportRepository(context)
        val saved = recreated.observe(owner).first()
        assertEquals(listOf(custom), saved.customCategories)
        assertEquals(custom, saved.categories["custom-movement"])
        assertEquals(expense, saved.expenses.single())
        assertEquals(rule, saved.rules.single())
        assertEquals(setOf("Netflix"), saved.hiddenSubscriptions)
        assertEquals(ExpenseCategory.Food, saved.categories["recorded"])
        val otherSettings = recreated.observe(other).first()
        assertTrue(otherSettings.customCategories.isEmpty())
        assertTrue(otherSettings.expenses.isEmpty())
        assertTrue(otherSettings.rules.isEmpty())
        assertTrue(otherSettings.categories.isEmpty())
        assertTrue(otherSettings.hiddenSubscriptions.isEmpty())
        recreated.setSubscriptionHidden(owner, "Netflix", false)
        assertTrue(repository.observe(owner).first().hiddenSubscriptions.isEmpty())
        recreated.saveExpense(owner, expense.copy(id = "second"))
        recreated.saveExpense(owner, expense.copy(cents = 2090))
        assertEquals(listOf("expense", "second"), repository.observe(owner).first().expenses.map { it.id })
        assertEquals(2090L, repository.observe(owner).first().expenses.first().cents)
        recreated.removeExpense(owner, expense.id)
        recreated.removeExpense(owner, "second")
        recreated.removeRule(owner, rule.id)
        assertTrue(repository.observe(owner).first().expenses.isEmpty())
        assertTrue(repository.observe(owner).first().rules.isEmpty())
    }
}
