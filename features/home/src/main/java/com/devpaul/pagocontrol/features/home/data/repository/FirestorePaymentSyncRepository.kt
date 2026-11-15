package com.devpaul.pagocontrol.features.home.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.SystemClock
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.remote.FirebasePaymentCloudStore
import com.devpaul.pagocontrol.features.home.data.sync.PaymentSyncEngine
import com.devpaul.pagocontrol.features.home.data.sync.RoomPaymentSyncLocalStore
import com.devpaul.pagocontrol.features.home.domain.entity.PaymentSyncState
import com.devpaul.pagocontrol.features.home.domain.repository.PaymentSyncRepository
import com.devpaul.pagocontrol.session.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.core.annotation.Single

@Single(binds = [PaymentSyncRepository::class])
class FirestorePaymentSyncRepository(
    database: PaymentDatabase,
    firestore: FirebaseFirestore,
    private val auth: AuthRepository,
    context: Context,
) : PaymentSyncRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(PaymentSyncState())
    override val state = mutableState.asStateFlow()
    private val wakeups = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val dao = database.notifications()
    private val engine = PaymentSyncEngine(
        local = RoomPaymentSyncLocalStore(dao),
        cloud = FirebasePaymentCloudStore(firestore = firestore, auth = auth),
        isCurrentOwner = { auth.session.value?.id == it },
    )

    init {
        context.getSystemService(ConnectivityManager::class.java).registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = requestSync()
            }
        )
        scope.launch {
            auth.session.map { it?.id }.distinctUntilChanged().collectLatest { uid ->
                mutableState.value = PaymentSyncState(ownerId = uid)
                if (uid == null) return@collectLatest
                coroutineScope {
                    val events = Channel<Unit>(Channel.CONFLATED)
                    launch { wakeups.collect { events.trySend(Unit) } }
                    launch {
                        dao.observeWriteTokens(uid).distinctUntilChanged()
                            .collect { events.trySend(Unit) }
                    }
                    launch {
                        dao.observePendingWriteCount(uid).collect { count ->
                            mutableState.update { it.copy(pendingWrites = count) }
                        }
                    }
                    events.trySend(Unit)
                    var lastRemoteCheck = -60_000L
                    var failures = 0
                    for (event in events) {
                        delay(350)
                        while (events.tryReceive().isSuccess) { /* Coalesce bursts of captured notifications. */
                        }
                        val checkRemote = SystemClock.elapsedRealtime() - lastRemoteCheck >= 60_000L
                        mutableState.update { it.copy(isSyncing = true) }
                        try {
                            withTimeout(45_000) {
                                engine.sync(
                                    ownerId = uid,
                                    checkRemote = checkRemote
                                )
                            }
                            if (checkRemote) lastRemoteCheck = SystemClock.elapsedRealtime()
                            failures = 0
                            val checkpoint = dao.syncCheckpoint(uid)
                            mutableState.update {
                                it.copy(
                                    initialDownloadComplete = checkpoint?.initialDownloadComplete == true,
                                    hasError = false
                                )
                            }
                        } catch (cancelled: CancellationException) {
                            if (cancelled !is TimeoutCancellationException) throw cancelled
                            mutableState.update { it.copy(hasError = true) }
                            if (++failures < 3) {
                                delay(2_000L * failures); events.trySend(Unit)
                            }
                        } catch (error: Exception) {
                            mutableState.update { it.copy(hasError = true) }
                            val permanent = error is IllegalArgumentException ||
                                    (error is FirebaseFirestoreException && error.code in setOf(
                                        FirebaseFirestoreException.Code.PERMISSION_DENIED,
                                        FirebaseFirestoreException.Code.UNAUTHENTICATED,
                                        FirebaseFirestoreException.Code.INVALID_ARGUMENT,
                                    ))
                            if (++failures < 3 && !permanent) {
                                delay(2_000L * failures); events.trySend(Unit)
                            }
                        } finally {
                            mutableState.update { it.copy(isSyncing = false) }
                        }
                    }
                }
            }
        }
    }

    override fun requestSync() {
        wakeups.tryEmit(Unit)
    }
}
