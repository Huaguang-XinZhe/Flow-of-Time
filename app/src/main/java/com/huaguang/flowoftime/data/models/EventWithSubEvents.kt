package com.huaguang.flowoftime.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithSubEvents(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentEventId"
    )
    val subEvents: List<Event>
)