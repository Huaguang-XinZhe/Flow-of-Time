package com.huaguang.flowoftime.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.huaguang.flowoftime.coreEventKeyWords
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun isCoreEvent(name: String): Boolean {
    for (keyWord in coreEventKeyWords) {
        val contains = name.contains(keyWord, true)

        if (contains) return true
    }

    return false
}

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

/**
 * 我自定义的起床时段，这个有待根据个人记录进行定制。todo
 */
fun isWakeUpPeriod(): Boolean {
    val now = LocalTime.now()
    val sevenAM = LocalTime.of(7, 0)
    val elevenAM = LocalTime.of(11, 0)

    return now.isAfter(sevenAM) && now.isBefore(elevenAM)
}


fun isSleepingTime(time: LocalTime): Boolean {
    val sleepStartTime = LocalTime.of(22, 30)
    val sleepEndTime = LocalTime.of(4, 30)
    // 如果时间在 22:30 之后，或者在 4:30 之前，那么这个时间在你的睡眠时间内
    return time.isAfter(sleepStartTime) || time.isBefore(sleepEndTime)
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


fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", text)
    clipboard.setPrimaryClip(clip)
}

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Vibrator::class.java)

    // 检查设备是否有振动器
    if (!vibrator.hasVibrator()) {
        return
    }

    // 检查设备是否有硬件振动器并支持振动效果
    if (vibrator.hasAmplitudeControl()) {
        // 创建一次性振动
        val vibrationEffect =
            VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(vibrationEffect)
    }
}
