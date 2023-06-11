package com.huaguang.flowoftime.ui.components.event_buttons

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventButtonsViewModel @Inject constructor(
    private val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private var currentStatus
        get() = sharedState.eventStatus.value
        set(value) {
            sharedState.eventStatus.value = value
        }

    // 专用
    val mainButtonText = mutableStateOf("开始")
    val subButtonText = mutableStateOf("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)

    fun toggleStateOnMainStart() {
        currentStatus = EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS
        mainButtonText.value = "结束"
        subButtonShow.value = true
    }

    fun toggleStateOnMainStop() {
        currentStatus = EventStatus.NO_EVENT_IN_PROGRESS
        mainButtonText.value = "开始"
        subButtonShow.value = false
    }

    fun toggleStateOnSubInsert() {
        currentStatus = EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS
        subButtonText.value = "插入结束"
        mainButtonShow.value = false
    }

    fun toggleStateOnSubStop() {
        currentStatus = EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS
        subButtonText.value = "插入"
        mainButtonShow.value = true
    }

    fun restoreButtonShow() {
        if (mainButtonText.value == "开始") return

        if (subButtonText.value == "插入结束") {
            mainButtonShow.value = false
        }

        subButtonShow.value = true

    }

    fun updateStateOnGetUpConfirmed() {
        // 按钮文本直接还原为开始，不需要结束
        mainButtonText.value = "开始"
        // 比较特殊，插入按钮不需要显示
        subButtonShow.value = false
    }


}