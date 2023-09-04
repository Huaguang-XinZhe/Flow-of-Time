package com.huaguang.flowoftime.pages.time_record.time_regulator

import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.data.sources.SPHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TimeRegulatorViewModel @Inject constructor(
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