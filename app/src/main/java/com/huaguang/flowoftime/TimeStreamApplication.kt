package com.huaguang.flowoftime

import android.app.Application
import androidx.room.Room
import com.huaguang.flowoftime.data.EventDatabase

class TimeStreamApplication : Application() {

    val database: EventDatabase by lazy {
        Room.databaseBuilder(
            this,
            EventDatabase::class.java,
            "event_database"
        ).build()
    }

}
