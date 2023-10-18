package com.huaguang.flowoftime.custom_interface

import com.huaguang.flowoftime.EventType
import java.time.LocalDateTime

interface EventControl {
    suspend fun startEvent(
        startTime: LocalDateTime = LocalDateTime.now(),
        name: String = "",
        eventType: EventType = EventType.SUBJECT,
        category: String? = null,
    )

    suspend fun stopEvent(eventType: EventType = EventType.SUBJECT)

}