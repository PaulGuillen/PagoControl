package com.devpaul.cvfree.notifications

import android.Manifest
import com.devpaul.core.platform.analytics.AppAnalytics
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import com.devpaul.core.platform.locale.AppLanguageManager
import com.devpaul.cvfree.MainActivity
import com.devpaul.cvfree.R

class ResumeNotificationManager(private val context: Context) {
    val history = NotificationHistoryStore(
        context.getSharedPreferences("notification_history", Context.MODE_PRIVATE),
    )
    private val preferences = context.getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
    private val notifications = NotificationManagerCompat.from(context)

    var enabled: Boolean
        get() = preferences.getBoolean("enabled", true)
        set(value) {
            preferences.edit { putBoolean("enabled", value) }
            if (!value) notifications.cancel(SAVED_NOTIFICATION_ID)
        }

    var permissionRequested: Boolean
        get() = preferences.getBoolean("permission_requested", false)
        set(value) {
            preferences.edit { putBoolean("permission_requested", value) }
        }

    fun hasPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    fun canPostNotifications(): Boolean = hasPermission() && notifications.areNotificationsEnabled() &&
        (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            notifications.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localizedContext = AppLanguageManager.wrap(context)
            notifications.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    localizedContext.getString(R.string.notification_saved_channel),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = localizedContext.getString(R.string.notification_saved_channel_description)
                },
            )
        }
    }

    fun showResumeSaved() {
        if (!enabled || !canPostNotifications()) return
        ensureChannel()
        val localizedContext = AppLanguageManager.wrap(context)
        val openResumes = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_MY_CVS, true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            SAVED_NOTIFICATION_ID,
            openResumes,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_cv)
            .setContentTitle(localizedContext.getString(R.string.notification_resume_saved_title))
            .setContentText(localizedContext.getString(R.string.notification_resume_saved_body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        try {
            notifications.notify(SAVED_NOTIFICATION_ID, notification)
            AppAnalytics.outcome("saved_notification", "posted")
        } catch (error: SecurityException) {
            AppAnalytics.error("saved_notification", error)
            return
        }
    }

    fun settingsIntent(): Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (notifications.areNotificationsEnabled()) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
        } else {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
    }

    companion object {
        const val EXTRA_OPEN_MY_CVS = "com.devpaul.cvfree.OPEN_MY_CVS"
        private const val CHANNEL_ID = "saved_resumes"
        private const val SAVED_NOTIFICATION_ID = 1001
    }
}
