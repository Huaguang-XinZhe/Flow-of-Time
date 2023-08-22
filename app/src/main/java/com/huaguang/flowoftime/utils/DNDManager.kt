package com.huaguang.flowoftime.utils

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

class DNDManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val isDNDEnabled: Boolean
        get() = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY

    /**
     * 只有在充分授权的前提下才能调用，否则会抛出异常
     */
    fun openDND() {
        try {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } catch (e: SecurityException) {
            Log.e("打标签喽", "无法打开勿扰模式，缺少必要的权限。", e)
        }
    }

    /**
     * 只有开启了勿扰模式才会关闭。
     * 如果没有授予权限，会抛出异常。
     */
    fun closeDND() {
        try {
            if (isDNDEnabled) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        } catch (e: SecurityException) {
            Log.e("打标签喽", "无法打开勿扰模式，缺少必要的权限。", e)
        }
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun jumpAuth() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        context.startActivity(intent)
    }


}