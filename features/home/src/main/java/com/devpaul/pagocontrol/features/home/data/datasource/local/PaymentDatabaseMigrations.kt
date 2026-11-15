package com.devpaul.pagocontrol.features.home.data.datasource.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object PaymentDatabaseMigrations {
    val FROM_1_TO_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE payment_notifications_new (id TEXT NOT NULL, packageName TEXT NOT NULL, source TEXT NOT NULL, title TEXT NOT NULL, text TEXT NOT NULL, postedAt INTEGER NOT NULL, suggestedAmountCents INTEGER, amountCents INTEGER, status TEXT NOT NULL, ownerId TEXT NOT NULL, pendingWriteId TEXT, remoteRevision INTEGER NOT NULL, PRIMARY KEY(ownerId, id))")
            db.execSQL("INSERT INTO payment_notifications_new SELECT id, packageName, source, title, text, postedAt, suggestedAmountCents, amountCents, status, '', lower(hex(randomblob(16))), 0 FROM payment_notifications")
            db.execSQL("DROP TABLE payment_notifications")
            db.execSQL("ALTER TABLE payment_notifications_new RENAME TO payment_notifications")
            db.execSQL("CREATE TABLE notification_sources_new (packageName TEXT NOT NULL, ownerId TEXT NOT NULL, PRIMARY KEY(ownerId, packageName))")
            db.execSQL("INSERT INTO notification_sources_new SELECT packageName, '' FROM notification_sources")
            db.execSQL("DROP TABLE notification_sources")
            db.execSQL("ALTER TABLE notification_sources_new RENAME TO notification_sources")
            db.execSQL("CREATE TABLE payment_sync_state (ownerId TEXT NOT NULL, revision INTEGER NOT NULL, initialDownloadComplete INTEGER NOT NULL, PRIMARY KEY(ownerId))")
        }
    }
}
