package com.devpaul.pagocontrol.platform.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

object AppCheckProviderInstaller {
    fun install(context: Context, useEmulators: Boolean) {
        FirebaseApp.initializeApp(context)
        if (useEmulators) return
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
    }
}
