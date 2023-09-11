package com.huaguang.flowoftime.data.models.db_returns

import java.time.Duration
import java.time.LocalDateTime

data class InsertParent(
    val endTime: LocalDateTime?,
    val duration: Duration,
)
