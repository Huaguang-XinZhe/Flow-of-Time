package com.huaguang.flowoftime.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventControl
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.utils.DNDManager
import com.huaguang.flowoftime.utils.getEventDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

/**
 * 页面 ViewModel，用于协调当前页面内各个组件的交互，并存储 TimeRecordPage UI 页面的数据，作为其唯一依赖
 */
class TimeRecordPageViewModel(
    val eventButtonsViewModel: EventButtonsViewModel,
    val timeRegulatorViewModel: TimeRegulatorViewModel,
    val eventInputViewModel: EventInputViewModel,
    val repository: EventRepository,
    private val spHelper: SPHelper,
    val sharedState: SharedState,
    private val dndManager: DNDManager,
) : ViewModel() {

    private var eventStatus
        get() = sharedState.eventStatus.value
        set(value) {
            sharedState.eventStatus.value = value
        }

    private val _currentEventFlow = MutableStateFlow<Event?>(null)
    val currentEventFlow: StateFlow<Event?> = _currentEventFlow.asStateFlow()

    var currentEvent
        get() = sharedState.currentEvent
        set(value) {
            sharedState.currentEvent = value
        }

    var autoId = 0L

    init {
        viewModelScope.launch {
            repository.getCurrentEventFlow().filterNotNull().collect { event ->
                RDALogger.info("收集到 event = $event")
                if (currentEvent == null) currentEvent = event // 给内存中的 currentEvent（实现数据库和内存当前项的同步更新）
//                RDALogger.info("收集块：currentEvent = $currentEvent")
                _currentEventFlow.value = event // 传给 UI
            }
        }
    }

    val eventControl = object : EventControl {
        override fun startEvent(startTime: LocalDateTime, type: EventType) {
            viewModelScope.launch {
                currentEvent = createCurrentEvent(startTime, type) // type 由用户与 UI 的交互自动决定
//                RDALogger.info("start = $currentEvent")
                autoId = repository.insertEvent(currentEvent!!) // 存入数据库

            }
        }

        override fun stopEvent() {
            viewModelScope.launch {
                currentEvent = updateCurrentEvent()
                repository.updateEvent(currentEvent!!) // 更新数据库
                spHelper.resetPauseInterval()
            }
            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
        }
    }

    private suspend fun updateCurrentEvent(): Event {
        var event: Event? = null

        currentEvent?.let {
            val newEvent = it.copy() // 先复制原始对象

            // 如果是主事件，就从数据库中获取子事件列表，并计算其间隔总和
            val subEventsDuration = if (newEvent.parentEventId == null) {
                repository.calculateSubEventsDuration(newEvent.id)
            } else Duration.ZERO

            newEvent.id = autoId // 必须指定这一条，否则数据库不会更新
            newEvent.endTime = LocalDateTime.now()
            newEvent.duration =
                Duration.between(newEvent.startTime, newEvent.endTime).minus(subEventsDuration)
            newEvent.pauseInterval = spHelper.getPauseInterval()

            event = newEvent
        }

        return event!!
    }

    private suspend fun createCurrentEvent(
        startTime: LocalDateTime,
        type: EventType,
    ) = Event(
        startTime = startTime,
        eventDate = getEventDate(startTime),
        parentEventId = fetchMainEventId(),
        type = type,
        // TODO: 随便拟一些数据，测试，之后要删掉 
        name = "时光流开发完善", 
        category = "开发", 
        tags = listOf("时光流", "个人管理", "现实应用", "时间")
    )


    private suspend fun fetchMainEventId(): Long? {
        return if (eventStatus == EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS) {
            repository.fetchMainEventId()
        } else null
    }


}