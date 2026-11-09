package com.example.androidbase

import android.content.Context
import com.example.androidbase.data.CoreDataModule
import com.example.androidbase.data.remote.NetworkModule
import com.example.androidbase.features.auth.AuthModule
import com.example.androidbase.features.home.HomeModule
import com.example.androidbase.platform.EventReporter
import com.example.androidbase.features.splash.SplashModule
import com.example.androidbase.features.onboarding.OnboardingModule
import org.koin.core.annotation.ComponentScan
import com.example.androidbase.navigation.NavigationModule
import com.example.androidbase.session.SessionModule
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        CoreDataModule::class,
        NavigationModule::class,
        SessionModule::class,
        NetworkModule::class,
        AuthModule::class,
        HomeModule::class,
        SplashModule::class,
        OnboardingModule::class,
    ],
)
@ComponentScan
class AppModule {
    @Single
    fun eventReporter(context: Context): EventReporter = if (BuildConfig.FIREBASE_ENABLED) {
        FirebaseEventReporter(context)
    } else {
        DebugEventReporter()
    }
}
