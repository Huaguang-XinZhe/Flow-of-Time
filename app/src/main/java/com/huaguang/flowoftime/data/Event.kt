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
    var hasTriggeredReminder: Boolean = false, // TODO: 这个是 UI 状态，或许可以用 rememberSaveable 来存储（在进程重建时亦能保存） 
    var eventDate: LocalDate? = null // 新增字段，用于存储事件发生的日期
)
