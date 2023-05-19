package com.huaguang.flowoftime.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDateTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L, // Unique ID for the event
    var name: String, // Event name
    val startTime: LocalDateTime, // Event start time
    var endTime: LocalDateTime? = null, // Event end time (nullable, because an event can be ongoing)
    var duration: Duration? = null, // Duration of the event (nullable, because an event can be ongoing)
    var hasTriggeredReminder: Boolean = false // Whether a reminder has been triggered for this event
)

