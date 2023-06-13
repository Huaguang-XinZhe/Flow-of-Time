package com.huaguang.flowoftime.ui.components.event_buttons

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventButtonsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState
) : ViewModel() {

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
    val undoFilledIconShow = mutableStateOf(false)

    suspend fun onUndoFilledClicked(
        action: suspend (justEndedEvent: Event) -> Unit
    ) {
        val event = getJustEndedEvent()

        // 在这里要执行这个操作，但是缺少 currentEvent 这个其他 ViewModel 中的属性，
        // 于是就把 event 这个关键实例传出，让外界有 currentEvent 的地方去更改。
        action(event)

        // 返回原先的结束状态
        if (event.parentId == null) {
            toggleStateOnMainStart()
        } else {
            toggleStateOnSubInsert()
        }

        undoFilledIconShow.value = false
    }

    /**
     * 这个函数是从数据库中获取刚刚结束的事件，可能是主事件，也可能是子事件。
     */
    private suspend fun getJustEndedEvent(): Event {
        RDALogger.info("getJustEndedEvent 执行！")
        return if (mainButtonShow.value == true && mainButtonText.value == "开始") {
            RDALogger.info("从最近的主事件中获取")
            repository.getLastMainEvent()!! // 主事件刚结束，从最近的主事件获取
        } else repository.getLastEvent() // 子事件结束，获取最近的事件
    }

    fun toggleStateOnMainStart() {
        currentStatus = EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS
        mainButtonText.value = "结束"
        subButtonShow.value = true
        undoFilledIconShow.value = false
    }

    fun toggleStateOnMainStop() {
        currentStatus = EventStatus.NO_EVENT_IN_PROGRESS
        mainButtonText.value = "开始"
        subButtonShow.value = false
        undoFilledIconShow.value = true
    }

    fun toggleStateOnSubInsert() {
        currentStatus = EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS
        subButtonText.value = "插入结束"
        mainButtonShow.value = false
        undoFilledIconShow.value = false
    }

    fun toggleStateOnSubStop() {
        currentStatus = EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS
        subButtonText.value = "插入"
        mainButtonShow.value = true
        undoFilledIconShow.value = true
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