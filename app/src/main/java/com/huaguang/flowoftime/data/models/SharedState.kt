package com.huaguang.flowoftime.data.models

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventStatus

class SharedState(val application: Application) {
    // TODO: 这两个状态已经没用了
    val newEventName = mutableStateOf("")
    val isInputShow = mutableStateOf(false)

    val scrollIndex = mutableStateOf(0)
    var eventCount = 0

    var currentEvent: Event? = null
    val eventStatus = mutableStateOf(EventStatus.NO_EVENT)
    val toastMessage = MutableLiveData<String>()
    val dialogShow = mutableStateOf(false)
    var autoId = 0L

    fun updateStateOnStart() {
//        isInputShow.value = true
//        newEventName.value = ""
        // 更新事件数量
        eventCount++
        // 更新滚动索引
        scrollIndex.value = eventCount - 1
    }

//    fun resetInputState() {
//        if (isInputShow.value) {
//            isInputShow.value = false
//            newEventName.value = ""
//        }
//    }

}