package com.huaguang.flowoftime.data

import android.util.Log
import androidx.room.TypeConverter
import java.time.Duration
import java.time.LocalDateTime

class LocalDateTimeConverter {
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        Log.i("打标签喽", "fromLocalDateTime 内部执行！")
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return if (dateTimeString != null) {
            LocalDateTime.parse(dateTimeString)
        } else null
    }
}

class DurationConverter {
    @TypeConverter
    fun fromDuration(duration: Duration?): Long? {
        return duration?.toMillis()
    }

    @TypeConverter
    fun toDuration(durationMillis: Long?): Duration? {
        return if (durationMillis != null) {
            Duration.ofMillis(durationMillis)
        } else null
    }
}
