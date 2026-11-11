package com.devpaul.cvfree

import android.app.Application
import com.devpaul.core.platform.analytics.AppAnalytics
import com.devpaul.cvfree.analytics.FirebaseAnalyticsReporter
import com.devpaul.core.platform.log.TimberFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

class AndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        timberInitializer()
        AppAnalytics.initialize(FirebaseAnalyticsReporter(this))
        AppCheckProviderInstaller.install(this)
        koinInitializer()
    }

    private fun koinInitializer() = startKoin {
        androidContext(this@AndroidApplication)
        androidLogger()
        modules(AppModule().module)
    }

    private fun timberInitializer() {
        TimberFactory.setup()
    }
}
