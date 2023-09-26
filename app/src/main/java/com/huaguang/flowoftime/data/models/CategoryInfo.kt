package com.huaguang.flowoftime.data.models

import java.time.Duration
import java.time.LocalDate

data class CategoryInfo(
    val previous: String?,
    val now: String?,
    val date: LocalDate,
    val duration: Duration,
)
