package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.Action
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.UndoStack
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.Operation
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.PauseState
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
    private val idState: IdState,
    val sharedState: SharedState,
    private val pauseState: PauseState,
    private val dndManager: DNDManager,
    private val undoStack: UndoStack<Operation>,
) : ViewModel() {

    private var currentEvent
        get() = sharedState.currentEvent
        set(value) {
            sharedState.currentEvent = value
        }

    var pauseAcc = 0 // 临时变量，不需要保存，只在结束那一瞬起作用

    init {
        // 这个协程会优先于上一个协程的执行，不知道为什么。这个协程只会执行一次，而上面那个协程被挂起，有新值的时候就会执行。
        viewModelScope.launch {
            if (currentEvent == null) currentEvent = repository.getCurrentEvent()
            RDALogger.info("currentEvent = $currentEvent")
        }


    }

    val eventControl = object : EventControl {
        override suspend fun startEvent(startTime: LocalDateTime, name: String, eventType: EventType) {
            currentEvent = createCurrentEvent(startTime, name, eventType) // type 由用户与 UI 的交互自动决定
            val autoId = repository.insertEvent(currentEvent!!) // 存入数据库

            if (hasParent(eventType)) { // 如果当前新开始的事件有父事件，那么父事件的 withContent 应当为 true
                collectPauseInterval(eventType)
                repository.updateParentWithContent(currentEvent!!.parentEventId!!) // 有父事件，那其 parentEventId 就不会是 null
            }

            updateIdState(autoId, eventType)
            updateInputState(autoId, name)

            undoStack.addState(Operation( // 将当前操作添加到撤销栈
                action = getActionByType(eventType),
                eventId = autoId,
            ))
        }

        override suspend fun stopEvent(eventType: EventType) {
            if (withContent(eventType)) { // 这里没有从数据库获取 withContent，效率低，也困难
                RDALogger.info("进入 withContent 块结束事件")
                val (eventId, totalPauseInterval) = handlePauseInterval(eventType)
                val duration = calEventDuration(eventId)
                repository.updateThree(eventId, duration, totalPauseInterval)
            } else {
                currentEvent = updateCurrentEvent()
                repository.updateEvent(currentEvent!!) // 更新数据库

            }
//            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
        }
    }

    private fun getActionByType(eventType: EventType): Action {
        return when (eventType) {
            EventType.SUBJECT -> Action.SUBJECT_START
            EventType.STEP -> Action.STEP_START
            EventType.SUBJECT_INSERT -> Action.SUBJECT_INSERT_START
            EventType.STEP_INSERT -> Action.STEP_INSERT_START
            EventType.FOLLOW -> Action.FOLLOW_START
        }
    }

    /**
     * pauseInterval 的确立和重置逻辑
     */
    private fun handlePauseInterval(eventType: EventType): Pair<Long, Int> {
        val eventId: Long
        val totalPauseInterval: Int

        pauseState.apply {
            if (eventType == EventType.SUBJECT) {
                eventId = idState.subject.value
                totalPauseInterval = subjectAcc.value
                subjectAcc.value = 0 // 重置
            } else { // else 只可能是步骤事项了
                eventId = idState.step.value
                totalPauseInterval = stepAcc.value
                stepAcc.value = 0 // 重置
            }
        }

        return Pair(eventId, totalPauseInterval)
    }

    /**
     * 每个含有下级的事项，都要减去本事项的暂停间隔，然后还要减去插入事项的总时长。
     * @param eventId 它就是那个含有下级事项的父事项 id
     */
    private suspend fun calEventDuration(eventId: Long): Duration {
        val startTime = repository.getStartTimeOfWithContentEvent(eventId)
        val pauseIntervalDuration = Duration.ofMinutes(pauseState.subjectAcc.value.toLong())
//        RDALogger.info("pauseIntervalDuration = $pauseIntervalDuration")
        val totalDurationOfSubInsert = repository.calTotalSubInsertDuration(eventId)
//        RDALogger.info("totalDurationOfSubInsert = $totalDurationOfSubInsert")
        val standardDuration = Duration.between(startTime, LocalDateTime.now())
//        RDALogger.info("standardDuration = $standardDuration")

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
    private fun updateCurrentEvent(): Event {
        var event: Event? = null

        currentEvent?.let {
            val autoId = idState.current.value
            // 插入事项不允许有暂停间隔
            val pauseInterval = if (it.type.isInsert()) 0 else pauseAcc
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


    /**
     * 在事件更新到数据库之前，获取当前事件的父事件的 id
     */
    private fun getParentEventId(type: EventType): Long? {
        idState.apply {
            return when(type) {
                EventType.SUBJECT -> null
                EventType.STEP, EventType.FOLLOW -> subject.value
                EventType.SUBJECT_INSERT -> subject.value
                EventType.STEP_INSERT -> step.value
            }
        }
    }


    /**
     * 判断当前新开始的事件是否有父事件，有的话，说明父事件有下级，那 withContent 就为 true。
     * 像这种语义不直观的判断，就独立成一个语义明确的方法，哪怕判断很简单。
     */
    private fun hasParent(type: EventType) = type != EventType.SUBJECT

    /**
     * 收集含有下级的事件（主题或步骤）的暂停间隔
     */
    private fun collectPauseInterval(eventType: EventType) {
        pauseState.apply {
            if (acc.value == 0) return

            RDALogger.info("收集开始")
            val totalInterval = if (eventType == EventType.STEP_INSERT) stepAcc else subjectAcc
            RDALogger.info("totalInterval = $totalInterval")
            totalInterval.value += acc.value
        }
    }


}