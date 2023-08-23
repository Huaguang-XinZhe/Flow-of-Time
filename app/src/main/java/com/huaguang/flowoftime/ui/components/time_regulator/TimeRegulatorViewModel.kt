package com.huaguang.flowoftime.ui.components.time_regulator

import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.data.sources.SPHelper
import java.time.LocalDateTime
import java.time.LocalTime

class TimeRegulatorViewModel(
    val time: LocalDateTime,
    private val spHelper: SPHelper
) : ViewModel() {

    private lateinit var recordTime: LocalTime

    fun calPauseInterval(checked: Boolean) { // checked 为 true 是继续（播放），表明当前事项正在计时……
        if (!checked) { // 暂停
            recordTime = LocalTime.now()
        } else { // 继续
            val interval = LocalTime.now().minute - recordTime.minute
            spHelper.savePauseInterval(interval)
        }
    }


}