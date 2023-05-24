package com.huaguang.flowoftime.data

import androidx.room.Embedded
import androidx.room.Relation

data class EventWithSubEvents(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentId"
    )
    val subEvents: List<Event>
)