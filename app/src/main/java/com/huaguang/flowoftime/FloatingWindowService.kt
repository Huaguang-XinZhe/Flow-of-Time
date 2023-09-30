package com.huaguang.flowoftime

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
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

    @Inject
    lateinit var repository: InspirationRepository // 必须公开，否则注入会失败

    private val floatingWindowManager = FloatingWindowManager(this) // Service 也是 context 的一种

    override fun onCreate() {
        super.onCreate()

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

            inputStr.observe(this@FloatingWindowService) { inputStr ->
                RDALogger.info("观察到变化，inputStr = $inputStr")
                if (inputStr.trim().isEmpty()) return@observe

                lifecycleScope.launch {
                    repository.insert(Inspiration(
                        date = getAdjustedDate(),
                        text = inputStr,
                    ))
                }
            }

        }
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//
//        return START_STICKY
//    }
}

