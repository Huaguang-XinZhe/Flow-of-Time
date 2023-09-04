package com.huaguang.flowoftime.pages.time_record

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventControl
import com.huaguang.flowoftime.pages.time_record.recording_event_item.EventType
import com.huaguang.flowoftime.pages.time_record.recording_event_item.RecordingEventItemViewModel
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.components.EventDisplay
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
    val recordingEventItemViewModel: RecordingEventItemViewModel,
    val eventRepository: EventRepository,
    val iconRepository: IconMappingRepository,

) : ViewModel() {

    var currentEvent: Event? = null
    var eventStop = false

    val eventControl = object : EventControl {
        override fun startNewEvent(startTime: LocalDateTime) {
            TODO("Not yet implemented")
        }

        override fun stopCurrentEvent() {
            TODO("Not yet implemented")
        }
    }

    fun getLastEventDisplay(): EventDisplay {
        return EventDisplay(
            name = "这是一个事件，主题事件",
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.of(2023, 9, 4, 21, 23),
            duration = Duration.ofHours(3),
            type = EventType.SUBJECT,
            category = "阅读",
            tags = listOf("应用", "时间统计法", "中国人"),
        )
    }

    fun getEventDisplay(): EventDisplay {
        return EventDisplay(
            name = "这是一个事件，主题事件",
            startTime = LocalDateTime.now(),
            type = EventType.SUBJECT,
            category = "阅读",
            tags = listOf("应用", "时间统计法", "中国人"),
        )
    }

}