package com.devpaul.pagocontrol

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.home.domain.entity.Movement
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentNotification
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiIntent
import com.devpaul.pagocontrol.features.home.ui.home.components.MovementRow
import com.devpaul.pagocontrol.features.home.ui.notificationreview.components.PendingNotificationCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OwnTransferUiTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun pendingReviewExplainsOwnTransferWithoutOfferingIncomeOrExpense() {
        var received: HomeUiIntent? = null
        compose.setContent {
            PagoControlTheme(false) {
                PendingNotificationCard(
                    notification = PaymentNotification(
                        id = "transfer",
                        packageName = "com.google.android.gm",
                        source = "Gmail",
                        title = "Transfer receipt",
                        text = "Transferencia entre mis cuentas S/ 100.00",
                        postedAt = 1,
                        suggestedAmountCents = 10000,
                    ),
                    saving = false,
                    mail = true,
                    onIntent = { received = it },
                )
            }
        }
        compose.onNodeWithText("Transfer receipt").performClick()
        compose.onNodeWithText("Transferencia entre mis cuentas").performScrollTo().assertExists()
        compose.onNodeWithText("Confirmar ingreso").assertDoesNotExist()
        compose.onNodeWithText("Confirmar egreso").assertDoesNotExist()
        assertEquals(null, received)
    }

    @Test
    fun transferRowLabelsOnlyTheActualTransfer() {
        compose.setContent {
            PagoControlTheme(false) {
                MovementRow(
                    movement = Movement(
                        id = "transfer",
                        title = "Bank",
                        detail = "Today",
                        source = "Gmail",
                        amountCents = -10000,
                        ownTransfer = true
                    ),

                    )
            }
        }
        compose.onNodeWithText("Transferencia entre mis cuentas").assertExists()
        compose.onNodeWithText("Es una transferencia entre mis cuentas").assertDoesNotExist()
        compose.onNodeWithText("Confirmar egreso").assertDoesNotExist()
    }
}
