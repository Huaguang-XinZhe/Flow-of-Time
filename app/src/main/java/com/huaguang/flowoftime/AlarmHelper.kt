package com.huaguang.flowoftime

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class AlarmHelper(private val context: Context) {

    companion object {
        const val ALARM_ID = 0
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, MyAlarmReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            ALARM_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun setAlarm(remainingTime: Long) {
        val alarmTime = System.currentTimeMillis() + remainingTime
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
    }

    fun cancelAlarm() {
        alarmManager.cancel(pendingIntent)
    }
}

