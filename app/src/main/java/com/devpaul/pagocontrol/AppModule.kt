package com.devpaul.pagocontrol

import com.devpaul.pagocontrol.data.CoreDataModule
import com.devpaul.pagocontrol.features.auth.AuthModule
import com.devpaul.pagocontrol.features.home.HomeModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        CoreDataModule::class,
        AuthModule::class,
        HomeModule::class,
    ],
)
@ComponentScan
class AppModule
