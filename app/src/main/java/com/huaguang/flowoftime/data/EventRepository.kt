package com.huaguang.flowoftime.data

import com.huaguang.flowoftime.coreEventNames
import com.huaguang.flowoftime.utils.EventSerializer
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.LocalDate

class EventRepository(
    val eventDao: EventDao,
    private val dateDurationDao: DateDurationDao
) {

    suspend fun calEventDateDuration(eventDate: LocalDate): Duration {
        var totalDuration = Duration.ZERO
        val events = eventDao.getFilteredEvents(coreEventNames, eventDate)

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
        val subEvents = eventDao.getSubEventsForMainEvent(mainEventId)
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

    suspend fun updateCoreDurationForDate(date: LocalDate, duration: Duration) {
        val dateDuration = dateDurationDao.getDateDuration(date)
        if (dateDuration != null) {
            dateDuration.duration = duration
            dateDurationDao.updateDateDuration(dateDuration)
        } else {
            dateDurationDao.insertDateDuration(DateDuration(date, duration))
        }
    }

}
