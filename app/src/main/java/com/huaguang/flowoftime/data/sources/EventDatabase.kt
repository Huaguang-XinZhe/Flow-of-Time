package com.huaguang.flowoftime.data.sources

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huaguang.flowoftime.data.Converters
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.db_returns.DateDuration
import com.huaguang.flowoftime.data.models.tables.Event


@Database(
    entities = [Event::class, DateDuration::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun dateDurationDao(): DateDurationDao

}

