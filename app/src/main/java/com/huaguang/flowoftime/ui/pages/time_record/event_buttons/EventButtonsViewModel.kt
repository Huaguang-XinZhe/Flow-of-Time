package com.huaguang.flowoftime.ui.pages.time_record.event_buttons

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.UndoStack
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventButtonsViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    val buttonsState: ButtonsState,
    val pauseState: PauseState,
    val inputState: InputState,
    val undoStack: UndoStack,
) : ViewModel() {

    private var cursorType get() = sharedState.cursorType.value
        set(value) {
            sharedState.cursorType.value = value
        }

    private val _eventLiveData = MutableLiveData<Event>()
    val eventLiveData: LiveData<Event> get() = _eventLiveData

    val buttonsStateControl = object : ButtonsStateControl {
        override fun toggleMainEnd() { // 按钮切换到主题事项结束的状态（说明主题事项正在进行）
            toggleStateOnMainStart()
        }

        override fun toggleSubEnd(type: EventType) { // 按钮切换到子项结束的状态（说明子项正在进行）
            buttonsState.apply {

                if (type == EventType.FOLLOW) { // 伴随事件正在进行
                    mainShow.value = false
                    subText.value = "伴随结束"
                    cursorType = EventType.FOLLOW
                } else if (type == EventType.STEP) { // 步骤正在进行
                    mainText.value = "步骤结束"
                    subText.value = "step 插入"
                    cursorType = EventType.STEP
                }
            }
        }
    }

    fun getDisplayTextForSub(subText: String): String {
        return when (subText) {
            "step 插入" -> "插入"
            "step 插入结束" -> "插入结束"
            else -> subText
        }
    }

    fun toggleStateOnMainStop() {
        cursorType = null
        
        buttonsState.apply {
            mainText.value = "开始"
            subShow.value = false

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

    fun onMainButtonClick(
        eventControl: EventControl,
        selectedTime: MutableState<LocalDateTime?>?,
        checked: MutableLiveData<Boolean>
    ) {
        clearState(checked, selectedTime)

        viewModelScope.launch {
            eventControl.apply {
                when (buttonsState.mainText.value) {
                    "开始" -> {
                        toggleStateOnMainStart()
                        startEvent(eventType = EventType.SUBJECT)
                    }
                    "结束" -> {
                        stopEvent()
                        toggleStateOnMainStop()
                    }
                    "步骤结束" -> {
                        stopEvent(eventType = EventType.STEP)
                        toggleStateOnMainStart()
                    }
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

    fun onSubButtonClick(
        eventControl: EventControl,
        selectedTime: MutableState<LocalDateTime?>?,
        checked: MutableLiveData<Boolean>
    ) {
        clearState(checked, selectedTime)

        viewModelScope.launch {
            eventControl.apply {
                when (buttonsState.subText.value) {
                    "插入" -> startInsert(EventType.SUBJECT_INSERT)
                    "step 插入" -> startInsert(EventType.STEP_INSERT)
                    "插入结束" -> endInsert(
                        stopEventType = EventType.SUBJECT_INSERT,
                        newCursorType = EventType.SUBJECT,
                    )
                    "step 插入结束" -> endInsert(
                        stopEventType = EventType.STEP_INSERT,
                        newCursorType = EventType.STEP,
                        newSubTextValue = "step 插入"
                    )
                    "伴随结束" -> {
                        stopEvent(eventType = EventType.FOLLOW)
                        cursorType = EventType.SUBJECT
                        updateButtonsStateToInsert()
                    }
                }
            }
        }
    }

    fun onSubButtonLongClick(eventControl: EventControl) {
        if (buttonsState.subText.value == "插入") return

        viewModelScope.launch {
            // 结束子事件————————————————
//            eventControl.stopEvent(eventType = EventType.INSERT)
//            insertOrFollowEnd(EventType.INSERT)

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
//                toggleStateOnSubInsert()
            }
        }

        buttonsState
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
        cursorType = EventType.SUBJECT

        buttonsState.apply {
            mainText.value = "结束"
            subShow.value = true
        }
    }

    private fun clearState(
        checked: MutableLiveData<Boolean>,
        selectedTime: MutableState<LocalDateTime?>?,
    ) {
        unCheck(selectedTime)
        pauseRecovery(checked)
    }

    /**
     * “暂停/恢复” 按钮恢复初始状态（继续）
     * checked 为继续的状态，!checked 即暂停的状态
     */
    private fun pauseRecovery(checked: MutableLiveData<Boolean>) {
        if (checked.value == false) { // 只有以前是暂停的状态，才恢复
            checked.value = true // 状态改变后会触发副作用内的逻辑，但需要时间，在此之前，下面的代码会先执行。
        }
    }

    /**
     * 取消时间标签的选中状态
     */
    private fun unCheck(selectedTime: MutableState<LocalDateTime?>?) {
        selectedTime?.value = null
    }

    private suspend fun EventControl.startInsert(eventType: EventType) {
        buttonsState.apply {
            subText.value = eventType.endName()
            mainShow.value = false
        }
        cursorType = eventType
        startEvent(eventType = eventType)
    }

    private suspend fun EventControl.endInsert(
        stopEventType: EventType,
        newCursorType: EventType,
        newSubTextValue: String = "插入"
    ) {
        stopEvent(eventType = stopEventType)
        cursorType = newCursorType
        updateButtonsStateToInsert(newSubTextValue)
    }

    private fun updateButtonsStateToInsert(
        newSubTextValue: String = "插入"
    ) {
        buttonsState.apply {
            subText.value = newSubTextValue
            mainShow.value = true

        }
    }

}
