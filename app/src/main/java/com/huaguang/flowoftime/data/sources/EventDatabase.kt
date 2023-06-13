package com.huaguang.flowoftime.data.sources

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huaguang.flowoftime.data.DurationConverter
import com.huaguang.flowoftime.data.LocalDateTimeConverter
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.DateDuration
import com.huaguang.flowoftime.data.models.Event

@Database(entities = [Event::class, DateDuration::class], version = 1, exportSchema = false)
@TypeConverters(
    LocalDateTimeConverter::class, DurationConverter::class,
    DurationConverter.LocalDateConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun dateDurationDao(): DateDurationDao
}

