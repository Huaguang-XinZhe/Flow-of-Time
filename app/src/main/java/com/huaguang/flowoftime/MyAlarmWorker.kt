package com.huaguang.flowoftime

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MyAlarmWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val application = applicationContext as TimeStreamApplication
        val database = application.database
        // 你的数据库操作
        // TODO:  
        Toast.makeText(application,
            "这是后台任务的 doWork 块，它执行了，这里以后可以做些数据库操作！", Toast.LENGTH_SHORT).show()

        return Result.success()
    }
}
