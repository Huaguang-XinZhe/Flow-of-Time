package com.huaguang.flowoftime.data.models.db_returns

import java.time.Duration
import java.time.LocalDate

data class EventCategoryInfo(
    val eventDate: LocalDate,
    val category: String?,
    val duration: Duration,
)
