package com.huaguang.flowoftime.ui.pages.time_record.event_buttons


import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.UndoStack
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.Action
import com.huaguang.flowoftime.data.models.ButtonActionParams
import com.huaguang.flowoftime.data.models.EventCategoryUpdate
import com.huaguang.flowoftime.data.models.Operation
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.ItemState
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
    val inputState: InputState,
    val undoStack: UndoStack,
    private val idState: IdState,
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

        override fun resetItemState(
            displayItemState: ItemState,
            recordingItemState: ItemState,
        ) {
            resetDRItemState(displayItemState, recordingItemState)
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

    fun onMainButtonClick(params: ButtonActionParams) {
        clearState(params)

        viewModelScope.launch {
            params.eventControl.apply {
                when (buttonsState.mainText.value) {
                    "开始" -> {
                        startEvent(eventType = EventType.SUBJECT)
                        toSubjectTimingState()
                    }
                    "结束" -> {
                        toInitialState()
                        stopEvent()
                    }
                    "步骤结束" -> {
                        toSubjectTimingState()
                        stopEvent(eventType = EventType.STEP)
                    }
                }
            }
        }
    }


    fun onMainButtonLongClick(params: ButtonActionParams) {
        if (buttonsState.mainText.value != "开始") return

        clearState(params)

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val startTime = repository.getOffsetStartTimeForSubject()

            if (startTime == null) {
                sharedState.toastMessage.value = "当前无法补计，直接开始吧"
                return@launch
            }

            params.eventControl.startEvent(startTime = startTime, eventType = EventType.SUBJECT)
            toSubjectTimingState()
            sharedState.toastMessage.value = "开始补计……"
        }
    }

    fun onSubButtonClick(params: ButtonActionParams) {
        clearState(params, resetDRItemState = false)

        viewModelScope.launch {
            params.eventControl.apply {
                when (buttonsState.subText.value) {
                    "插入" -> {
                        startEvent(eventType = EventType.SUBJECT_INSERT)
                        toSubjectInsertState()
                    }
                    "step 插入" -> {
                        startEvent(eventType = EventType.STEP_INSERT)
                        toStepInsertState()
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

    fun onUndoButtonClick(
        checked: MutableLiveData<Boolean>,
        recordingItemState: ItemState
    ) {
        val operation = undoStack.undo() ?: return // 其实不大可能为空，栈里要真为空的话，那撤销按钮根本就不会显示
        pauseRecovery(checked) // 恢复暂停按钮状态
        recordingItemState.mode.value = Mode.RECORD // 恢复到记录状态

        viewModelScope.launch {
            operation.apply {
                if (action.isStart()) {
                    restoreIdState(operation)
                    repository.deleteEvent(eventId)
                } else { // 只有两种，要么 start，要么 end，所以这里一定是结束（撤销）
                    // 必须在前边进行，否则获取的 duration 为 null
                    if (action == Action.SUBJECT_END) {
                        sharedState.categoryUpdate.value = EventCategoryUpdate(eventId, "-1")
                    }

                    repository.updateThree(eventId, null, pauseInterval, null)
                }
            }
        }

        // 撤销后要进入的状态
        when(operation.action) {
            Action.SUBJECT_START -> toInitialState()

            Action.STEP_START,
            Action.FOLLOW_START,
            Action.SUBJECT_INSERT_START,
            Action.SUBJECT_END -> toSubjectTimingState()

            Action.STEP_INSERT_START,
            Action.STEP_END -> toStepTimingState()

            Action.FOLLOW_END -> toFollowTimingState()

            Action.SUBJECT_INSERT_END -> toSubjectInsertState()

            Action.STEP_INSERT_END -> toStepInsertState()
        }


    }

    private fun restoreIdState(operation: Operation) {
        idState.apply {
            operation.immutableIdState?.let {
                current.value = it.current
                subject.value = it.subject
                step.value = it.step
            }
        }
    }

    private fun clearState(params: ButtonActionParams, resetDRItemState: Boolean = true) {
        unCheck(params.selectedTime)
        pauseRecovery(params.checked)

        if (resetDRItemState) { // 如果子按钮点击或长按，就不需要重置 Item 状态了。
            resetDRItemState(params.displayItemState, params.recordingItemState)
        }
    }

    private fun resetDRItemState(
        displayItemState: ItemState,
        recordingItemState: ItemState
    ) {
        displayItemState.mode.value = Mode.DISPLAY
        recordingItemState.mode.value = Mode.RECORD
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

    fun onMenuClick() {
        idState.apply {
            subject.value = 195
            current.value = 195
        }

        toSubjectTimingState()
    }


}
