package com.huaguang.flowoftime

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.huaguang.flowoftime.utils.vibrate

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 这里执行的代码是在闹钟触发时要做的事情
        // 这可能包括显示通知，启动服务等
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = TimeStreamApplication.NOTIFICATION_CHANNEL_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("今日提醒")
            .setContentText("当下核心事务已经达到 8 小时了！去清理待办吧。")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 这里需要使用你自己应用中的图标
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

        // 振动 1 秒
        vibrate(context)

        val workRequest = OneTimeWorkRequestBuilder<MyAlarmWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

    }
}

