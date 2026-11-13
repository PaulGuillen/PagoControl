package com.devpaul.pagocontrol.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.devpaul.pagocontrol.data.notifications.PaymentDatabase
import com.devpaul.pagocontrol.domain.GmailMonitoringRepository
import com.devpaul.pagocontrol.domain.GmailTransactionCandidate
import com.devpaul.pagocontrol.domain.PaymentAmount
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.domain.PaymentNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

/** Local fixtures for development builds. Not packaged in release. */
class SimulatePaymentReceiver : BroadcastReceiver(), KoinComponent {
    private val repository: PaymentNotificationRepository by inject()
    private val gmail: GmailMonitoringRepository by inject()
    private val database: PaymentDatabase by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val scenario = intent.getStringExtra("scenario") ?: "pair"
        val amount = intent.getStringExtra("amount") ?: "12.34"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (scenario == "clear") {
                    val ids = database.openHelper.readableDatabase.query(
                        "SELECT id FROM payment_notifications WHERE id LIKE 'debug-sim-%'"
                    ).use { cursor -> buildList { while (cursor.moveToNext()) add(cursor.getString(0)) } }
                    ids.forEach { repository.discard(it) }
                    pending.resultData = "Datos de simulacion retirados del balance y pendientes."
                    return@launch
                }
                require(scenario in setOf("bcp", "steam", "pair", "income", "rejected")) { "Escenario no valido" }
                require(PaymentAmount.parse(amount) != null) { "Importe no valido" }
                if (!gmail.observeGmailEnabled().first()) {
                    pending.resultData = "Activa Monitorear Gmail antes de simular."
                    return@launch
                }
                val batch = UUID.randomUUID().toString()
                suspend fun capture(key: String, title: String, text: String) {
                    repository.capture(PaymentNotification(
                        id = "debug-sim-$batch-$key",
                        packageName = GmailTransactionCandidate.PACKAGE_NAME,
                        source = "Gmail (prueba)", title = title, text = text,
                        postedAt = System.currentTimeMillis(), suggestedAmountCents = null,
                    ))
                }
                if (scenario == "bcp" || scenario == "pair") capture("bcp", "BCP Notificaciones",
                    "Realizaste un consumo de S/ $amount con tu Tarjeta de D\u00e9bito BCP en STEAM (PRUEBA).\nMonto\nTotal del consumo\nS/ $amount")
                if (scenario == "steam" || scenario == "pair") capture("steam", "Steam Support (prueba)",
                    "Gracias por tu compra de prueba.\nJuego de ejemplo\nTotal: S/ $amount")
                if (scenario == "income") capture("income", "Interbank (prueba)", "Transferencia recibida S/ $amount")
                if (scenario == "rejected") capture("rejected", "BCP Notificaciones", "Compra rechazada S/ $amount (prueba)")
                pending.resultData = "Simulacion $scenario enviada. Revisa Correo en PagoControl."
            } catch (error: Exception) {
                pending.resultCode = 1
                pending.resultData = error.message ?: "Error de simulacion"
            } finally {
                pending.finish()
            }
        }
    }
}
