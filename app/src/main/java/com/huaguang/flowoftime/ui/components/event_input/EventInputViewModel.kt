package com.huaguang.flowoftime.ui.components.event_input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.BlockType
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventInputViewModel @Inject constructor(
    val repository: EventRepository,
    val iconRepository: IconMappingRepository,
    val idState: IdState,
    val sharedState: SharedState,
    val inputState: InputState,

) : ViewModel() {

    private var initialName = ""
    private var endTime: LocalDateTime? = null

//    val scrollTrigger = mutableStateOf(false)
//    val scrollOffset = mutableFloatStateOf(0f)
    // TODO: 输入框弹起时可能需要列表滚动，待日后集中处理

    fun onDisplayItemDoubleClick(itemState: ItemState) {
        itemState.mode.value = Mode.RECORD
    }

    fun onRecordingItemDoubleClick(itemState: ItemState) {
        // 事件已经终结的时候才能进行切换否则提示（展示区块或者终结的记录块，都可以切换展示）
        if (itemState.block == BlockType.DISPLAY || sharedState.cursorType.value == null) { // cursorType 只有记录块有
            itemState.mode.value = Mode.DISPLAY
        } else {
            sharedState.toastMessage.value = "事项终结后才能切换哦😉"
        }
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

            val category = if (eventType.value == EventType.SUBJECT) { // 类属
                sharedState.classify(text) // 这里的类属不成功也会返回 null
            } else null

            category?.let {
                if (intent.value == InputIntent.RECORD) {
                    idState.endId = eventId.value // 意图记录且有类属时才需要更新 endId
                }
                sharedState.toastMessage.value = "成功类属为：$category"
            }

            viewModelScope.launch {
                repository.updateNameAndCategory(eventId.value, text, category)
            }
        }
    }

    fun onNameClick(event: Event, mode: Mode) {
        inputState.apply {
            if (show.value) return // 如果输入框已经弹出的话，就不允许在修改其他事项的名称

            eventId.value = event.id
            show.value = true
            newName.value = event.name
            intent.value = InputIntent.MODIFY
        }

        initialName = event.name // 传出，给更新数据用
        endTime = event.endTime // 传出，用于判断事件是否正在进行

        // TODO: 触发滚动的方法应该根据页面类型进行定制
//        val diff = event.id - idState.subject.value
//        if (mode == Mode.RECORD && diff > 0) { // 触发滚动
//            scrollTrigger.value = !scrollTrigger.value
//            scrollOffset.value = diff * 25f
//        }
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

    private fun handleRecordIntent() {
        // TODO: 起床、睡等特殊事项的处理；主事项和插入事项的处理

    }

    private fun handleModifyIntent() {
        inputState.apply {
            if (newName.value == initialName) return // 有差异才更新
            // TODO:
        }
    }


}

