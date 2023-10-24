package com.huaguang.flowoftime.data.repositories

import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.coreEventKeyWords
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.db_returns.EventTimes
import com.huaguang.flowoftime.data.models.db_returns.SimpleEvent
import com.huaguang.flowoftime.data.models.db_returns.StopRequire
import com.huaguang.flowoftime.data.models.tables.DateDuration
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.other.EventWithSubEvents
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.utils.EventSerializer
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EventRepository(
    private val eventDao: EventDao,
    private val dateDurationDao: DateDurationDao,
) {

    suspend fun deleteEventsExceptToday() {
        val customToday = getAdjustedDate()

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
        val customToday = getAdjustedDate()
        return eventDao.getEventsWithSubEvents(customToday.minusDays(1), customToday)
    }

    fun getRecentTwoDaysCombinedEventsFlow(): Flow<List<CombinedEvent?>> {
        val customToday = getAdjustedDate()
        return eventDao.getAllWithinRangeEvents(
            startDate = customToday.minusDays(1),
            endDate = customToday,
        ).map { recentTwoDaysEvents -> // 这个 map 的作用是把 List<Event> 对象转换成 List<CombinedEvent> 对象
            val eventsMap = recentTwoDaysEvents.associateBy { it.id } // 为所有查询到的事件建立映射（用事件的 id 映射事件本身）
            buildCombinedEvents(eventsMap, null) // 这个函数会得到一个复合事件的列表
        }
    }

    /**
     * 获取上次性泄和今天间隔的天数的 Flow（转换而成）
     */
    fun getLatestXXXIntervalDaysFlow(): Flow<Int> {
        val customToday = getAdjustedDate()
        return eventDao.getLatestXXXDate().map { date -> // date 是上次性泄的日期
            date?.let { ChronoUnit.DAYS.between(it, customToday).toInt() } ?: -1
        }
    }

    suspend fun getCombinedEvents(): List<CombinedEvent> {
        val allEventsMap = withContext(Dispatchers.IO) {
            eventDao.getAllEvents().associateBy { it.id }
        }
        return buildCombinedEvents(allEventsMap, null)
    }

    fun getCustomTodayEvents(): Flow<List<EventWithSubEvents>> {
        return eventDao.getEventsWithSubEvents(getAdjustedDate())
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
            val customYesterday = getAdjustedDate().minusDays(1)
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


    suspend fun updateTimeAndDuration(newCustomTime: CustomTime, newDuration: Duration?) {
        val eventId = newCustomTime.eventInfo.id
        val newTime = newCustomTime.timeState.value!!

        withContext(Dispatchers.IO) {
            if (newCustomTime.type == TimeType.START) {
                eventDao.updateStartTimeAndDurationById(eventId, newTime, newDuration)
            } else {
                eventDao.updateEndTimeAndDurationById(eventId, newTime, newDuration!!) // 调整结束时间时的新 duration 绝不为 0！
            }
        }
    }

    suspend fun updateNameAndCategory(id: Long, newName: String, newCategory: String?) {
        withContext(Dispatchers.IO) {
            eventDao.updateNameAndCategoryById(id, newName, newCategory)
        }
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
        eventType: EventType,
    ): Duration {
        val insertDurationList = withContext(Dispatchers.IO) {
            if (eventType == EventType.SUBJECT) {
                val eventTypes = listOf(EventType.SUBJECT_INSERT, EventType.STEP_INSERT)
                eventDao.getItemInsertDurationList(id, eventTypes)
            } else { // 也只能是步骤了（结束）
                eventDao.getStepInsertDurationList(id, EventType.STEP_INSERT)
            }
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

    suspend fun updateTags(id: Long, tags: MutableList<String>) {
        withContext(Dispatchers.IO) {
            eventDao.updateTags(id, tags)
        }
    }

    suspend fun updateCategory(id: Long, category: String) {
        withContext(Dispatchers.IO) {
            eventDao.updateCategory(id, category)
        }
    }

    suspend fun updateClassName(id: Long, category: String, tags: MutableList<String>?) {
        withContext(Dispatchers.IO) {
            eventDao.updateClassName(id, category, tags)
        }
    }

    suspend fun getAllEvents() =
        withContext(Dispatchers.IO) {
            eventDao.getAllEvents()
        }

    suspend fun getEventsByIdRange(startId: Long, endId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getEventsByIdRange(startId, endId)
        }

    suspend fun getDateAndCategoryById(subjectId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getDateAndCategoryById(subjectId)
        }

    suspend fun getStopRequire(eventId: Long): StopRequire =
        withContext(Dispatchers.IO) {
            eventDao.getStopRequireById(eventId)
        }

    suspend fun getEventCategoryInfoById(eventId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getEventCategoryInfoById(eventId)
        }

    fun getCombinedEventsByDateCategoryFlow(date: LocalDate, category: String?): Flow<List<CombinedEvent>> {
        val allEventsFlow = eventDao.getAllEventsByDateFlow(date)
        val parentIdListFlow = eventDao.getIdListByDateCategoryFlow(date, category)

        return combine(allEventsFlow, parentIdListFlow) { allEvents, parentIdList ->
            val allEventsMap = allEvents.associateBy { it.id }
            parentIdList.map { eventId ->
                CombinedEvent(
                    event = allEventsMap[eventId]!!,
                    contentEvents = buildCombinedEvents(allEventsMap, eventId)
                )
            }
        }.flowOn(Dispatchers.IO)
    }


    fun getKeyTimePointsByDate(date: LocalDate) =
        eventDao.getKeyTimePointsByDate(date)

    suspend fun deleteEventsByDate(date: LocalDate) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEventsByDate(date)
        }
    }

    suspend fun getDateDurationById(id: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getDateDurationById(id)
        }

    suspend fun getXXXEvents() =
        withContext(Dispatchers.IO) {
            eventDao.getEventsByCategory("xxx")
        }

    /**
     * 查询昨天的所有事件，返回 SimpleEvent 列表
     */
    suspend fun getYesterdayEvents(): List<SimpleEvent> {
        val customYesterday = getAdjustedDate().minusDays(1)
        return withContext(Dispatchers.IO) {
            eventDao.getYesterdaySimpleEvents(customYesterday)
        }
    }

}
