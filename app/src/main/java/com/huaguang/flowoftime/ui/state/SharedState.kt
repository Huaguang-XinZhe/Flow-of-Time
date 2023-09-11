package com.huaguang.flowoftime.ui.state

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.data.models.tables.Event

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