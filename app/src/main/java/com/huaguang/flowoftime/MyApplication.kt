package com.huaguang.flowoftime

import android.app.Application
import androidx.room.Room
import com.huaguang.flowoftime.data.UserDatabase

class MyApplication: Application() {
    lateinit var database: UserDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "user_database"
        ).build()
    }
}