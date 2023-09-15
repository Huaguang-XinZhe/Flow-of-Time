package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeType
import java.time.LocalDateTime

data class CustomTime(
    val eventInfo: EventInfo,
    val type: TimeType,
    val initialTime: LocalDateTime? = null,
    val timeState: MutableState<LocalDateTime?>
)

data class EventInfo(
    val id: Long,
    val isTiming: Boolean, // 选中的 TimeLabel 所在的事件是否正在进行
    val parentId: Long?,
    val eventType: EventType,
)