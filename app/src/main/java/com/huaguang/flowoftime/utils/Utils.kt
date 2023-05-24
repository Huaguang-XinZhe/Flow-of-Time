package com.huaguang.flowoftime.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
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
    return when {
        hours == 0L && remainingMinutes == 0L -> ""
        hours == 0L -> "${remainingMinutes}分钟"
        remainingMinutes == 0L -> "${hours}小时"
        else -> "${hours}小时${remainingMinutes}分钟"
    }
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
