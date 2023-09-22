package com.huaguang.flowoftime.data.sources

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huaguang.flowoftime.data.Converters
import com.huaguang.flowoftime.data.dao.DailyStatisticsDao
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.models.tables.DateDuration
import com.huaguang.flowoftime.data.models.tables.Event


@Database(
    entities = [Event::class, DateDuration::class, DailyStatistics::class], // 新增表
    version = 2, // 新增表，必须更新
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EventDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    abstract fun dateDurationDao(): DateDurationDao

    abstract fun dailyStatisticsDao(): DailyStatisticsDao

}

