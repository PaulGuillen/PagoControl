package com.example.androidbase

import android.app.Application
import com.example.androidbase.data.SessionLifecycle
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.annotation.KoinInternalApi
import org.koin.ksp.generated.module
import timber.log.Timber

class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        if (BuildConfig.FIREBASE_ENABLED) FirebaseBootstrap.initialize(this)
        koinInitializer()
    }

    @OptIn(KoinInternalApi::class)
    private fun koinInitializer() = startKoin {
        androidContext(this@AndroidApplication)
        androidLogger()
        properties(mapOf("firebase.enabled" to BuildConfig.FIREBASE_ENABLED))
        modules(AppModule().module)
    }.also { application ->
        application.koin.extensionManager.registerExtension(
            "session-lifecycle",
            application.koin.get<SessionLifecycle>(),
        )
    }
}
