package com.huaguang.flowoftime.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
