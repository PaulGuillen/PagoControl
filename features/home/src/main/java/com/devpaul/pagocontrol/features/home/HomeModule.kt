package com.devpaul.pagocontrol.features.home

import android.content.Context
import androidx.room.Room
import com.devpaul.pagocontrol.features.home.data.datasource.local.DataStoreHomeGuidePreferences
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabaseMigrations
import com.devpaul.pagocontrol.features.home.domain.repository.HomeGuidePreferences
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class HomeModule {
    @Single
    fun homeGuidePreferences(context: Context): HomeGuidePreferences =
        DataStoreHomeGuidePreferences(context)

    @Single
    fun paymentDatabase(context: Context): PaymentDatabase = Room.databaseBuilder(
        context = context,
        klass = PaymentDatabase::class.java,
        name = "payment_notifications.db",
    ).addMigrations(PaymentDatabaseMigrations.FROM_1_TO_2).build()
}
