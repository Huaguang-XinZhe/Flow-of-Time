package com.huaguang.flowoftime.ui.pages.time_record.event_buttons

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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

    private val _eventLiveData = MutableLiveData<Event>()
    val eventLiveData: LiveData<Event> get() = _eventLiveData

    val mainButtonText = mutableStateOf("开始")
    val subButtonText = mutableStateOf("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)
    val undoFilledIconShow = mutableStateOf(false)

    fun toggleStateOnMainStop() {
        currentStatus = EventStatus.NO_EVENT_IN_PROGRESS
        mainButtonText.value = "开始"
        subButtonShow.value = false
        undoFilledIconShow.value = true
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

    fun onMainButtonClick(eventControl: EventControl, selectedTime: MutableState<LocalDateTime?>?) {
        selectedTime?.value = null // 取消选中状态

        when (mainButtonText.value) {
            "开始" -> {
                toggleStateOnMainStart()
                eventControl.startEvent(eventType = EventType.SUBJECT)
            }
            "结束" -> {
                viewModelScope.launch {
                    eventControl.stopEvent()
                    toggleStateOnMainStop()
                }
            }
        }
    }

    fun onMainButtonLongClick(eventControl: EventControl) {
        if (mainButtonText.value == "结束") return

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val startTime = repository.getOffsetStartTime()

            if (startTime == null) {
                sharedState.toastMessage.value = "当前无法补计，直接开始吧"
                return@launch
            }

            eventControl.startEvent(startTime, EventType.SUBJECT)
            toggleStateOnMainStart()

            sharedState.toastMessage.value = "开始补计……"
        }
    }

    fun onSubButtonClick(eventControl: EventControl, selectedTime: MutableState<LocalDateTime?>?) {
        selectedTime?.value = null // 取消选中状态

        when (subButtonText.value) {
            "插入" -> {
                toggleStateOnSubInsert() // 这个必须放在前边，否则 start 逻辑会出问题
                eventControl.startEvent(eventType = EventType.INSERT)
            }
            "插入结束" -> {
                viewModelScope.launch {
                    eventControl.stopEvent()
                    toggleStateOnSubStop()
                }
            }
        }
    }

    fun onSubButtonLongClick(eventControl: EventControl) {
        if (subButtonText.value == "插入") return

        viewModelScope.launch {
            // 结束子事件————————————————
            eventControl.stopEvent()
            toggleStateOnSubStop()

            // 结束主事件————————————————
            eventControl.stopEvent()
            toggleStateOnMainStop()

        }

        sharedState.toastMessage.value = "全部结束！"
    }

    fun onUndoFilledClick() {
        viewModelScope.launch {
            val event = getJustEndedEvent()

            // 交由 PageViewModel 去观察。
            // 利用观察者模式，就不需要引用其 currentEvent 依赖，只需要在值变化时通知它，然后处理。通知的同时会传入最新值。
            _eventLiveData.value = event.copy(
                endTime = null,
                duration = null
            )

            repository.deleteEvent(event.id)

            // 返回原先的结束状态
            if (event.parentEventId == null) {
                toggleStateOnMainStart()
            } else {
                toggleStateOnSubInsert()
            }
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

    private fun toggleStateOnMainStart() {
        currentStatus = EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS
        mainButtonText.value = "结束"
        subButtonShow.value = true
        undoFilledIconShow.value = false
    }

    private fun toggleStateOnSubInsert() {
        currentStatus = EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS
        subButtonText.value = "插入结束"
        mainButtonShow.value = false
        undoFilledIconShow.value = false
    }


}

interface EventControl {
    fun startEvent(
        startTime: LocalDateTime = LocalDateTime.now(),
        eventType: EventType = EventType.SUBJECT
    )

    fun stopEvent()

}