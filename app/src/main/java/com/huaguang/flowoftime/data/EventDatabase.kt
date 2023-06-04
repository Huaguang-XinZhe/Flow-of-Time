package com.huaguang.flowoftime.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Event::class, DateDuration::class], version = 1, exportSchema = false)
@TypeConverters(LocalDateTimeConverter::class, DurationConverter::class,
    DurationConverter.LocalDateConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun dateDurationDao(): DateDurationDao
}

