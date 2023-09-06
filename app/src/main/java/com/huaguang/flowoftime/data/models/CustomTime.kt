package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.TimeType
import java.time.LocalDateTime

data class CustomTime(
    val type: TimeType,
    var timeState: MutableState<LocalDateTime?>,
    var initialTime: LocalDateTime? = null
)
