package com.huaguang.flowoftime.data.models.db_returns

import java.time.LocalDate
import java.time.LocalDateTime

data class StopRequire(
    val startTime: LocalDateTime,
    val eventDate: LocalDate,
    val category: String?,
)