package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.data.sources.SPHelper

data class IdState(
    val current: MutableState<Long>,
    val subject: MutableState<Long>,
    val step: MutableState<Long>,
    // 不包含
    var startId: Long, // 这个 id 记录的是已经计算过了的事件的最大 id（即最后一个计算过的事件，之后此事件和此事件之前的事件将不再参与集中式的类属计算）
    // 包含
    var endId: Long, // 这个 id 就是类属时长计算末尾的事件 id，一般时当前正在进行事件的上一个事件或者已经结束的当前事件
) {
    companion object {
        fun initialValue(spHelper: SPHelper) = spHelper.getIdState()
    }
}
