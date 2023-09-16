package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.DashType

data class LabelState(
    val eventId: MutableState<Long>,
    val show: MutableState<Boolean>,
    val name: MutableState<String>,
    var names: List<String>?,  // 非状态（没有影响）
    val type: MutableState<DashType>,
) {
    companion object {
        fun initialValue() = LabelState(
            eventId = mutableStateOf(0L),
            show = mutableStateOf(false),
            name = mutableStateOf(""),
            names = null,
            type = mutableStateOf(DashType.TAG)
        )
    }
}
