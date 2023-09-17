package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.InputIntent

data class InputState(
    val eventId: MutableState<Long>,  // 影响的事件的 id
    val eventType: MutableState<EventType>, // 影响的事件的类型
    val show: MutableState<Boolean>, // 输入框的弹出状态，是弹出还是隐藏？
    val newName: MutableState<String>,  // 事件的新名称
    val intent: MutableState<InputIntent>, // 输入框弹出的意图，是要修改还是要记录新事件？
) {
    companion object {
        fun initialValue() =
            InputState(
                eventId = mutableStateOf(0L),
                eventType = mutableStateOf(EventType.SUBJECT),
                show = mutableStateOf(false),
                newName = mutableStateOf(""),
                intent = mutableStateOf(InputIntent.RECORD),
            )
    }
}
