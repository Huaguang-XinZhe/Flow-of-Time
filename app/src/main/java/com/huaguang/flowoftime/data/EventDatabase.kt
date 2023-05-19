package com.huaguang.flowoftime.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Event::class], version = 1)
@TypeConverters(LocalDateTimeConverter::class, DurationConverter::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

