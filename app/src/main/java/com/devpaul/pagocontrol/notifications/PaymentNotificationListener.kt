package com.devpaul.pagocontrol.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.devpaul.pagocontrol.domain.PaymentAmount
import com.devpaul.pagocontrol.domain.PaymentNotification
import com.devpaul.pagocontrol.domain.PaymentNotificationRepository
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PaymentNotificationListener : NotificationListenerService(), KoinComponent {
    private val repository: PaymentNotificationRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(notification: StatusBarNotification) {
        if (notification.packageName == packageName) return
        val payload = notification.notification
        if (payload.flags and Notification.FLAG_GROUP_SUMMARY != 0 || payload.flags and Notification.FLAG_ONGOING_EVENT != 0) return
        val extras = payload.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty().take(200)
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty().ifBlank {
                extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString("\n").orEmpty()
            }.take(1000)
        if (title.isBlank() && text.isBlank()) return
        val source = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(notification.packageName, 0)).toString()
        }.getOrDefault(notification.packageName)
        val identity = "${notification.key}|${notification.postTime}"
        val id = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
        scope.launch {
            try {
                repository.capture(PaymentNotification(
                    id = id, packageName = notification.packageName, source = source,
                    title = title, text = text, postedAt = notification.postTime,
                    suggestedAmountCents = PaymentAmount.suggest("$title $text"),
                ))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                android.util.Log.e("PaymentNotifications", "No se pudo guardar la notificación local")
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
