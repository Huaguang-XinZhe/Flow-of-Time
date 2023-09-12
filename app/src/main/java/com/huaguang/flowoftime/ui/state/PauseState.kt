package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.data.sources.SPHelper
import java.time.LocalDateTime

data class PauseState(
    val start: MutableState<LocalDateTime?>, // 按下暂停时的开始时间（可以处理跨日的情形)，初始值为 null（没有暂停）
    val acc: MutableState<Int>, // 当前最近事项暂停的累积值，初始值为 0
) {
    companion object {
        fun initialValue(spHelper: SPHelper) = spHelper.getPauseState()
    }
}
