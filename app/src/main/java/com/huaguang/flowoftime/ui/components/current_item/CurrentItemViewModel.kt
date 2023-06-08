package com.huaguang.flowoftime.ui.components.current_item

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.getEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CurrentItemViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    var eventType = sharedState.eventType.value
    var isTracking = sharedState.isTracking.value

    val currentEvent: MutableState<Event?> =  mutableStateOf(null)
    var incompleteMainEvent: Event? by mutableStateOf(null)

    // 辅助函数逻辑变量
    var subButtonClickCount = 0
    var isLastStopFromSub = false


    fun resetCurrentEventAndState() {
        // 子事件结束后恢复到主事件（数据库插入会重组一次，因此这里无需赋值重组）
        if (eventType == EventType.SUB) {
            restoreOnMainEvent()
        } else {
            resetCurrentOnMainBranch()
        }

        Log.i("打标签喽", "currentEvent.value = ${currentEvent.value}")
    }

    private fun restoreOnMainEvent(fromDelete: Boolean = false) {
        Log.i("打标签喽", "结束的是子事件")
        if (!fromDelete) {
            currentEvent.value?.let {
                it.id = it.parentId!!
                it.startTime = incompleteMainEvent!!.startTime
                it.name = incompleteMainEvent!!.name
                it.endTime = LocalDateTime.MIN // 为优化显示，实际业务不需要
                it.parentId = null
                it.isCurrent = true
            }
        } else {
            currentEvent.value = incompleteMainEvent!!.copy(
                endTime = LocalDateTime.MIN,
                isCurrent = true
            )
        }

        isLastStopFromSub = true
        eventType = EventType.MAIN // 必须放在 stop 逻辑中
    }

    private fun resetCurrentOnMainBranch(fromDelete: Boolean = false) {
        Log.i("打标签喽", "结束的是主事件")
        if (fromDelete) {
            currentEvent.value = null
        } else { // 本来应该为 null，这里是为了优化显示
            currentEvent.value?.name = "￥为减少重组，优化频闪，不显示的特别设定￥"
        }

        isLastStopFromSub = false
        isTracking = false
    }

    suspend fun updateCurrentEventOnStop() {
        currentEvent.value?.let {
            // 如果是主事件，就计算从数据库中获取子事件列表，并计算其间隔总和
            val subEventsDuration = if (it.parentId == null) {
                repository.calculateSubEventsDuration(it.id)
            } else Duration.ZERO

            // 这里就不赋给 currentEventState 的值了，减少不必要的重组
            it.endTime = LocalDateTime.now()
            it.duration = Duration.between(it.startTime, it.endTime).minus(subEventsDuration)
            it.isCurrent = false
        }
    }

    suspend fun saveCurrentEvent() {
        val updateCondition = isLastStopFromSub && eventType == EventType.MAIN

        currentEvent.value?.let {
            repository.saveCurrentEvent(updateCondition, it)
        }
    }

    fun updateSTonDragStopped(updatedEvent: Event) {
        if (updatedEvent.isCurrent) { // 处理 currentItem
            currentEvent.value?.startTime = updatedEvent.startTime
        }
    }

    suspend fun saveInCompleteMainEvent() {
        if (eventType == EventType.SUB) {
            subButtonClickCount++

            if (subButtonClickCount == 1) { // 首次点击插入按钮
                val id = repository.insertEvent(currentEvent.value!!)
                // 存储每个未完成的主事件，以备后边插入的子事件结束后获取
                incompleteMainEvent = currentEvent.value!!.copy(id = id)
            }

        } else {
            subButtonClickCount = 0 // 一遇到主事件就清空
        }

    }


    fun createCurrentEvent(
        startTime: LocalDateTime
    ) = Event(
        startTime = startTime,
        eventDate = getEventDate(startTime),
        parentId = fetchMainEventId(),
        isCurrent = true
    )

    private fun fetchMainEventId(): Long? {
        var mainEventId: Long? = null

        viewModelScope.launch {
            mainEventId = if (eventType == EventType.SUB) {
                repository.fetchMainEventId()
            } else null
        }

        return mainEventId
    }


}