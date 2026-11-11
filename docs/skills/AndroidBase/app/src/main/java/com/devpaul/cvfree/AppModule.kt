package com.devpaul.cvfree

import com.devpaul.core.data.CoreDataModule
import com.devpaul.features.auth.AuthModule
import com.devpaul.features.home.HomeModule
import com.devpaul.features.onboarding.OnboardingModule
import com.devpaul.features.resume.ResumeModule
import com.devpaul.features.settings.SettingsModule
import com.devpaul.features.splash.SplashModule
import com.devpaul.navigation.NavigationModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    [
        CoreDataModule::class,
        NavigationModule::class,
        AuthModule::class,
        HomeModule::class,
        OnboardingModule::class,
        ResumeModule::class,
        SettingsModule::class,
        SplashModule::class,
    ]
)

@ComponentScan
class AppModule
