package com.huaguang.flowoftime.ui.state

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.data.sources.SPHelper
import javax.inject.Inject

class SharedState @Inject constructor(
    val application: Application,
    val spHelper: SPHelper,
) {
    // TODO: 这两个状态已经没用了
    val newEventName = mutableStateOf("")
    val isInputShow = mutableStateOf(false)

    val scrollIndex = mutableStateOf(0)
    var eventCount = 0

    val toastMessage = MutableLiveData<String>()
    val dialogShow = mutableStateOf(false)

    // 指示当前最近的正在进行的事项的类型，null 代表当前没有事项正在进行
    val cursorType = spHelper.getCursorType()

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