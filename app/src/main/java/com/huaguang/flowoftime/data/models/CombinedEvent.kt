package com.huaguang.flowoftime.data.models

data class CombinedEvent(
    val event: Event,
    val contentEvents: List<CombinedEvent>
)