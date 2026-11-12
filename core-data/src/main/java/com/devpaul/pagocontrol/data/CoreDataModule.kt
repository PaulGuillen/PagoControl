package com.devpaul.pagocontrol.data

import com.devpaul.pagocontrol.data.local.DataStoreAppearanceRepository
import com.devpaul.pagocontrol.data.local.DataStoreProfileRepository
import com.devpaul.pagocontrol.domain.ProfileRepository
import com.devpaul.pagocontrol.domain.HomeGuidePreferences
import com.devpaul.pagocontrol.data.local.DataStoreHomeGuidePreferences
import androidx.room.Room
import com.devpaul.pagocontrol.data.notifications.PaymentDatabase

import com.devpaul.pagocontrol.domain.AppearanceRepository
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
    fun homeGuidePreferences(context: Context): HomeGuidePreferences = DataStoreHomeGuidePreferences(context)

    @Single
    fun profileRepository(context: Context): ProfileRepository = DataStoreProfileRepository(context)

    @Single
    fun startupPreferences(context: Context): StartupPreferences = DataStoreStartupPreferences(context)

    @Single
    fun paymentDatabase(context: Context): PaymentDatabase = Room.databaseBuilder(
        context = context,
        klass = PaymentDatabase::class.java,
        name = "payment_notifications.db",
    ).build()

    @Single
    fun appearanceRepository(context: Context): AppearanceRepository = DataStoreAppearanceRepository(context)
}
