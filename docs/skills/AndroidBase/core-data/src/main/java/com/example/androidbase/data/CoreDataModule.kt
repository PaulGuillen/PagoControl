package com.example.androidbase.data

import android.content.Context
import com.example.androidbase.data.local.DataStorePreferences
import com.example.androidbase.data.local.DemoSessionRepository
import com.example.androidbase.data.local.LocalExampleRepository
import com.example.androidbase.data.remote.FirebaseSessionRepository
import com.example.androidbase.data.remote.FirestoreExampleRepository
import com.example.androidbase.domain.AddExampleUseCase
import com.example.androidbase.domain.AppPreferences
import com.example.androidbase.domain.ExampleRepository
import com.example.androidbase.session.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

@Module
@ComponentScan
class CoreDataModule {
    @Single
    fun preferences(context: Context): AppPreferences = DataStorePreferences(context)

    @Factory
    fun addExample(repository: ExampleRepository): AddExampleUseCase = AddExampleUseCase(repository)

    @Single
    fun sessionRepository(
        @Property("firebase.enabled") firebaseEnabled: Boolean,
        firebaseAuth: Lazy<FirebaseAuth>,
    ): SessionRepository = if (firebaseEnabled) {
        FirebaseSessionRepository(firebaseAuth.value)
    } else {
        DemoSessionRepository()
    }

    @Single
    fun exampleRepository(
        context: Context,
        @Property("firebase.enabled") firebaseEnabled: Boolean,
        firestore: Lazy<FirebaseFirestore>,
    ): ExampleRepository = if (firebaseEnabled) {
        FirestoreExampleRepository(firestore.value)
    } else {
        LocalExampleRepository(context)
    }

    @Single
    fun firebaseAuth(@Property("firebase.enabled") firebaseEnabled: Boolean): FirebaseAuth {
        check(firebaseEnabled) { "Firebase está desactivado" }
        return FirebaseAuth.getInstance()
    }

    @Single
    fun firebaseFirestore(@Property("firebase.enabled") firebaseEnabled: Boolean): FirebaseFirestore {
        check(firebaseEnabled) { "Firebase está desactivado" }
        return FirebaseFirestore.getInstance()
    }

    @Single
    fun firebaseFunctions(@Property("firebase.enabled") firebaseEnabled: Boolean): FirebaseFunctions {
        check(firebaseEnabled) { "Firebase está desactivado" }
        return FirebaseFunctions.getInstance("us-central1")
    }
}
