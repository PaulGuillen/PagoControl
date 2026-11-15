package com.devpaul.pagocontrol.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.devpaul.pagocontrol.MainActivity
import com.devpaul.pagocontrol.R
import com.devpaul.pagocontrol.designsystem.formatSoles
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.features.home.domain.entity.Movement
import com.devpaul.pagocontrol.features.home.domain.repository.MovementNotifier
import com.devpaul.pagocontrol.session.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single(binds = [MovementNotifier::class, LocalMovementNotifier::class])
class LocalMovementNotifier(
    private val context: Context,
    private val auth: AuthRepository,
    private val profiles: ProfileRepository,
) : MovementNotifier {
    private val manager = context.getSystemService(NotificationManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.movement_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.movement_channel_description) }
            )
        }
        scope.launch {
            auth.session.collectLatest { user ->
                manager.cancelAll()
                if (user != null) {
                    profiles.observe(user.id).catch { manager.cancelAll() }.collect { preferences ->
                        if (!preferences.notificationsEnabled) manager.cancelAll()
                    }
                }
            }
        }
    }

    override suspend fun notify(movement: Movement) {
        if (movement.amountCents == 0L) return
        val user = auth.session.value ?: return
        if (!profiles.observe(user.id).first().notificationsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        if (!manager.areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.getNotificationChannel(CHANNEL_ID)?.importance == NotificationManager.IMPORTANCE_NONE
        ) return
        val title = context.getString(
            if (movement.amountCents > 0) R.string.movement_income else R.string.movement_expense,
            formatSoles(movement.amountCents, signed = false),
        )
        val body = context.getString(
            R.string.movement_notification_detail,
            movement.source,
            movement.title
        )
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_movement_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_movement_notification)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.movement_notification_private))
                    .build()
            )
            .build()
        // Recheck after suspending for preferences, in case the account changed.
        if (auth.session.value?.id != user.id) return
        manager.notify("movement:${user.id}:${movement.id}", 0, notification)
    }

    private companion object {
        const val CHANNEL_ID = "recorded_movements"
    }
}
