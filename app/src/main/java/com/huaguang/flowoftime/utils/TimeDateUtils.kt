package com.huaguang.flowoftime.utils

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun getEventDate(startTime: LocalDateTime): LocalDate {
    return if (startTime.hour in 0..4) {
        startTime.toLocalDate().minusDays(1)
    } else {
        startTime.toLocalDate()
    }
}

fun getAdjustedDate(): LocalDate {
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

/**
 * 把 LocalDate 转换为 “09-26 周二” 这样的格式。
 */
fun formatDateToCustomPattern(date: LocalDate): String {
    val monthDayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
    return "${monthDayFormatter.format(date)} $dayOfWeek"
}


fun formatLocalDateTime(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return dateTime.format(formatter)
}

fun formatDurationInText(duration: Duration, englishUnit: Boolean = true): String {
    val hUnit: String
    val mUnit: String
    val hours = duration.toHours()
    val remainingMinutes = duration.minusHours(hours).toMinutes()

    if (englishUnit) {
        hUnit = "h"
        mUnit = "m"
    } else {
        hUnit = "小时"
        mUnit = "分钟"
    }

    return when {
        hours == 0L && remainingMinutes == 0L -> ""
        hours == 0L -> "$remainingMinutes$mUnit"
        remainingMinutes == 0L -> "$hours$hUnit"
        else -> "$hours$hUnit$remainingMinutes$mUnit"
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
