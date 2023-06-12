package com.huaguang.flowoftime.utils

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun getEventDate(startTime: LocalDateTime): LocalDate {
    return if (startTime.hour in 0..4) {
        startTime.toLocalDate().minusDays(1)
    } else {
        startTime.toLocalDate()
    }
}

fun getAdjustedEventDate(): LocalDate {
    val now = LocalTime.now()
    val midnight = LocalTime.MIDNIGHT
    val fiveAM = LocalTime.of(5, 0)

    return if (now.isAfter(midnight) && now.isBefore(fiveAM)) {
        // 当前时间是在 0 点与凌晨 5 点之间
        LocalDate.now().minusDays(1)
    } else LocalDate.now()
}

fun isSleepingTime(time: LocalTime): Boolean {
    val sleepStartTime = LocalTime.of(22, 30)
    val sleepEndTime = LocalTime.of(4, 30)
    // 如果时间在 22:30 之后，或者在 4:30 之前，那么这个时间在你的睡眠时间内
    return time.isAfter(sleepStartTime) || time.isBefore(sleepEndTime)
}

/**
 * 判断现在的时间是不是在起床时间的范围内，如果是的话，就认为这是新的一天
 */
fun isGetUpTime(time: LocalTime): Boolean {
    val getUpRangeStart = LocalTime.of(7, 30)
    val getUpRangeEnd = LocalTime.of(11, 0)

    return time.isAfter(getUpRangeStart) && time.isBefore(getUpRangeEnd)
}

fun formatLocalDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(formatter)
}

fun formatDurationInText(duration: Duration): String {
    val hours = duration.toHours()
    val remainingMinutes = duration.minusHours(hours).toMinutes()
    return when {
        hours == 0L && remainingMinutes == 0L -> ""
        hours == 0L -> "${remainingMinutes}分钟"
        remainingMinutes == 0L -> "${hours}小时"
        else -> "${hours}小时${remainingMinutes}分钟"
    }
}

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() - hours * 60
    return "%02d:%02d".format(hours, minutes)
}

fun parseToLocalDateTime(timeStr: String, date: LocalDate): LocalDateTime {
    val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
    return LocalDateTime.of(date, time)
}
