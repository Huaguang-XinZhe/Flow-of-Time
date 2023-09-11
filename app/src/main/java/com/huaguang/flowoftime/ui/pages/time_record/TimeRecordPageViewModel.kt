package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.SharedState
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

    private var stepOver = false // TODO: 这个值要存起来

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

                if (eventType == EventType.STEP) stepOver = false // 重置
            }

        }

        override fun stopEvent(eventType: EventType) {
            viewModelScope.launch {
                if (withContent(eventType)) {
                    RDALogger.info("进入 withContent 块结束事件")
                    val eventId = if (eventType == EventType.SUBJECT) idState.subject.value
                        else idState.step.value // else 只可能是步骤事项了

                    val duration = calEventDuration(eventId)
                    repository.updateEndTimeAndDurationById(eventId, duration)

                    if (eventType == EventType.STEP) stepOver = true // 为了在插入事件时获取正确的 parentId
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
     * 每个含有下级的事项，都要减去本事项的暂停间隔，然后还要减去插入事项的总时长。
     * @param eventId 它就是那个含有下级事项的父事项 id
     */
    private suspend fun calEventDuration(eventId: Long): Duration {
        val stopRequired = repository.getStopRequired(eventId)
        val pauseIntervalDuration = Duration.ofMinutes(stopRequired.pauseInterval.toLong())
        RDALogger.info("pauseIntervalDuration = $pauseIntervalDuration")
        val totalDurationOfSubInsert = repository.calTotalSubInsertDuration(eventId)
        RDALogger.info("totalDurationOfSubInsert = $totalDurationOfSubInsert")
        val standardDuration = Duration.between(stopRequired.startTime, LocalDateTime.now())
        RDALogger.info("standardDuration = $standardDuration")

        return standardDuration.minus(totalDurationOfSubInsert).minus(pauseIntervalDuration)
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
            val autoId = idState.current.value
            // 插入事项不允许有暂停间隔
            val pauseInterval = if (it.type == EventType.INSERT) 0 else {
                repository.getPauseIntervalById(autoId)
            }
            val endTime = LocalDateTime.now()
            val duration = Duration.between(it.startTime, endTime)
                .minus(Duration.ofMinutes(pauseInterval.toLong()))

            it.id = autoId // 必须指定这一条，否则数据库不会更新
            it.endTime = endTime
            it.pauseInterval = pauseInterval
            it.duration = duration

            event = it
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
                    val parentType = if (subject.value > step.value) EventType.SUBJECT
                        else if (stepOver) EventType.SUBJECT else EventType.STEP
                    RDALogger.info("stepOver = $stepOver，parentType = $parentType")
                    if (parentType == EventType.STEP) step.value else subject.value
                }
            }
        }
    }


}