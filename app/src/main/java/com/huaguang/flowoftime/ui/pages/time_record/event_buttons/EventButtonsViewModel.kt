package com.huaguang.flowoftime.ui.pages.time_record.event_buttons

import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.Action
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.UndoStack
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
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

    val buttonsStateControl = object : ButtonsStateControl {
        override fun subjectTiming() { // 主题事项正在进行
            toSubjectTimingState()
        }

        override fun followTiming() {
            toFollowTimingState()
        }

        override fun stepTiming() {
            toStepTimingState()
        }
    }

    fun getDisplayTextForSub(subText: String): String {
        return when (subText) {
            "step 插入" -> "插入"
            "step 插入结束" -> "插入结束"
            else -> subText
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
                        toSubjectTimingState()
                        startEvent(eventType = EventType.SUBJECT)
                    }
                    "结束" -> {
                        stopEvent()
                        toInitialState()
                    }
                    "步骤结束" -> {
                        stopEvent(eventType = EventType.STEP)
                        toSubjectTimingState()
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
//            toggleStateOnMainStart()

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
                    "插入" -> {
                        toSubjectInsertState()
                        startEvent(eventType = EventType.SUBJECT_INSERT)
                    }
                    "step 插入" -> {
                        toStepInsertState()
                        startEvent(eventType = EventType.STEP_INSERT)
                    }
                    "插入结束" -> {
                        toSubjectTimingState()
                        stopEvent(EventType.SUBJECT_INSERT)
                    }
                    "step 插入结束" -> {
                        toStepTimingState()
                        stopEvent(EventType.STEP_INSERT)
                    }
                    "伴随结束" -> {
                        toSubjectTimingState()
                        stopEvent(EventType.FOLLOW)
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
//            toggleStateOnMainStop()

        }

        sharedState.toastMessage.value = "全部结束！"
    }

    fun onUndoButtonClick() {
        val operation = undoStack.undo() ?: return // 其实不大可能为空，栈里要真为空的话，那撤销按钮根本就不会显示

        viewModelScope.launch {
            operation.apply {
                if (action.isStart()) {
                    repository.deleteEvent(eventId)
                } else if (action.isEnd()) {
                    repository.updateThree(eventId, null, pauseInterval, null)
                }
            }
        }

        // 撤销后要进入的状态
        when(operation.action) {
            Action.SUBJECT_START -> toInitialState()
            Action.STEP_START -> toSubjectTimingState()
            Action.FOLLOW_START -> toSubjectTimingState()
            Action.SUBJECT_INSERT_START -> toSubjectTimingState()
            Action.STEP_INSERT_START -> toStepTimingState()
            Action.SUBJECT_END -> toSubjectTimingState()
            Action.STEP_END -> toStepTimingState()
            Action.FOLLOW_END -> toFollowTimingState()
            Action.SUBJECT_INSERT_END -> toSubjectInsertState()
            Action.STEP_INSERT_END -> toStepInsertState()
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

    private fun toStepInsertState() {
        cursorType = EventType.STEP_INSERT

        buttonsState.apply {
            subShow.value = true
            subText.value = "step 插入结束"
            mainShow.value = false
        }
    }

    private fun toStepTimingState() {
        cursorType = EventType.STEP

        buttonsState.apply {
            mainShow.value = true
            mainText.value = "步骤结束"
            subShow.value = true
            subText.value = "step 插入"
        }
    }

    private fun toSubjectTimingState() {
        cursorType = EventType.SUBJECT

        buttonsState.apply {
            mainShow.value = true
            mainText.value = "结束"
            subShow.value = true
            subText.value = "插入"
        }
    }

    private fun toInitialState() {
        cursorType = null

        buttonsState.apply {
            mainShow.value = true
            mainText.value = "开始"
            subShow.value = false
        }
    }

    private fun toSubjectInsertState() {
        cursorType = EventType.SUBJECT_INSERT

        buttonsState.apply {
            subShow.value = true
            subText.value = "插入结束"
            mainShow.value = false
        }
    }

    private fun toFollowTimingState() {
        cursorType = EventType.FOLLOW

        buttonsState.apply {
            subShow.value = true
            subText.value = "伴随结束"
            mainShow.value = false
        }
    }

}
