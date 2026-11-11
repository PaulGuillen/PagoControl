package com.devpaul.pagocontrol

import android.app.Application
import com.devpaul.pagocontrol.platform.log.TimberFactory
import com.devpaul.pagocontrol.platform.analytics.AppAnalytics
import com.devpaul.pagocontrol.platform.analytics.FirebaseAnalyticsReporter
import com.devpaul.pagocontrol.platform.firebase.AppCheckProviderInstaller
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TimberFactory.setup()
        AppAnalytics.initialize(
            FirebaseAnalyticsReporter(
                context = this,
                enabled = !BuildConfig.DEBUG && BuildConfig.FIREBASE_EMULATOR_HOST.isBlank(),
                environment = BuildConfig.ENVIRONMENT,
            ),
        )
        AppCheckProviderInstaller.install(this, useEmulators = BuildConfig.FIREBASE_EMULATOR_HOST.isNotBlank())
        koinInitializer()
    }

    private fun koinInitializer() = startKoin {
        androidContext(this@AndroidApplication)
        androidLogger()
        modules(AppModule().module)
    }
}
