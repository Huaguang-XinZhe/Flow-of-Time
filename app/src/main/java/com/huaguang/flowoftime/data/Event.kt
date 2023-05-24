package com.huaguang.flowoftime.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var name: String,
    val startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var duration: Duration? = null,
    var hasTriggeredReminder: Boolean = false,
    var eventDate: LocalDate? = null, // 用于存储事件发生的日期
    var parentId: Long? = null  // 用于存储该事件关联的主事件的ID，如果该事件是主事件，则此字段为null
)

