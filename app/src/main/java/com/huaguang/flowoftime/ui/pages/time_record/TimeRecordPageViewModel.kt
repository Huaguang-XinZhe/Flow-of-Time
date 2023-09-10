package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.IdState
import com.huaguang.flowoftime.data.models.SharedState
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.utils.DNDManager
import com.huaguang.flowoftime.utils.getEventDate
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
    private val idState: IdState,
    val sharedState: SharedState,
    private val dndManager: DNDManager,
) : ViewModel() {

    private var currentEvent
        get() = sharedState.currentEvent
        set(value) {
            sharedState.currentEvent = value
        }


    init {
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
                val autoId = repository.insertEvent(currentEvent!!) // 存入数据库
                RDALogger.info("autoId = $autoId")
                updateIdState(autoId, eventType)
                updateInputState(autoId, name)
            }

        }

        override fun stopEvent(eventType: EventType) {
            viewModelScope.launch {
                if (withContent(eventType)) {
                    val eventId = if (eventType == EventType.SUBJECT) idState.subject.value
                        else idState.step.value // else 只可能是步骤事项了
                    val duration = calEventDuration(eventId)
                    repository.updateEndTimeAndDuration(eventId, duration)
                } else {
                    currentEvent = updateCurrentEvent()
                    repository.updateEvent(currentEvent!!) // 更新数据库
                }
                spHelper.resetPauseInterval()
            }
//            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
        }
    }

    private fun updateIdState(autoId: Long, eventType: EventType) {
        idState.apply {
            current.value = autoId
            if (eventType == EventType.SUBJECT) {
                subject.value = autoId
            } else if (eventType == EventType.STEP) {
                step.value = autoId // 有新步骤的话，就会对旧的覆盖，所以，stepId 其实最新的步骤事件的 id
            }
        }
    }

    /**
     * 判断一个事项是否有内容事项，或者说有下级
     */
    private fun withContent(eventType: EventType): Boolean {
        idState.apply {
            return (eventType == EventType.SUBJECT && current.value != subject.value) || // 结束的是主题事项，但它却不是当前的，代表有下级
                    (eventType == EventType.STEP && current.value != step.value) // 结束的是步骤事项，但它也不是当前的，那就代表有下级
        }
    }


    /**
     * 每个含有下级的事项，都要减去本事项的暂停间隔，然后还要减去插入事项的总时长。
     */
    private suspend fun calEventDuration(eventId: Long): Duration {
        val stopRequired = repository.getStopRequired(eventId)
        val pauseIntervalDuration = Duration.ofMinutes(stopRequired.pauseInterval.toLong())
        val totalDurationOfSubInsert = repository.calTotalInsertDuration(eventId)
        val standardDuration = Duration.between(stopRequired.startTime, LocalDateTime.now())

        return standardDuration.minus(totalDurationOfSubInsert).minus(pauseIntervalDuration)
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

    /**
     * 结束时更新当前事件的信息，插入事件和当前的主题事件会走这条路（即没有下级的会走这条路）
     */
    private suspend fun updateCurrentEvent(): Event {
        var event: Event? = null

        currentEvent?.let {
            val newEvent = it.copy() // 先复制原始对象
            val autoId = idState.current.value
            // 插入事项不允许有暂停间隔
            val pauseInterval = if (newEvent.type == EventType.INSERT) 0 else {
                repository.getPauseIntervalById(autoId)
            }
            val endTime = LocalDateTime.now()
            val duration = Duration.between(newEvent.startTime, endTime)
                .minus(Duration.ofMinutes(pauseInterval.toLong()))

            newEvent.id = autoId // 必须指定这一条，否则数据库不会更新
            newEvent.endTime = endTime
            newEvent.pauseInterval = pauseInterval
            newEvent.duration = duration

            event = newEvent
        }

        return event!!
    }

    private fun createCurrentEvent(
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


    private fun getParentEventId(type: EventType): Long? {
        idState.apply {
            return when(type) {
                EventType.SUBJECT -> null
                EventType.STEP, EventType.FOLLOW -> subject.value
                EventType.INSERT -> {
                    val parentType = if (subject.value > step.value) EventType.SUBJECT else EventType.STEP
                    if (parentType == EventType.STEP) step.value else subject.value
                }
            }
        }
    }


}