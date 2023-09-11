package com.huaguang.flowoftime.data.models.db_returns

import java.time.LocalDateTime

data class StopRequired(
    val startTime: LocalDateTime,
    val pauseInterval: Int,
)
