package com.huaguang.flowoftime.data

import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.data.models.Event
import java.time.Duration

data class SPData(
    val isOneDayButtonClicked: Boolean,
    val isInputShow: Boolean,
    val buttonText: String,
    val subButtonText: String,
    val coreDuration: Duration,
    val eventStatus: EventStatus,
    val currentEvent: Event?,
    val scrollIndex: Int,
)
