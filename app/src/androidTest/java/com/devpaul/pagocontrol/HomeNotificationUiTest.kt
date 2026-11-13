package com.devpaul.pagocontrol

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.domain.Dashboard
import com.devpaul.pagocontrol.domain.Movement
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.features.home.ui.home.HomeContent
import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiState
import com.devpaul.pagocontrol.features.home.ui.home.HomeSection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeNotificationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun separatesWalletReceiptsFromMailAndDoesNotPresentAnExpenseAsZeroSpending() {
        var state by mutableStateOf(HomeUiState(content = HomeContent.Ready(Dashboard(
            balanceCents = 2000, incomeCents = 2500, expenseCents = 500, incomeCount = 1, expenseCount = 1,
            weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = emptyList(),
        )), gmailContent = HomeContent.Ready(Dashboard(
            balanceCents = 7000, incomeCents = 10000, expenseCents = 3000, incomeCount = 1, expenseCount = 1,
            weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = emptyList(),
        ))))
        compose.setContent {
            PagoControlTheme(false) {
                HomeScreen(
                    state = state, name = "Prueba",
                    onIntent = { intent -> if (intent is HomeUiIntent.SelectSection) state = state.copy(section = intent.section) },
                    themeIndex = 1, onThemeSelected = {}, notificationAccessGranted = true,
                )
            }
        }
        compose.onNodeWithText("COBROS RECIBIDOS ESTE MES").assertIsDisplayed()
        compose.onNodeWithText("S/ 25.00").assertIsDisplayed()
        compose.onNodeWithText("Egresos").assertDoesNotExist()
        compose.onNodeWithText("Correo").performClick()
        compose.onNodeWithText("BALANCE REGISTRADO").assertIsDisplayed()
        compose.onNodeWithText("Ingresos").assertIsDisplayed()
        compose.onNodeWithText("Egresos").assertIsDisplayed()
        compose.onNodeWithText("S/ 70.00").assertIsDisplayed()
        compose.onNodeWithText("COBROS RECIBIDOS ESTE MES").assertDoesNotExist()
        compose.onNodeWithText("Elegir apps a monitorear").assertDoesNotExist()
        compose.onNodeWithText("Billeteras digitales").performClick()
        compose.onNodeWithText("S/ 25.00").assertIsDisplayed()
        assertEquals(HomeSection.Wallets, state.section)
    }

    @Test fun asksBeforeDeletingGmailMovementAndAllowsCancellation() {
        val intents = mutableListOf<HomeUiIntent>()
        val state = HomeUiState(
            section = HomeSection.Mail,
            gmailContent = HomeContent.Ready(Dashboard(
                balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0,
                weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = listOf(
                    Movement(id = "gmail-payment", title = "Compra de prueba", detail = "Hoy", source = "Gmail", amountCents = -2500, automatic = true),
                ),
            )),
            gmailPendingNotifications = listOf(PaymentNotification(
                id = "gmail-payment", packageName = "com.google.android.gm", source = "Gmail",
                title = "Banco", text = "Pago realizado por S/ 25.00", postedAt = 1, suggestedAmountCents = 2500,
            )),
        )
        compose.setContent {
            PagoControlTheme(false) {
                HomeScreen(state = state, name = "Prueba", onIntent = { intents.add(it) }, themeIndex = 1, onThemeSelected = {}, notificationAccessGranted = true)
            }
        }
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Compra de prueba"))
        compose.onNodeWithText("Revisar movimiento").assertDoesNotExist()
        compose.onNodeWithContentDescription("Eliminar movimiento: Compra de prueba").performClick()
        compose.onNodeWithText("Eliminar movimiento").assertIsDisplayed()
        assertTrue(intents.isEmpty())
        compose.onNodeWithText("Cancelar").performClick()
        assertTrue(intents.isEmpty())
        compose.onNodeWithContentDescription("Eliminar movimiento: Compra de prueba").performClick()
        compose.onNodeWithText("Eliminar").performClick()
        compose.runOnIdle {
            assertEquals(listOf(HomeUiIntent.DeleteGmailMovement(id = "gmail-payment")), intents)
        }
    }

    @Test fun requiresExplicitReviewBeforeConfirmingAnIncome() {
        val intents = mutableListOf<HomeUiIntent>()
        val state = HomeUiState(
            content = HomeContent.Ready(Dashboard(
                balanceCents = 0, incomeCents = 0, expenseCents = 0, incomeCount = 0, expenseCount = 0,
                weeklyFlow = List(7) { 0 }, monthlyFlow = List(5) { 0 }, movements = emptyList(),
            )),
            pendingNotifications = listOf(PaymentNotification(
                id = "test-payment", packageName = "test.bank", source = "Banco de prueba",
                title = "Pago recibido", text = "María te envió S/ 25.00", postedAt = 1,
                suggestedAmountCents = 2500,
            )),
        )
        compose.setContent {
            PagoControlTheme(false) {
                HomeScreen(state = state, name = "Prueba", onIntent = { intents.add(it) }, themeIndex = 1, onThemeSelected = {}, notificationAccessGranted = true)
            }
        }
        assertTrue(intents.isEmpty())
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Revisar movimiento"))
        compose.onNodeWithText("Revisar movimiento").performClick()
        compose.onNodeWithText("25.00").assertIsDisplayed()
        compose.onNodeWithText("Confirmar ingreso").performClick()
        compose.runOnIdle {
            assertEquals(listOf(HomeUiIntent.ConfirmPayment(id = "test-payment", amountCents = 2500)), intents)
        }
    }
}
