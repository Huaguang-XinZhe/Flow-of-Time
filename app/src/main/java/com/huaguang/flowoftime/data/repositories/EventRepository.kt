package com.huaguang.flowoftime.data.repositories

import com.huaguang.flowoftime.DEFAULT_EVENT_INTERVAL
import com.huaguang.flowoftime.coreEventKeyWords
import com.huaguang.flowoftime.data.dao.DateDurationDao
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.DateDuration
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.data.models.EventWithSubEvents
import com.huaguang.flowoftime.utils.EventSerializer
import com.huaguang.flowoftime.utils.extensions.formatDurationInText
import com.huaguang.flowoftime.utils.extensions.getAdjustedEventDate
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

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        eventDao.deleteAllEvents()
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
            eventDao.getSubEventsForMainEvent(mainEventId)
        }
        return subEvents.fold(Duration.ZERO) { total, event -> total.plus(event.duration) }
    }

    suspend fun exportEvents(): String {
        val eventsWithSubEvents = eventDao.getEventsWithSubEventsImmediate()
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
            eventDao.updateEvent(event)
        }
    }

    suspend fun fetchMainEventId() =
        withContext(Dispatchers.IO) {
            eventDao.getLastMainEventId() // 在插入子事件之前一定存在主事件，不会有问题
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

}
