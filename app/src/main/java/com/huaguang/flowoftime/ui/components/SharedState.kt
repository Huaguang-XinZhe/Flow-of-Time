package com.huaguang.flowoftime.ui.components

import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.EventStatus

class SharedState {

    val isInputShow = mutableStateOf(false)
    val newEventName = mutableStateOf("")
    val scrollIndex = mutableStateOf(0)
    var eventCount = 0
    val eventStatus = mutableStateOf(EventStatus.NO_EVENT_IN_PROGRESS)

    fun updateStateOnStart() {
        isInputShow.value = true
        newEventName.value = ""
        // 更新事件数量
        eventCount++
        // 更新滚动索引
        scrollIndex.value = eventCount - 1
    }

    fun resetInputState() {
        if (isInputShow.value) {
            isInputShow.value = false
            newEventName.value = ""
        }
    }

}