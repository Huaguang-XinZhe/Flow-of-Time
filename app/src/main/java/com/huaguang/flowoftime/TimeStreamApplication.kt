package com.huaguang.flowoftime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.sources.EventDatabase
import com.huaguang.flowoftime.data.sources.IconDatabase
import dagger.hilt.android.HiltAndroidApp
import java.io.FileOutputStream
import javax.inject.Inject

@HiltAndroidApp
class TimeStreamApplication @Inject constructor() : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "my_service_channel"

        // Migration
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // We add the new column to the table
//                database.execSQL("ALTER TABLE date_durations ADD COLUMN durationStr TEXT NOT NULL DEFAULT ''")
//            }
//        }
    }

    val eventDB: EventDatabase by lazy {
        Room.databaseBuilder(
            this,
            EventDatabase::class.java,
            "event_database"
        )
//            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val iconDB: IconDatabase
        get() = _iconDB
    private lateinit var _iconDB: IconDatabase

    override fun onCreate() {
        super.onCreate()

        initializeIconDB()
        createNotificationChannel()

        RDALogger.start("打标签喽").enableLogging(true)

    }

    private fun createNotificationChannel() {
        val channelName = "My Service Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun initializeIconDB() {
        val dbFile = this.getDatabasePath("icon_mapping.db")

        // Step 1: Check if the database already exists
        if (!dbFile.exists()) {
            // Step 2: Copy database from assets if it doesn't exist
            copyDatabaseFromAssets()
        }

        // Step 3: Initialize Room database
        _iconDB = Room.databaseBuilder(
            this,
            IconDatabase::class.java, "icon_mapping.db"
        ).build()
    }

    private fun copyDatabaseFromAssets() {
        val assetManager = this.assets
        assetManager.open("icon_mapping.db").use { inputStream ->
            val outputFilePath = this.getDatabasePath("icon_mapping.db").path

            RDALogger.info("databasePath: $outputFilePath")

            FileOutputStream(outputFilePath).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }
                outputStream.flush()
            }
        }
    }

}

