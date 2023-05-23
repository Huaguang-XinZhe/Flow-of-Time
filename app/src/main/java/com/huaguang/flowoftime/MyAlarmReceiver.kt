package com.huaguang.flowoftime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 这里执行的代码是在闹钟触发时要做的事情
        // 这可能包括显示通知，启动服务等
        Toast.makeText(context, "Alarm received!", Toast.LENGTH_LONG).show()
    }
}

