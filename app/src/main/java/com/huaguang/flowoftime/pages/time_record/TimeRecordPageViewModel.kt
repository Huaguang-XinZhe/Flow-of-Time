package com.huaguang.flowoftime.pages.time_record

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventControl

import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.DNDManager
import com.huaguang.flowoftime.utils.getEventDate
import kotlinx.coroutines.launch
import java.time.Duration

import java.time.LocalDateTime

val LocalEventControl = compositionLocalOf<EventControl> { error("没有提供实现 EventControl 接口的对象！") }
val LocalSelectedTime = compositionLocalOf<MutableState<LocalDateTime>?> { null }

/**
 * 页面 ViewModel，用于协调当前页面内各个组件的交互，并存储 TimeRecordPage UI 页面的数据，作为其唯一依赖
 */
class TimeRecordPageViewModel(
    val eventButtonsViewModel: EventButtonsViewModel,
    val timeRegulatorViewModel: TimeRegulatorViewModel,
    val eventRepository: EventRepository,
    val iconRepository: IconMappingRepository,
    private val spHelper: SPHelper,
    private val sharedState: SharedState,
    private val dndManager: DNDManager,
) : ViewModel() {

    private var eventStatus
        get() = sharedState.eventStatus.value
        set(value) {
            sharedState.eventStatus.value = value
        }

    var currentEventState: MutableState<Event?> = mutableStateOf(null)
    var eventStop = false
    var autoId = 0L

    val eventControl = object : EventControl {
        override fun startEvent(startTime: LocalDateTime, type: EventType) {
            viewModelScope.launch {
                currentEventState.value = createCurrentEvent(startTime, type) // type 由用户与 UI 的交互自动决定
                RDALogger.info("start = ${currentEventState.value}")
                autoId = eventRepository.insertEvent(currentEventState.value!!) // 存入数据库

                // 关键状态
                eventStop = false
            }
        }

        override fun stopEvent() {
            viewModelScope.launch {
                currentEventState.value = updateCurrentEvent()
                eventRepository.updateEvent(currentEventState.value!!) // 更新数据库
                spHelper.resetPauseInterval()
            }
            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭

            // 关键状态
            eventStop = true
        }
    }

    private suspend fun updateCurrentEvent(): Event {
        var event: Event? = null

        currentEventState.value?.let {
            val newEvent = it.copy() // 先复制原始对象

            // 如果是主事件，就从数据库中获取子事件列表，并计算其间隔总和
            val subEventsDuration = if (newEvent.parentEventId == null) {
                eventRepository.calculateSubEventsDuration(newEvent.id)
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
            eventRepository.fetchMainEventId()
        } else null
    }


}