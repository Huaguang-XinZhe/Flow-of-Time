package com.huaguang.flowoftime.ui.components.event_input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.InputState
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventInputViewModel @Inject constructor(
    val repository: EventRepository,
    val iconRepository: IconMappingRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    val inputState = InputState.initialValue()

    private var initialName = ""
    private var endTime: LocalDateTime? = null

    fun onConfirmButtonClick() {
        inputState.apply {
            if (newName.value == "") {
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
                sharedState.currentEvent?.name = inputState.newName.value
            }

            viewModelScope.launch {
                repository.updateEventName(eventId.value, newName.value)
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
    }



    fun onStepButtonClick() {
        TODO("Not yet implemented")
    }

    fun onCoreFloatingButtonClick() {
        TODO("Not yet implemented")
    }

    fun undoButtonClick() {
        TODO("Not yet implemented")
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
                if (endTime == null && eventId.value == sharedState.autoId) {
                    result = true
                }
            } else { // 记录的环境下
                result = true
            }
        }

        return result
    }

}