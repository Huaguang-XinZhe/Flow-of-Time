package com.huaguang.flowoftime.data.models.db_returns

import java.time.Duration
import java.time.LocalDateTime

data class InsertParent(
    val endTime: LocalDateTime?,
    val duration: Duration?, // 当 endTime 为 null 的时候，duration 也为 null
)
