package com.huaguang.flowoftime.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun formatLocalDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(formatter)
}

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val remainingMinutes = duration.minusHours(hours).toMinutes()
    return if (hours == 0L) {
        "${remainingMinutes}分钟"
    } else {
        "${hours}小时${remainingMinutes}分钟"
    }
}
