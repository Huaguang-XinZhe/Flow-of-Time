package com.huaguang.flowoftime.data.models

import java.time.Duration

data class DurationReturns(
    val newDuration: Duration?,
    val newDurationsMap: Map<Long, Duration>?,
    val deltaDurationMap: Map<Long, Duration>?,
)
