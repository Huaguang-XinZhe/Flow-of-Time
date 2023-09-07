package com.huaguang.flowoftime.ui.components.event_input

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventInputViewModel @Inject constructor(
    val repository: EventRepository,
    val iconRepository: IconMappingRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    val isInputShow = mutableStateOf(false)
    val newEventName = mutableStateOf("")

    private var eventId = 0L // 用于从数据库找到相应的数据条目
    private var initialName = ""
    private var isRecordingItem = true

    fun onConfirmButtonClick() {
        isInputShow.value = false
        if (newEventName.value == initialName) return // 有差异才更新

        viewModelScope.launch {
            // 更新当前项的 name 值，要不然结束的时候又给改回去了
            RDALogger.info("isRecordingItem = $isRecordingItem")
            if (isRecordingItem) sharedState.currentEvent?.name = newEventName.value
            repository.updateEventName(eventId, newEventName.value)
        }
    }

    fun onNameClick(event: Event, isDisplay: Boolean) {
        newEventName.value = event.name
        isInputShow.value = true

        eventId = event.id // 传出，给更新数据用
        initialName = event.name // 传出，给更新数据用
        isRecordingItem = !isDisplay // 传出，用于判断是否需要更新当前项的 name 值
    }

    fun undoButtonClick() {
        TODO("Not yet implemented")
    }

}