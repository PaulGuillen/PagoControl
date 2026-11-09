package com.devpaul.pagocontrol.data

import com.devpaul.pagocontrol.data.local.DataStoreAppearanceRepository
import com.devpaul.pagocontrol.data.local.DemoAuthRepository
import com.devpaul.pagocontrol.data.local.DemoHomeRepository

import com.devpaul.pagocontrol.domain.AppearanceRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.domain.HomeRepository
import android.content.Context
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class CoreDataModule {
    @Single
    fun authRepository(): AuthRepository = DemoAuthRepository()

    @Single
    fun homeRepository(authRepository: AuthRepository): HomeRepository = DemoHomeRepository(authRepository)

    @Single
    fun appearanceRepository(context: Context): AppearanceRepository = DataStoreAppearanceRepository(context)
}
