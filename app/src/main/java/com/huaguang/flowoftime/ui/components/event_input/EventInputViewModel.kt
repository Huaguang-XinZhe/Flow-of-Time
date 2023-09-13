package com.huaguang.flowoftime.ui.components.event_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventInputViewModel @Inject constructor(
    val repository: EventRepository,
    val iconRepository: IconMappingRepository,
    private val spHelper: SPHelper,
    private val idState: IdState,
    val sharedState: SharedState,
    val inputState: InputState,
) : ViewModel() {

    private var initialName = ""
    private var endTime: LocalDateTime? = null
    var coreName = ""
    var confirmThenStart = false
    val scrollTrigger = mutableStateOf(false)
    val scrollOffset = mutableStateOf(0f)

    private val _currentCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val currentCombinedEventFlow: StateFlow<CombinedEvent?> = _currentCombinedEventFlow.asStateFlow()
    private val _secondLatestCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val secondLatestCombinedEventFlow: StateFlow<CombinedEvent?> = _secondLatestCombinedEventFlow.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getCurrentCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                _currentCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

        viewModelScope.launch {
            repository.getSecondLatestCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                _secondLatestCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }
    }

    fun coreButtonNotShow(): Boolean{
        val subTiming = sharedState.cursorType.value?.let {
            it != EventType.SUBJECT
        } ?: false // 没有事件正在计时，也就意味着没有子项正在计时，为 false

        return inputState.show.value || subTiming
    }

    fun onConfirmButtonClick(text: String) {
        inputState.apply {
            newName.value = text // 把输入完成的值赋给 newName

            if (text == "") {
                sharedState.toastMessage.value = "名称不能为空哦……"
                return
            }

            show.value = false

            if (intent.value == InputIntent.MODIFY) { // 意图修改
                handleModifyIntent()
            } else { // 意图记录
                handleRecordIntent()
            }

            if (isCurrentRecording()) { // 更新正在进行的当前项的 name 值，不然结束的时候又给改回去了
                sharedState.currentEvent?.name = text
            }

            viewModelScope.launch {
                repository.updateEventName(eventId.value, text)
            }
        }
    }

    fun onNameClick(event: Event, itemType: ItemType) {
        inputState.apply {
            eventId.value = event.id
            show.value = true
            newName.value = event.name
            intent.value = InputIntent.MODIFY
            type.value =  itemType// 用于判断是否需要更新当前项的 name 值
        }

        initialName = event.name // 传出，给更新数据用
        endTime = event.endTime // 传出，用于判断事件是否正在进行

        val diff = event.id - idState.subject.value
        if (itemType == ItemType.RECORD && diff > 0) { // 触发滚动
            scrollTrigger.value = !scrollTrigger.value
            scrollOffset.value = diff * 25f
        }
    }

    fun onStepButtonClick(
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl,
    ) {
        viewModelScope.launch {
            eventControl.startEvent(eventType = EventType.STEP)
            buttonsStateControl.stepTiming()
        }
    }

    fun onCoreFloatingButtonClick(
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl,
    ) {
        viewModelScope.launch {
            coreName = spHelper.getCurrentCoreEventName(coreName)

            if (coreName.isEmpty()) { // 在最开始的时候，SP 中没有值，coreName 仍有可能为空，这是就弹窗请用户设置，然后再开始事件
                sharedState.apply {
                    dialogShow.value = true
                    toastMessage.value = "请预先设置当前核心（名称）"
                }
                confirmThenStart = true // 设置好点击确认就马上开启一个新事件

                return@launch
            }

            val type = if (hasSubjectExist()) EventType.FOLLOW else EventType.SUBJECT

            eventControl.startEvent(
                name = coreName,
                eventType = type
            )

            if (hasSubjectExist()) {
                buttonsStateControl.followTiming() // 切换到 ”伴随结束“ 的按钮状态
            } else {
                buttonsStateControl.subjectTiming() // 切换到 “主题结束” 的按钮状态
            }
        }
    }

    fun onCoreFloatingButtonLongClick() {
        coreName = spHelper.getCurrentCoreEventName(coreName)
        sharedState.dialogShow.value = true // 显示名称输入 Dialog
    }

    fun onDialogDismiss() {
        sharedState.dialogShow.value = false
    }

    fun onDialogConfirm(
        newText: String,
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl
    ) {
        onDialogDismiss()
        if (newText.isEmpty() && newText == coreName) return

        coreName = newText // 必须同时更新内存中的 coreName
        spHelper.saveCurrentCoreEventName(newText)

        if (confirmThenStart) { // 最开始的时候，设置完就开启新事件
            onCoreFloatingButtonClick(eventControl, buttonsStateControl)
            confirmThenStart = false // 重置，以防止在本次应用周期内的下次修改再次开启
        }
    }

    private fun handleRecordIntent() {
        // TODO: 起床、睡等特殊事项的处理；主事项和插入事项的处理

    }

    private fun handleModifyIntent() {
        inputState.apply {
            if (newName.value == initialName) return // 有差异才更新
            // TODO:
        }
    }

    private fun isCurrentRecording(): Boolean {
        var result = false

        inputState.apply {
            if (intent.value == InputIntent.MODIFY) {
                // 只要在修改的坏境下，这个判断条件才成立，才能代表正在进行的当前项
                if (endTime == null && eventId.value == idState.current.value) {
                    result = true
                }
            } else { // 记录的环境下
                result = true
            }
        }

        return result
    }

    private fun hasSubjectExist() = sharedState.cursorType.value != null

}

