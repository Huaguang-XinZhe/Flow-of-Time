package com.huaguang.flowoftime.data

import com.huaguang.flowoftime.data.models.Event
import java.time.Duration
import java.time.LocalDateTime

data class SPData(
    val isOneDayButtonClicked: Boolean,
    val isInputShow: Boolean,
    val buttonText: String,
    val subButtonText: String,
    val isTracking: Boolean,
    val isCoreEventTracking: Boolean,
    val coreDuration: Duration,
    val startTimeTracking: LocalDateTime?,
    val currentEvent: Event?,
    val incompleteMainEvent: Event?,
    val scrollIndex: Int,
    val subButtonClickCount: Int,
    val isSubEventType: Boolean,
    val isLastStopFromSub: Boolean,
    val isCoreDurationReset: Boolean
)
