package com.huaguang.flowoftime.ui.state

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.tables.Event

class SharedState(val application: Application) {
    // TODO: 这两个状态已经没用了
    val newEventName = mutableStateOf("")
    val isInputShow = mutableStateOf(false)

    val scrollIndex = mutableStateOf(0)
    var eventCount = 0

    var currentEvent: Event? = null
    val toastMessage = MutableLiveData<String>()
    val dialogShow = mutableStateOf(false)

    // TODO: 这两个很关键，应当存起来（优化 stepTiming，保留 cursorType，EventStatus 能不能优化？）
    val eventStatus = mutableStateOf(EventStatus.NO_EVENT)
    val cursorType = mutableStateOf<EventType?>(null) // 指示当前最近的正在进行的事项的类型，null 代表当前没有事项正在进行

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