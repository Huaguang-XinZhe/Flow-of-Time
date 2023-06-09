package com.huaguang.flowoftime.ui.components

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventStatus

class SharedState(val application: Application) {

    val isInputShow = mutableStateOf(false)
    val newEventName = mutableStateOf("")
    val scrollIndex = mutableStateOf(0)
    var eventCount = 0
    val eventStatus = mutableStateOf(EventStatus.NO_EVENT_IN_PROGRESS)
    val toastMessage = MutableLiveData<String>()

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