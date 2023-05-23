package com.huaguang.flowoftime.data

import com.huaguang.flowoftime.names
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

}
