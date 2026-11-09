package com.example.androidbase.data

import com.example.androidbase.data.remote.FirebaseSessionRepository
import com.example.androidbase.session.SessionRepository
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.core.extension.KoinExtension

@Single
class SessionLifecycle(private val session: SessionRepository) : KoinExtension {
    override fun onRegister(koin: Koin) = Unit

    override fun onClose() {
        (session as? FirebaseSessionRepository)?.close()
    }
}
