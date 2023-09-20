package com.huaguang.flowoftime.ui.pages.time_record

import androidx.lifecycle.ViewModel
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.Action
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.UndoStack
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.ImmutableIdState
import com.huaguang.flowoftime.data.models.Operation
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.getEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * 页面 ViewModel，用于协调当前页面内各个组件的交互，并存储 TimeRecordPage UI 页面的数据，作为其唯一依赖
 */
@HiltViewModel
class EventControlViewModel @Inject constructor(
    val inputState: InputState,
    val sharedState: SharedState,
    val undoStack: UndoStack,
    val repository: EventRepository,
    private val pauseState: PauseState,
    val idState: IdState,
//    private val dndManager: DNDManager,
) : ViewModel() {

    val eventControl = object : EventControl {
        override suspend fun startEvent(startTime: LocalDateTime, name: String, eventType: EventType) {
            val autoId = updateInputState(name, eventType) {
                updateDBOnStart(startTime, name, eventType)
            }

            addOperationToUndoStack(
                action = getActionByTypeOnStart(eventType),
                eventId = autoId,
                idState = idState
            )

            updateIdState(autoId, eventType) // 必须放在入栈的后边才能更新 idState 里边状态的值
        }

        override suspend fun stopEvent(eventType: EventType) {
            val (eventId, pauseInterval) = updateDBOnStop(eventType)

            undoStack.addState(Operation( // 结束后添加到撤销栈
                action = getActionByTypeOnStop(eventType),
                eventId = eventId,
                pauseInterval = pauseInterval,
            ))
//            dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
        }
    }

    private suspend fun updateInputState(
        name: String,
        eventType: EventType,
        updateDB: suspend () -> Long
    ): Long {
        inputState.apply {
            show.value = name.isEmpty() // 不传 name 就不弹输入框，必须放在前边
            newName.value = ""
            intent.value = InputIntent.RECORD
            this.eventType.value = eventType
            val id = updateDB() // 插入更新数据库，计算 Id 的方法，这么做是为了代码简洁美观的同时，保证输入框的快速弹起
            eventId.value = id

            return id
        }
    }

    private suspend fun updateDBOnStart(
        startTime: LocalDateTime,
        name: String,
        eventType: EventType,
    ): Long {
        val newEvent = createCurrentEvent(startTime, name, eventType) // type 由用户与 UI 的交互自动决定
        val autoId = repository.insertEvent(newEvent) // 存入数据库

        if (hasParent(eventType)) { // 如果当前新开始的事件有父事件，那么父事件的 withContent 应当为 true
            collectPauseInterval(eventType)
            repository.updateParentWithContent(newEvent.parentEventId!!) // 有父事件，那其 parentEventId 就不会是 null
        }

        return autoId
    }

    private suspend fun updateDBOnStop(eventType: EventType): Pair<Long, Int> {
        val eventId: Long
        val pauseInterval: Int
        val duration: Duration

        if (withContent(eventType)) { // 这里没有从数据库获取 withContent，效率低，也困难
            RDALogger.info("进入 withContent 块结束事件")
            handlePauseInterval(eventType).let { pair ->
                eventId = pair.first
                pauseInterval = pair.second
            }
            duration = calEventDuration(
                eventId = eventId,
                eventType = eventType,
                startTime = repository.getStartTimeById(eventId),
                pauseIntervalLong = pauseInterval.toLong()
            )
        } else { // 更新没有下级的当前项
            eventId = idState.current.value
            pauseInterval = if (eventType.isInsert()) 0 else pauseState.currentAcc.value // 插入事项不允许有暂停间隔
            RDALogger.info("没有下级的事件结束：pauseInterval = $pauseInterval")
            duration = repository.getStartTimeById(eventId).let {
                Duration.between(it, LocalDateTime.now())
                    .minus(Duration.ofMinutes(pauseInterval.toLong()))
            }
        }

        repository.updateThree(eventId, duration, pauseInterval)

        return Pair(eventId, pauseInterval)
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
                RDALogger.info("结束主题事件：subjectAcc.value = ${subjectAcc.value}")
                subjectAcc.value = 0 // 重置
            } else { // else 只可能是步骤事项了
                eventId = idState.step.value
                totalPauseInterval = stepAcc.value
                RDALogger.info("结束步骤事件：stepAcc.value = ${stepAcc.value}")
                stepAcc.value = 0 // 重置
            }
        }

        return Pair(eventId, totalPauseInterval)
    }

    /**
     * 每个含有下级的事项，都要减去本事项的暂停间隔，然后还要减去插入事项的总时长。
     * @param eventId 它就是那个含有下级事项的父事项 id
     */
    private suspend fun calEventDuration(
        eventId: Long,
        eventType: EventType,
        startTime: LocalDateTime,
        pauseIntervalLong: Long
    ): Duration {
        val pauseIntervalDuration = Duration.ofMinutes(pauseIntervalLong)
        RDALogger.info("pauseIntervalDuration = $pauseIntervalDuration")
        val totalDurationOfSubInsert = repository.calTotalSubInsertDuration(eventId, eventType)
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
    )


    /**
     * 在事件更新到数据库之前，获取当前事件的父事件的 id
     */
    private fun getParentEventId(type: EventType): Long? {
        idState.apply {
            return when(type) {
                EventType.SUBJECT -> null
                EventType.STEP, EventType.FOLLOW, EventType.SUBJECT_INSERT -> subject.value
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

    private fun addOperationToUndoStack(action: Action, eventId: Long, idState: IdState) {
        val immutableIdState = ImmutableIdState(
            current = idState.current.value,
            subject = idState.subject.value,
            step = idState.step.value,
        )

        undoStack.addState(
            Operation(
                action = action,
                eventId = eventId,
                immutableIdState = immutableIdState
            )
        )
    }

    private fun getActionByTypeOnStart(eventType: EventType): Action {
        return when (eventType) {
            EventType.SUBJECT -> Action.SUBJECT_START
            EventType.STEP -> Action.STEP_START
            EventType.SUBJECT_INSERT -> Action.SUBJECT_INSERT_START
            EventType.STEP_INSERT -> Action.STEP_INSERT_START
            EventType.FOLLOW -> Action.FOLLOW_START
        }
    }

    private fun getActionByTypeOnStop(eventType: EventType): Action {
        return when (eventType) {
            EventType.SUBJECT -> Action.SUBJECT_END
            EventType.STEP -> Action.STEP_END
            EventType.SUBJECT_INSERT -> Action.SUBJECT_INSERT_END
            EventType.STEP_INSERT -> Action.STEP_INSERT_END
            EventType.FOLLOW -> Action.FOLLOW_END
        }
    }

}