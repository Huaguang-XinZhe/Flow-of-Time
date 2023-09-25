package com.huaguang.flowoftime.data.models.db_returns

import java.time.LocalDateTime

data class KeyTimePoints(
    val wakeUpTime: LocalDateTime?,
    val sleepTime: LocalDateTime?,
    val nextWakeUpTime: LocalDateTime?,
)
