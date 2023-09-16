package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.data.sources.SPHelper
import java.time.LocalDateTime

data class PauseState(
    val start: MutableState<LocalDateTime?>, // 按下暂停时的开始时间（可以处理跨日的情形)，初始值为 null（没有暂停）
    val acc: MutableState<Int>, // 当前最近事项暂停的累积值，初始值为 0（类游标，会适时清零）
    val subjectAcc: MutableState<Int>, // 这个累积值是专门为带有下级的主题事件准备的，不到事件结束，不重置（重置时机不同）
    val stepAcc: MutableState<Int>, // 带有下级的步骤事件的累积值
    val currentAcc: MutableState<Int>
) {
    companion object {
        fun initialValue(spHelper: SPHelper) = spHelper.getPauseState()
    }
}
