package com.huaguang.flowoftime

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: Button

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingButton = Button(application).apply {
            text = "点击我"
            setOnClickListener {
                Toast.makeText(application, "悬浮窗被点击了！", Toast.LENGTH_SHORT).show()
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(floatingButton, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(floatingButton)
    }
}
