package com.huaguang.flowoftime.data.repositories

import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.DEFAULT_EVENT_INTERVAL
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.coreEventKeyWords
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.DateDuration
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.data.models.EventWithSubEvents
import com.huaguang.flowoftime.utils.EventSerializer
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
        val eventsWithSubEvents = if (tag == "昨日") {
            val customYesterday = getAdjustedEventDate().minusDays(1)
            eventDao.getYesterdayEventsWithSubEventsImmediate(customYesterday)
        } else {
            eventDao.getEventsWithSubEventsImmediate()
        }

        val eventsWithSubEventsAsPairs = eventsWithSubEvents.map { convertToPair(it) }
        return EventSerializer.exportEvents(eventsWithSubEventsAsPairs)
    }

    private fun convertToPair(eventWithSubEvents: EventWithSubEvents): Pair<Event, List<Event>> {
        return Pair(eventWithSubEvents.event, eventWithSubEvents.subEvents)
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
            RDALogger.info("updateEvent 被调用！")
            eventDao.updateEvent(event)
        }
    }

    suspend fun fetchMainEventId() =
        withContext(Dispatchers.IO) {
            eventDao.getLatestMainEventId()
        }

    suspend fun insertEvent(event: Event) =
        withContext(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }


    suspend fun hasSubEvents(parentId: Long?): Boolean {
        return parentId?.let { eventDao.countSubEvents(it) > 0 } ?: false
    }


    suspend fun getOffsetStartTime(): LocalDateTime? {
        val lastEvent = withContext(Dispatchers.IO) {
            eventDao.getLastMainEvent()
        }
        return lastEvent?.endTime?.plus(DEFAULT_EVENT_INTERVAL)
            ?: lastEvent?.startTime?.plus(DEFAULT_EVENT_INTERVAL)
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

    suspend fun getEventWithSubEvents(eventId: Long) =
        withContext(Dispatchers.IO) {
            eventDao.getEventsWithSubEvents(eventId)
        }

    suspend fun getLastEvent(eventId: Long?): Event {
        val maxId = withContext(Dispatchers.IO) {
            eventDao.getMaxId() ?: 0L
        }
        val lastId = if (eventId == null) maxId else maxId - 1

        return withContext(Dispatchers.IO) {
            eventDao.getEvent(lastId)
        }
    }

    fun getCurrentEventFlow() = eventDao.getCurrentEventFlow()

    suspend fun getCurrentEvent() = eventDao.getCurrentEvent()

    suspend fun updateDatabase(newCustomTime: CustomTime, newDuration: Duration?) {
        val initialTime = newCustomTime.initialTime!!
        val newTime = newCustomTime.timeState.value!!

        withContext(Dispatchers.IO) {
            // TODO: 这里根据开始或结束时间来更新事件可能会出现问题
            if (newCustomTime.type == TimeType.START) {
                RDALogger.info("更新开始时间！")
                eventDao.updateEventByStartTime(initialTime, newTime, newDuration)
            } else {
                RDALogger.info("更新结束时间！")
                eventDao.updateEventByEndTime(initialTime, newTime, newDuration!!) // 调整结束时间时的新 duration 绝不为 0！
            }
        }
    }

    suspend fun updateEventName(id: Long, newName: String) {
        withContext(Dispatchers.IO) {
            eventDao.updateEventName(id, newName)
        }
    }

}
