package com.huaguang.flowoftime.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huaguang.flowoftime.utils.DurationSerializer
import com.huaguang.flowoftime.utils.LocalDateSerializer
import com.huaguang.flowoftime.utils.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var name: String = "",

    @Serializable(with = LocalDateTimeSerializer::class)
    var startTime: LocalDateTime,

    @Serializable(with = LocalDateTimeSerializer::class)
    var endTime: LocalDateTime? = null,

    @Serializable(with = DurationSerializer::class)
    var duration: Duration? = null,

    @Serializable(with = LocalDateSerializer::class)
    var eventDate: LocalDate? = null, // 用于存储事件发生的日期

    var parentId: Long? = null,   // 用于存储该事件关联的主事件的ID，如果该事件是主事件，则此字段为null

    var isCurrent: Boolean = false
)

