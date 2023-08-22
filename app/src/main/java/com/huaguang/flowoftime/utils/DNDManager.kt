package com.huaguang.flowoftime.utils

import android.app.NotificationManager
import android.content.Context

class DNDManager(private val context: Context) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val isDNDEnabled: Boolean
        get() = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY

    fun openDND() {
        if (!notificationManager.isNotificationPolicyAccessGranted) return

        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
    }

    fun closeDND() {
        if (!notificationManager.isNotificationPolicyAccessGranted && !isDNDEnabled) return

        notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return notificationManager.isNotificationPolicyAccessGranted
    }
}