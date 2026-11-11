package com.devpaul.pagocontrol.features.auth

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import org.koin.core.annotation.Single

@Module
@ComponentScan
class AuthModule {
    @Single
    fun firebaseApp(context: Context): FirebaseApp {
        if (BuildConfig.FIREBASE_EMULATOR_HOST.isBlank()) return FirebaseApp.getInstance()
        val name = "pagocontrol-emulators"
        return FirebaseApp.getApps(context).firstOrNull { it.name == name } ?: FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder(FirebaseApp.getInstance().options)
                .setProjectId("demo-pagocontrol")
                .setApiKey("fake-api-key")
                .build(),
            name,
        )
    }

    @Single
    fun firebaseAuth(app: FirebaseApp): FirebaseAuth = FirebaseAuth.getInstance(app).apply {
        setLanguageCode("es")
        if (BuildConfig.FIREBASE_EMULATOR_HOST.isNotBlank()) useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 9099)
    }

    @Single
    fun firebaseFirestore(app: FirebaseApp): FirebaseFirestore = FirebaseFirestore.getInstance(app).apply {
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build()).build()
        if (BuildConfig.FIREBASE_EMULATOR_HOST.isNotBlank()) useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 8080)
    }
}
