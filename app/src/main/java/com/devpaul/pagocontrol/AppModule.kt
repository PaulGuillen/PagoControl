package com.devpaul.pagocontrol

import com.devpaul.pagocontrol.data.CoreDataModule
import com.devpaul.pagocontrol.features.auth.AuthModule
import com.devpaul.pagocontrol.features.home.HomeModule
import com.devpaul.pagocontrol.features.movements.MovementsModule
import com.devpaul.pagocontrol.features.profile.ProfileModule
import com.devpaul.pagocontrol.features.splash.SplashModule
import com.devpaul.pagocontrol.features.onboarding.OnboardingModule
import org.koin.core.annotation.ComponentScan
import com.devpaul.pagocontrol.navigation.NavigationModule
import com.devpaul.pagocontrol.session.SessionModule
import org.koin.core.annotation.Module

@Module(
    includes = [
        CoreDataModule::class,
        NavigationModule::class,
        SessionModule::class,
        AuthModule::class,
        HomeModule::class,
        MovementsModule::class,
        ProfileModule::class,
        SplashModule::class,
        OnboardingModule::class,
    ],
)
@ComponentScan
class AppModule
