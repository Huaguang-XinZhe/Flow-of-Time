package com.huaguang.flowoftime.ui.pages.time_record.event_buttons

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventButtonsViewModel @Inject constructor(
    private val repository: EventRepository,
    spHelper: SPHelper,
    private val sharedState: SharedState,
    val buttonsState: ButtonsState,
) : ViewModel() {

    private var currentStatus
        get() = sharedState.eventStatus.value
        set(value) {
            sharedState.eventStatus.value = value
        }

    private val _eventLiveData = MutableLiveData<Event>()
    val eventLiveData: LiveData<Event> get() = _eventLiveData

    var stepTiming = false // 为了辅助 EventStatus 的确立

    init {
        stepTiming = spHelper.getStepTiming()
    }

    val buttonsStateControl = object : ButtonsStateControl {
        override fun toggleMainEnd() { // 按钮切换到主题事项结束的状态（说明主题事项正在进行）
            toggleStateOnMainStart()
        }

        override fun toggleSubEnd(type: EventType) { // 按钮切换到子项结束的状态（说明子项正在进行）
            currentStatus = EventStatus.SUB_TIMING
            
            buttonsState.apply {
                undoShow.value = false

                if (type == EventType.FOLLOW) { // 伴随事件正在进行
                    mainShow.value = false
                    subText.value = "伴随结束"
                } else if (type == EventType.STEP) { // 步骤正在进行
                    mainText.value = "步骤结束"
                    subText.value = "插入"
                    
                    stepTiming = true
                }
            }
        }

        override fun hasSubjectExist() = currentStatus != EventStatus.NO_EVENT
    }

    fun toggleStateOnMainStop() {
        currentStatus = EventStatus.NO_EVENT
        
        buttonsState.apply {
            mainText.value = "开始"
            subShow.value = false
            undoShow.value = true
        }
    }

    private fun insertOrFollowEnd() {
        currentStatus = if (stepTiming) EventStatus.SUB_TIMING else EventStatus.SUBJECT_ONLY

        buttonsState.apply {
            subText.value = "插入"
            mainShow.value = true
            undoShow.value = true
        }
    }

    fun restoreButtonShow() {
        buttonsState.apply {
            if (mainText.value == "开始") return

            if (subText.value == "插入结束") {
                mainShow.value = false
            }

            subShow.value = true
        }
    }

    fun updateStateOnGetUpConfirmed() {
        buttonsState.apply {
            // 按钮文本直接还原为开始，不需要结束
            mainText.value = "开始"
            // 比较特殊，插入按钮不需要显示
            subShow.value = false
        }
    }

    fun onMainButtonClick(eventControl: EventControl, selectedTime: MutableState<LocalDateTime?>?) {
        selectedTime?.value = null // 取消选中状态

        when (buttonsState.mainText.value) {
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
            "步骤结束" -> {
                viewModelScope.launch {
                    eventControl.stopEvent(eventType = EventType.STEP)
                    toggleStateOnMainStart()
                    stepTiming = false // 结束时重置
                }
            }
        }
    }

    fun onMainButtonLongClick(eventControl: EventControl) {
        if (buttonsState.mainText.value == "结束") return

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val startTime = repository.getOffsetStartTime()

            if (startTime == null) { //
                sharedState.toastMessage.value = "当前无法补计，直接开始吧"
                return@launch
            }

            eventControl.startEvent(startTime = startTime, eventType = EventType.SUBJECT)
            toggleStateOnMainStart()

            sharedState.toastMessage.value = "开始补计……"
        }
    }

    fun onSubButtonClick(eventControl: EventControl, selectedTime: MutableState<LocalDateTime?>?) {
        selectedTime?.value = null // 取消选中状态

        when (buttonsState.subText.value) {
            "插入" -> {
                toggleStateOnSubInsert() // 这个必须放在前边，否则 start 逻辑会出问题
                eventControl.startEvent(eventType = EventType.INSERT)
            }
            "插入结束" -> {
                viewModelScope.launch {
                    eventControl.stopEvent(eventType = EventType.INSERT)
                    insertOrFollowEnd()
                }
            }
            "伴随结束" -> {
                viewModelScope.launch {
                    eventControl.stopEvent(eventType = EventType.FOLLOW)
                    insertOrFollowEnd()
                }
            }
        }
    }

    fun onSubButtonLongClick(eventControl: EventControl) {
        if (buttonsState.subText.value == "插入") return

        viewModelScope.launch {
            // 结束子事件————————————————
            eventControl.stopEvent(eventType = EventType.INSERT)
            insertOrFollowEnd()

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

        buttonsState.undoShow.value = false
    }

    /**
     * 这个函数是从数据库中获取刚刚结束的事件，可能是主事件，也可能是子事件。
     */
    private suspend fun getJustEndedEvent(): Event {
        RDALogger.info("getJustEndedEvent 执行！")
        return if (buttonsState.mainShow.value && buttonsState.mainText.value == "开始") {
            RDALogger.info("从最近的主事件中获取")
            repository.getLastMainEvent()!! // 主事件刚结束，从最近的主事件获取
        } else repository.getLastEvent() // 子事件结束，获取最近的事件
    }

    private fun toggleStateOnMainStart() {
        currentStatus = EventStatus.SUBJECT_ONLY

        buttonsState.apply {
            mainText.value = "结束"
            subShow.value = true
            undoShow.value = false
        }
    }

    private fun toggleStateOnSubInsert() {
        currentStatus = EventStatus.SUB_TIMING

        buttonsState.apply {
            subText.value = "插入结束"
            mainShow.value = false
            undoShow.value = false
        }
    }

}
