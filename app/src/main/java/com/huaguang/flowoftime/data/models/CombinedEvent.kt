package com.huaguang.flowoftime.data.models

import com.huaguang.flowoftime.data.models.tables.Event

data class CombinedEvent(
    val event: Event,
    val contentEvents: List<CombinedEvent>
)