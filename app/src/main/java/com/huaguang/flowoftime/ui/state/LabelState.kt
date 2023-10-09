package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class LabelState(
    var eventId: Long,
    val dialogShow: MutableState<Boolean>,
    val category: MutableState<String?>,
    var tags: List<String>?,  // 非状态（没有影响）
) {
    companion object {
        fun initialValue() = LabelState(
            eventId = 0L,
            dialogShow = mutableStateOf(false),
            category = mutableStateOf(null),
            tags = null,
        )
    }
}
