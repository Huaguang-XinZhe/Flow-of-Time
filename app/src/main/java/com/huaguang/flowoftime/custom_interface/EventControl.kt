package com.huaguang.flowoftime.custom_interface

import com.huaguang.flowoftime.EventType
import java.time.LocalDateTime

interface EventControl {
    fun startEvent(
        startTime: LocalDateTime = LocalDateTime.now(),
        name: String = "",
        eventType: EventType = EventType.SUBJECT
    )

    fun stopEvent(eventType: EventType = EventType.SUBJECT)

}