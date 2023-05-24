package com.huaguang.flowoftime.data

import com.huaguang.flowoftime.names
import com.huaguang.flowoftime.utils.EventSerializer
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class EventRepository(val eventDao: EventDao) {

    fun getEventDate(startTime: LocalDateTime): LocalDate {
        return if (startTime.hour in 0..4) {
            startTime.toLocalDate().minusDays(1)
        } else {
            startTime.toLocalDate()
        }
    }

    suspend fun calculateTotalDuration(): Duration {
        var totalDuration = Duration.ZERO
        val events = eventDao.getFilteredEvents(names, LocalDate.now())

        // 当 events 为空，里边的代码就不会执行
        for (event in events) {
            totalDuration = totalDuration.plus(event.duration)
        }

        return totalDuration
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

}
