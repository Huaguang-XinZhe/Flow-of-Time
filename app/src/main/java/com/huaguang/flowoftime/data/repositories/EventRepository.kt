package com.huaguang.flowoftime.data.repositories

import com.ardakaplan.rdalogger.RDALogger

import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.coreEventKeyWords
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.db_returns.DateDuration
import com.huaguang.flowoftime.data.models.db_returns.EventTimes
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.other.EventWithSubEvents
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.utils.EventSerializer
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class EventRepository(
    private val eventDao: EventDao,
    private val dateDurationDao: DateDurationDao
) {

    suspend fun deleteEventsExceptToday() {
        val customToday = getAdjustedEventDate()

        withContext(Dispatchers.IO) {
            eventDao.deleteEventsExceptToday(customToday)
        }
    }

    suspend fun getSubEventTimesWithinRange(
        mainEventId: Long,
        startCursor: LocalDateTime?,
    ): List<EventTimes> =
        withContext(Dispatchers.IO) {
            eventDao.getSubEventTimesWithinRange(mainEventId, startCursor)
        }

    suspend fun calEventDateDuration(eventDate: LocalDate): Duration {
        var totalDuration = Duration.ZERO
        val events = eventDao.getFilteredEvents(coreEventKeyWords, eventDate)

        // 当 events 为空，里边的代码就不会执行
        for (event in events) {
            totalDuration = totalDuration.plus(event.duration)
        }

        return totalDuration
    }

    fun getRecentTwoDaysEvents(): Flow<List<EventWithSubEvents>> {
        val customToday = getAdjustedEventDate()
        return eventDao.getEventsWithSubEvents(customToday.minusDays(1), customToday)
    }

    fun getCustomTodayEvents(): Flow<List<EventWithSubEvents>> {
        return eventDao.getEventsWithSubEvents(getAdjustedEventDate())
    }

    suspend fun calculateSubEventsDuration(mainEventId: Long): Duration {
        val subEvents = withContext(Dispatchers.IO) {
            eventDao.getContentEventsForEvent(mainEventId)
        }
        return subEvents.fold(Duration.ZERO) { total, event -> total.plus(event.duration) }
    }

    /**
     * @param tag 根据标签选择是导出昨天的数据还是全部的数据。
     */
    suspend fun exportEvents(tag: String): String {

        fun convertToPair(eventWithSubEvents: EventWithSubEvents): Pair<Event, List<Event>> {
            return Pair(eventWithSubEvents.event, eventWithSubEvents.subEvents)
        }

        val eventsWithSubEvents = if (tag == "昨日") {
            val customYesterday = getAdjustedEventDate().minusDays(1)
            eventDao.getYesterdayEventsWithSubEventsImmediate(customYesterday)
        } else {
            eventDao.getEventsWithSubEventsImmediate()
        }

        val eventsWithSubEventsAsPairs = eventsWithSubEvents.map { convertToPair(it) }
        return EventSerializer.exportEvents(eventsWithSubEventsAsPairs)
    }

    suspend fun saveCoreDurationForDate(date: LocalDate, duration: Duration) {
        withContext(Dispatchers.IO) {
            // 之所以要先查是为了应对晚睡更改为其他事项而后又有可能会改回来的情况，所以需要更新方法，而不仅仅是插入。
            val dateDuration = dateDurationDao.getDateDuration(date)

            if (dateDuration != null) {
                dateDuration.let {
                    it.duration = duration
                    it.durationStr = formatDurationInText(duration)

                    dateDurationDao.updateDateDuration(it)
                }
            } else {
                dateDurationDao.insertDateDuration(
                    DateDuration(date, duration, formatDurationInText(duration))
                )
            }
        }
    }


    suspend fun updateEvent(event: Event) {
        withContext(Dispatchers.IO) {
            eventDao.updateEvent(event)
        }
    }

    suspend fun insertEvent(event: Event) =
        withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }


    suspend fun getOffsetStartTimeForSubject(): LocalDateTime? {
        val endTime = withContext(Dispatchers.IO) {
            eventDao.getLastSubjectEndTime()
        }
        val interval = Duration.ofMinutes(2L)

        return endTime?.plus(interval) // 如果数据库不为空的话，一般 endTime 不为 null
    }

    suspend fun getOffsetStartTimeForStep(idState: IdState): LocalDateTime {
        val interval = Duration.ofMinutes(2L)

        val time = withContext(Dispatchers.IO) {
            with(idState) { // 使用 with 才能把引用传进来并返回块内的值，apply 虽然会传引用，但不会返回块内值，而是返回接收者
                if (subject.value > step.value) { // 只有一个主题事件，没有步骤
                    eventDao.getStartTimeById(subject.value)
                } else { // 在有步骤存在的情况下，长按补计步骤
                    eventDao.getEndTimeById(step.value)
                }
            }
        }

        return time.plus(interval)
    }

    suspend fun deleteEventWithSubEvents(
        event: Event,
        subEvents: List<Event> = listOf()
    ) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(event.id)
            for (subEvent in subEvents) {
                eventDao.deleteEvent(subEvent.id)
            }
        }
    }

    suspend fun deleteEvent(eventId: Long) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(eventId = eventId)
        }
    }


    suspend fun getLastMainEvent() =
        withContext(Dispatchers.IO) {
            eventDao.getLastMainEvent()
        }

    suspend fun getLastEvent() =
        withContext(Dispatchers.IO) {
            eventDao.getLastEvent()
        }

    suspend fun getCurrentEvent() =
        withContext(Dispatchers.IO) {
            eventDao.getCurrentEvent()
        }


    suspend fun updateTimeAndDuration(newCustomTime: CustomTime, newDuration: Duration?) {
        val eventId = newCustomTime.eventInfo.id
        val newTime = newCustomTime.timeState.value!!

        withContext(Dispatchers.IO) {
            // TODO: 这里根据开始或结束时间来更新事件可能会出现问题
            if (newCustomTime.type == TimeType.START) {
                RDALogger.info("更新开始时间！")
                eventDao.updateStartTimeAndDurationById(eventId, newTime, newDuration)
            } else {
                RDALogger.info("更新结束时间！")
                eventDao.updateEndTimeAndDurationById(eventId, newTime, newDuration!!) // 调整结束时间时的新 duration 绝不为 0！
            }
        }
    }

    suspend fun updateEventName(id: Long, newName: String) {
        withContext(Dispatchers.IO) {
            eventDao.updateEventName(id, newName)
        }
    }

    suspend fun getCombinedEvents(): List<CombinedEvent> {
        val allEventsMap = withContext(Dispatchers.IO) {
            eventDao.getAllEvents().associateBy { it.id }
        }
        return buildCombinedEvents(allEventsMap, null)
    }

    fun getCurrentCombinedEventFlow(): Flow<CombinedEvent?> { // 当数据库中没有数据的时候，将发射 null
        return eventDao.getLatestRootEventAndChildren().map { allEvents ->
            buildCombinedEventFromEvents(allEvents)
        }
    }

    fun getSecondLatestCombinedEventFlow(): Flow<CombinedEvent?> {
        return eventDao.getSecondLatestRootEventAndChildren().map { allEvents ->
            buildCombinedEventFromEvents(allEvents)
        }
    }

    private fun buildCombinedEventFromEvents(allEvents: List<Event>): CombinedEvent? {
        if (allEvents.isEmpty()) return null

        val latestRootEvent = allEvents[0]

        return if (allEvents.size == 1) {
            // 如果没有找到子事件，只创建一个包含根事件的 CombinedEvent
            CombinedEvent(
                event = latestRootEvent,
                contentEvents = listOf()
            )
        } else {
            // 如果找到了子事件，创建一个包含根事件和所有子事件的 CombinedEvent
            val allEventsMap = allEvents.associateBy { it.id }
            buildCombinedEvent(allEventsMap, latestRootEvent)
        }
    }



    private fun buildCombinedEvent(allEventsMap: Map<Long, Event>, rootEvent: Event): CombinedEvent {
        return CombinedEvent(
            event = rootEvent,
            contentEvents = buildCombinedEvents(allEventsMap, rootEvent.id)
        )
    }

    private fun buildCombinedEvents(allEventsMap: Map<Long, Event>, parentId: Long?): List<CombinedEvent> {
        return allEventsMap.values.filter { it.parentEventId == parentId }.map { event ->
            CombinedEvent(
                event = event,
                contentEvents = buildCombinedEvents(allEventsMap, event.id)
            )
        }
    }

    suspend fun updateThree(
        eventId: Long,
        newDuration: Duration?,
        pauseInterval: Int,
        endTime: LocalDateTime? = LocalDateTime.now(),
    ) {
        withContext(Dispatchers.IO) {
            eventDao.updateThree(eventId, newDuration, pauseInterval, endTime)
        }
    }


    suspend fun calTotalSubInsertDuration(
        id: Long,
        eventTypes: List<EventType> = listOf(EventType.SUBJECT_INSERT, EventType.STEP_INSERT)
    ): Duration {
        val insertDurationList = withContext(Dispatchers.IO) {
            eventDao.getSubInsertDurationList(id, eventTypes)
        }
        return insertDurationList.fold(Duration.ZERO) { total, duration ->
            total.plus(duration)
        }
    }

    suspend fun getEventById(id: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getEventById(id)
        }


    suspend fun getStartTimeById(eventId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getStartTimeById(eventId)
        }


    suspend fun getDurationById(eventId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getDurationById(eventId)
        }

    suspend fun getInsertParentById(parentId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getInsertParentById(parentId)
        }

    suspend fun updateDuration(id: Long, newDuration: Duration) {
        withContext(Dispatchers.IO) {
            eventDao.updateDurationById(id, newDuration)
        }
    }

    suspend fun updateParentWithContent(parentId: Long) {
        withContext(Dispatchers.IO) {
            eventDao.updateWithContentById(parentId, true)
        }
    }


}
