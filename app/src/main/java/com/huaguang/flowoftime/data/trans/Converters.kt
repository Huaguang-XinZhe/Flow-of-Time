package com.huaguang.flowoftime.data.trans

import androidx.room.TypeConverter
import com.huaguang.flowoftime.EventType
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

object Converters {
    @TypeConverter
    fun fromEventType(type: EventType): String {
        return type.name
    }

    @TypeConverter
    fun toEventType(type: String): EventType {
        return EventType.valueOf(type)
    }

    @TypeConverter
    fun fromTags(tags: List<String>?): String? { // from 是存入数据库
        return tags?.joinToString(",")
    }

    @TypeConverter
    fun toTags(tags: String?): List<String>? { // to 是从数据库读取
        return tags?.split(",")
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return if (dateTimeString != null) {
            LocalDateTime.parse(dateTimeString)
        } else null
    }

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

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return if (dateString != null) {
            LocalDate.parse(dateString)
        } else null
    }


}

