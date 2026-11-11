package com.devpaul.pagocontrol.data

import com.devpaul.pagocontrol.data.local.DataStoreAppearanceRepository
import com.devpaul.pagocontrol.data.local.DataStoreProfileRepository
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.data.local.DemoHomeRepository

import com.devpaul.pagocontrol.domain.AppearanceRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.HomeRepository
import com.devpaul.pagocontrol.data.local.DataStoreStartupPreferences
import com.devpaul.pagocontrol.domain.StartupPreferences
import android.content.Context
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class CoreDataModule {
    @Single
    fun profileRepository(context: Context): ProfileRepository = DataStoreProfileRepository(context)

    @Single
    fun startupPreferences(context: Context): StartupPreferences = DataStoreStartupPreferences(context)

    @Single
    fun homeRepository(authRepository: AuthRepository): HomeRepository = DemoHomeRepository(authRepository)

    @Single
    fun appearanceRepository(context: Context): AppearanceRepository = DataStoreAppearanceRepository(context)
}
