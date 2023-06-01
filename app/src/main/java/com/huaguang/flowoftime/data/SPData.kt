package com.huaguang.flowoftime.data

import java.time.Duration

data class SPData(
    val isOneDayButtonClicked: Boolean,
    val isInputShow: Boolean,
    val buttonText: String,
    val subButtonText: String,
    val remainingDuration: Duration?,
    val isTracking: Boolean,
    val currentEvent: Event?,
    val incompleteMainEvent: Event?,
    val scrollIndex: Int,
    val subButtonClickCount: Int,
    val isSubEventType: Boolean,
    val isLastStopFromSub: Boolean
)
