package com.huaguang.flowoftime.data.models

import java.time.LocalDateTime

data class StopRequired(
    val startTime: LocalDateTime,
    val pauseInterval: Int,
)
