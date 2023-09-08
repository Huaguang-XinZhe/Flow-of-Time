package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventControl
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

    private val _currentCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val currentCombinedEventFlow: StateFlow<CombinedEvent?> = _currentCombinedEventFlow.asStateFlow()
    private val _secondLatestCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val secondLatestCombinedEventFlow: StateFlow<CombinedEvent?> = _secondLatestCombinedEventFlow.asStateFlow()

    var autoId = 0L
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
        override fun startEvent(startTime: LocalDateTime, eventType: EventType) {
            viewModelScope.launch {
                currentEvent = createCurrentEvent(startTime, eventType) // type 由用户与 UI 的交互自动决定
                autoId = repository.insertEvent(currentEvent!!) // 存入数据库
                if (eventType == EventType.SUBJECT) subjectId = autoId // 存储当前项的主题事件的 id
                updateInputState(autoId)
            }

        }

        override fun stopEvent(eventType: EventType) {
            viewModelScope.launch {
                if (notCurrentSubjectEvent(eventType)) {
                    endFinalSubject()
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
    private fun notCurrentSubjectEvent(eventType: EventType) = eventType == EventType.SUBJECT && autoId != subjectId

    private suspend fun endFinalSubject() {
        // TODO: 有待细化完善
        repository.updateEventEndTimeById(subjectId)
    }

    private fun updateInputState(id: Long) {
        eventInputViewModel.inputState.apply {
            eventId.value = id
            show.value = true
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
        type: EventType,
    ) = Event(
        startTime = startTime,
        eventDate = getEventDate(startTime),
        parentEventId = fetchMainEventId(type),
        type = type,
    )


    private suspend fun fetchMainEventId(type: EventType): Long? {
        return if (type == EventType.INSERT || type == EventType.FOLLOW) {
            repository.fetchMainEventId()
        } else null
    }


}