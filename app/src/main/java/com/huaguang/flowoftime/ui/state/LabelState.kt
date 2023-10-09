package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf

data class LabelState(
    val eventId: MutableState<Long>,
    val show: MutableState<Boolean>,
    val name: MutableState<String>,
    var names: List<String>?,  // 非状态（没有影响）

) {
    companion object {
        fun initialValue() = LabelState(
            eventId = mutableLongStateOf(0L),
            show = mutableStateOf(false),
            name = mutableStateOf(""),
            names = null,
        )
    }
}
