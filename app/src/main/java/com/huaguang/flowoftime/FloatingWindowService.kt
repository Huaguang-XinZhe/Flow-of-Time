package com.huaguang.flowoftime

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.res.ColorStateList
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.tables.Inspiration
import com.huaguang.flowoftime.data.repositories.InspirationRepository
import com.huaguang.flowoftime.utils.getAdjustedDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FloatingWindowService : LifecycleService() {

    companion object {
        private const val NOTIFICATION_ID = 1
    }

    @Inject
    lateinit var repository: InspirationRepository // 必须公开，否则注入会失败

    private val floatingWindowManager = FloatingWindowManager(this) // Service 也是 context 的一种

    override fun onCreate() {
        super.onCreate()
        RDALogger.info("onCreate 执行！")
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        RDALogger.info("onStartCommand 执行！")

        floatingWindowManager.apply {
            initFloatingButton() // 初始化悬浮窗逻辑

            // 这段观察代码必须放在 Service 中，否则在其他应用之上无法观察。
            isFabClose.observe(this@FloatingWindowService) { isClose ->
                val bgColorRes: Int
                val iconRes: Int

                if (isClose) {
                    bgColorRes = R.color.red
                    iconRes = R.drawable.close
                } else {
                    bgColorRes = R.color.deep_green
                    iconRes = R.drawable.write
                }

                fab?.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@FloatingWindowService, bgColorRes)
                ) // 设置 FAB 的背景色，必须用 backgroundTintList
                fab?.setImageResource(iconRes)
            }

            var id = 0L // 必须放在观察之外缓存，否则每次的值都是初始值
            inputConfirm.observe(this@FloatingWindowService) { inputConfirm ->
                val (type, taggedText, category) = inputConfirm

                lifecycleScope.launch {
                    when(type) {
                        DBOperationType.INVALID -> return@launch
                        DBOperationType.GET_LAST -> {
                            val (maxId, lastText) = repository.getLastIdText()
                            id = maxId
                            val displayText = if (lastText != null) {
                                val (source, tag) = lastText.split(separator)
                                if (tag.isEmpty()) { // 纯文本
                                    source
                                } else { // html 文本
                                    HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trimEnd()
                                }
                            } else ""
                            floatingWindowManager.handleSingleTap(displayText)
                        }
                        DBOperationType.INSERT -> {
                            val inspiration = Inspiration(
                                date = getAdjustedDate(),
                                text = taggedText,
                                category = category
                            )
                            repository.insert(inspiration)
                        }
                        DBOperationType.UPDATE -> repository.updateTextById(id, taggedText)
                    }
                }
            }

        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        RDALogger.info("onDestroy 执行！")
        floatingWindowManager.removeFloatingButton()
    }

    private fun createNotification(): Notification {
        val channelId = "floating_window_service_channel"
        val channel = NotificationChannel(
            channelId,
            "Floating Window Service",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Floating Window Service Running")
            .setSmallIcon(R.drawable.write)
            .build()
    }


}

