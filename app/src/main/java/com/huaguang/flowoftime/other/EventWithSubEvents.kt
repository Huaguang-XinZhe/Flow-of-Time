package com.huaguang.flowoftime.other

import androidx.room.Embedded
import androidx.room.Relation
import com.huaguang.flowoftime.data.models.tables.Event

data class EventWithSubEvents(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentEventId"
    )
    val subEvents: List<Event>
)