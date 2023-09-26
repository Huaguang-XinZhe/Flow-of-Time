package com.huaguang.flowoftime.data.models.db_returns

import java.time.Duration
import java.time.LocalDate

data class DateDuration(
    val date: LocalDate,
    val totalDuration: Duration,
)
