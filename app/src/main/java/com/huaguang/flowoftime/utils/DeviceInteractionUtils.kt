package com.huaguang.flowoftime.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.VibrationEffect
import android.os.Vibrator

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

fun getNavigationBarHeight(context: Context) {
    val resources: Resources = context.resources
    val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        val navigationBarHeight: Int = resources.getDimensionPixelSize(resourceId)
        // You now have the navigation bar height
    }

}