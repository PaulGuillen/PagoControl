package com.devpaul.features.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
class AuthModule {

    @Single
    fun firebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Single
    fun firebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
