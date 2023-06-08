package com.huaguang.flowoftime.ui.components.event_buttons

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventButtonsViewModel @Inject constructor(
    sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private var eventType = sharedState.eventType.value

    // 专用
    val mainEventButtonText = mutableStateOf("开始")
    val subEventButtonText = mutableStateOf("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)


    fun toggleButtonStateStopped() {
        if (eventType == EventType.SUB) {
            toggleSubButtonState("插入结束")
        } else {
            toggleMainButtonState("结束")
        }
    }

    fun toggleMainButtonState(buttonText: String) {
        when (buttonText) {
            "开始" -> {
                mainEventButtonText.value = "结束"
                subButtonShow.value = true
            }
            "结束" -> {
                mainEventButtonText.value = "开始"
                subButtonShow.value = false
            }
        }
    }

    fun toggleSubButtonState(buttonText: String) {
        when (buttonText) {
            "插入" -> {
                eventType = EventType.SUB
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                // 不能放在这里，stop 里边的协程会挂起，这一段会先执行，必须放入 stop 里边
//                eventTypeState.value = EventType.MAIN
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
            }
        }
    }

    fun restoreButtonShow() {
        if (mainEventButtonText.value == "开始") return

        if (subEventButtonText.value == "插入结束") {
            Log.i("打标签喽", "插入结束部分恢复！")
            mainButtonShow.value = false
        }

        subButtonShow.value = true

    }

    fun updateStateOnGetUpConfirmed() {
        // 按钮文本直接还原为开始，不需要结束
        mainEventButtonText.value = "开始"
        // 比较特殊，插入按钮不需要显示
        subButtonShow.value = false
    }


}