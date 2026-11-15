package com.devpaul.pagocontrol.features.home.data.sync

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.features.home.data.datasource.local.PaymentDatabase
import com.devpaul.pagocontrol.features.home.data.datasource.local.entity.NotificationEntity
import com.devpaul.pagocontrol.features.home.data.datasource.remote.FirebasePaymentCloudStore
import com.devpaul.pagocontrol.features.home.data.datasource.remote.PaymentCloudStore
import com.devpaul.pagocontrol.session.AuthRepository
import com.devpaul.pagocontrol.session.SignInResult
import com.devpaul.pagocontrol.session.UserSession
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Runs only against Firebase emulators, with an independent app and in-memory Room databases. */
@RunWith(AndroidJUnit4::class)
class FirestorePaymentSyncIntegrationTest {
    @Test
    fun bootstrapIncrementalWritesAndDiscardWorkWithRealFirestoreSdk() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val app = requireNotNull(
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder().setProjectId("demo-pagocontrol-sync")
                    .setApplicationId("1:123:android:sync-test").setApiKey("fake-api-key").build(),
                "sync-test-${UUID.randomUUID()}",
            )
        )
        val firebaseAuth = FirebaseAuth.getInstance(app).apply { useEmulator("127.0.0.1", 9199) }
        val firestore = FirebaseFirestore.getInstance(app).apply { useEmulator("127.0.0.1", 8180) }
        val first = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        val second = Room.inMemoryDatabaseBuilder(context, PaymentDatabase::class.java).build()
        try {
            val uid = requireNotNull(firebaseAuth.signInAnonymously().await().user).uid
            val auth = object : AuthRepository {
                override val session = MutableStateFlow<UserSession?>(
                    UserSession(
                        id = uid,
                        name = "Test",
                        email = "test@example.com"
                    )
                )

                override suspend fun signIn(email: String, password: String) = SignInResult.Success
                override suspend fun signOut() {
                    session.value = null
                }
            }
            val cloud = CountingCloud(FirebasePaymentCloudStore(firestore = firestore, auth = auth))
            fun engine(database: PaymentDatabase) = PaymentSyncEngine(
                local = RoomPaymentSyncLocalStore(database.notifications()), cloud = cloud,
                isCurrentOwner = { auth.session.value?.id == it },
            )

            fun row(id: String, amount: Long) = NotificationEntity(
                id = id, packageName = "com.google.android.gm", source = "Gmail", title = "Bank",
                text = "Receipt", postedAt = 123, suggestedAmountCents = amount,
                amountCents = amount, status = "confirmed", ownerId = uid,
            )

            val firstEngine = engine(first)
            val secondEngine = engine(second)
            firstEngine.sync(ownerId = uid, checkRemote = true)
            first.notifications().insert(row(id = "income", amount = 100))
            firstEngine.sync(ownerId = uid, checkRemote = false)
            assertEquals(0, cloud.pageReads)
            assertTrue(first.notifications().pendingWrites(uid).isEmpty())
            secondEngine.sync(ownerId = uid, checkRemote = true)
            assertEquals(
                100L,
                second.notifications().find(id = "income", ownerId = uid)?.amountCents
            )
            val initialReads = cloud.pageReads
            secondEngine.sync(ownerId = uid, checkRemote = true)
            assertEquals(initialReads, cloud.pageReads)
            first.notifications().insert(row(id = "expense", amount = -200))
            firstEngine.sync(ownerId = uid, checkRemote = false)
            secondEngine.sync(ownerId = uid, checkRemote = true)
            assertEquals(
                -200L,
                second.notifications().find(id = "expense", ownerId = uid)?.amountCents
            )
            first.notifications().discard(id = "income", ownerId = uid)
            firstEngine.sync(ownerId = uid, checkRemote = false)
            secondEngine.sync(ownerId = uid, checkRemote = true)
            assertEquals(
                "discarded",
                second.notifications().find(id = "income", ownerId = uid)?.status
            )
            assertEquals(1, second.notifications().confirmed(mail = true, ownerId = uid).size)
            assertEquals(3L, second.notifications().syncCheckpoint(uid)?.revision)
        } finally {
            first.close()
            second.close()
            firestore.terminate().await()
            app.delete()
        }
    }

    private class CountingCloud(private val delegate: PaymentCloudStore) :
        PaymentCloudStore by delegate {
        var pageReads = 0
        override suspend fun changes(
            ownerId: String,
            after: Long,
            through: Long,
            limit: Int
        ): List<NotificationEntity> {
            pageReads++
            return delegate.changes(
                ownerId = ownerId,
                after = after,
                through = through,
                limit = limit
            )
        }
    }
}
