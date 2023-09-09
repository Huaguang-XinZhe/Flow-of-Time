package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.SharedState
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
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

    private var currentEvent
        get() = sharedState.currentEvent
        set(value) {
            sharedState.currentEvent = value
        }
    private var autoId
        get() = sharedState.autoId
        set(value) {
            sharedState.autoId = value
        }

    private val _currentCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val currentCombinedEventFlow: StateFlow<CombinedEvent?> = _currentCombinedEventFlow.asStateFlow()
    private val _secondLatestCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val secondLatestCombinedEventFlow: StateFlow<CombinedEvent?> = _secondLatestCombinedEventFlow.asStateFlow()

    var subjectId = 0L

    init {
        RDALogger.info("init 块执行！")

        viewModelScope.launch {
            repository.getCurrentCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                RDALogger.info("收集到当前项：combinedEvent = $combinedEvent")
                _currentCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

        viewModelScope.launch {
            repository.getSecondLatestCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                RDALogger.info("收集到上一个：combinedEvent = $combinedEvent")
                _secondLatestCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

        // 这个协程会优先于上一个协程的执行，不知道为什么。这个协程只会执行一次，而上面那个协程被挂起，有新值的时候就会执行。
        viewModelScope.launch {
            if (currentEvent == null) currentEvent = repository.getCurrentEvent()
            RDALogger.info("currentEvent = $currentEvent")
        }
    }

    val eventControl = object : EventControl {
        override fun startEvent(startTime: LocalDateTime, name: String, eventType: EventType) {
            viewModelScope.launch {
                currentEvent = createCurrentEvent(startTime, name, eventType) // type 由用户与 UI 的交互自动决定
                autoId = repository.insertEvent(currentEvent!!) // 存入数据库

                if (eventType == EventType.SUBJECT) {
                    subjectId = autoId
                }

                updateInputState(autoId, name)
            }

        }

        override fun stopEvent(eventType: EventType) {
            viewModelScope.launch {
                if (notCurrentSubjectEvent(eventType)) {
                    val subjectDuration = calSubjectEventDuration()
                    repository.updateEndTimeAndDuration(subjectId, subjectDuration)
                } else {
                    currentEvent = updateCurrentEvent()
                    repository.updateEvent(currentEvent!!) // 更新数据库
                }
                spHelper.resetPauseInterval()
            }
//            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
        }
    }


    /**
     * 不是当前项的主题事项
     */
    private fun notCurrentSubjectEvent(eventType: EventType) =
        eventType == EventType.SUBJECT && autoId != subjectId

    /**
     * 间隔计算：减去 Item 中所有插入事件的间隔之和，再减去所有暂停间隔之和
     */
    private suspend fun calSubjectEventDuration(): Duration {
        val subjectEvent = repository.getEventById(subjectId)
        val pauseIntervalDuration = Duration.ofMinutes(subjectEvent.pauseInterval?.toLong() ?: 0L)
        val totalInsertDuration = repository.calTotalInsertDuration(subjectId)
        val standardDuration = Duration.between(subjectEvent.startTime, LocalDateTime.now())

        return standardDuration.minus(totalInsertDuration).minus(pauseIntervalDuration)
    }

    private fun updateInputState(id: Long, name: String) {
        eventInputViewModel.inputState.apply {
            eventId.value = id
            show.value = name.isEmpty() // 不传 name，或 name 值为空字符串，就不弹输入框
            newName.value = ""
            intent.value = InputIntent.RECORD
            type.value = ItemType.RECORD
        }
    }

    private fun updateCurrentEvent(): Event {
        var event: Event? = null

        currentEvent?.let {
            val newEvent = it.copy() // 先复制原始对象

            newEvent.id = autoId // 必须指定这一条，否则数据库不会更新
            newEvent.endTime = LocalDateTime.now()
            newEvent.duration = Duration.between(newEvent.startTime, newEvent.endTime)
            newEvent.pauseInterval = spHelper.getPauseInterval()

            event = newEvent
        }

        return event!!
    }

    private suspend fun createCurrentEvent(
        startTime: LocalDateTime,
        name: String,
        type: EventType,
    ) = Event(
        startTime = startTime,
        name = name,
        eventDate = getEventDate(startTime),
        parentEventId = getParentEventId(type),
        type = type,
        // TODO: 默认创建一些数据，以后要删除
        category = "阅读",
        tags = listOf("休闲", "精进", "应用")
    )


    private suspend fun getParentEventId(type: EventType): Long? {
        return when(type) {
            EventType.SUBJECT -> null
            EventType.STEP, EventType.FOLLOW -> subjectId // TODO: 这个值和 autoId 是关键，onStop 的时候要存起来
            EventType.INSERT -> {
                val lastEventType = repository.getLastEventType(autoId)
                if (lastEventType == EventType.STEP) autoId - 1 else subjectId
            }
        }
    }


}