package com.huaguang.flowoftime.ui.components.event_input

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
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
    private var confirmThenStart = false
    val scrollTrigger = mutableStateOf(false)
    val scrollOffset = mutableStateOf(0f)

    private val _currentCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val currentCombinedEventFlow: StateFlow<CombinedEvent?> = _currentCombinedEventFlow.asStateFlow()
    private val _secondLatestCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val secondLatestCombinedEventFlow: StateFlow<CombinedEvent?> = _secondLatestCombinedEventFlow.asStateFlow()

    init {
        viewModelScope.launch {
            // 去掉 filterNotNull，在数据库为空时将发射 null，否则不会发射值，也就不会通知 UI 变化
            repository.getCurrentCombinedEventFlow().collect { combinedEvent ->
//                RDALogger.info("currentCombinedEvent = $combinedEvent")
                _currentCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

        viewModelScope.launch {
            repository.getSecondLatestCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                _secondLatestCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }
    }

    fun onDisplayItemDoubleClick(itemState: MutableState<ItemType>) {
        itemState.value = ItemType.RECORD
    }

    fun onRecordingItemDoubleClick(itemState: MutableState<ItemType>) {
        if (sharedState.cursorType.value == null) { // 事件已经终结的时候才能进行切换否则提示
            itemState.value = ItemType.DISPLAY
        } else {
            sharedState.toastMessage.value = "事项终结后才能切换哦😉"
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

            viewModelScope.launch {
                RDALogger.info("eventId = ${eventId.value}, text = $text")
                repository.updateEventName(eventId.value, text)
            }
        }
    }

    fun onNameClick(event: Event, itemType: ItemType) {
        inputState.apply {
            if (show.value) return // 如果输入框已经弹出的话，就不允许在修改其他事项的名称

            eventId.value = event.id
            show.value = true
            newName.value = event.name
            intent.value = InputIntent.MODIFY
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
        buttonsStateControl.stepTiming()

        viewModelScope.launch {
            eventControl.startEvent(eventType = EventType.STEP)
        }
    }

    fun onStepButtonLongClick(
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl
    ) {
        viewModelScope.launch {
            val startTime = repository.getOffsetStartTimeForStep(idState)
            eventControl.startEvent( // start 放在前边，输入框弹的快一些。
                startTime = startTime,
                eventType = EventType.STEP
            )
            buttonsStateControl.stepTiming()
            sharedState.toastMessage.value = "step 补计……"
        }
    }

    fun onCoreFloatingButtonClick(
        eventControl: EventControl,
        buttonsStateControl: ButtonsStateControl,
        displayItemState: MutableState<ItemType>,
        recordingItemState: MutableState<ItemType>,
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
                buttonsStateControl.resetItemState(displayItemState, recordingItemState)
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
        buttonsStateControl: ButtonsStateControl,
        displayItemState: MutableState<ItemType>,
        recordingItemState: MutableState<ItemType>
    ) {
        onDialogDismiss()
        if (newText.isEmpty() && newText == coreName) return

        coreName = newText // 必须同时更新内存中的 coreName
        spHelper.saveCurrentCoreEventName(newText)

        if (confirmThenStart) { // 最开始的时候，设置完就开启新事件
            onCoreFloatingButtonClick(
                eventControl,
                buttonsStateControl,
                displayItemState,
                recordingItemState
            )
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


    private fun hasSubjectExist() = sharedState.cursorType.value != null

}

