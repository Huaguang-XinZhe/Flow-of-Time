package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.InputIntent
import com.huaguang.flowoftime.ItemType

data class InputState(
    val eventId: MutableState<Long>,  // 影响的事件的 id
    val show: MutableState<Boolean>, // 输入框的弹出状态，是弹出还是隐藏？
    val newName: MutableState<String>,  // 事件的新名称
    val intent: MutableState<InputIntent>, // 输入框弹出的意图，是要修改还是要记录新事件？
    val type: MutableState<ItemType>, // 作用条目的类型，是展示项还是记录项？
) {
    companion object {
        fun initialValue() =
            InputState(
                eventId = mutableStateOf(0L),
                show = mutableStateOf(false),
                newName = mutableStateOf(""),
                intent = mutableStateOf(InputIntent.RECORD),
                type = mutableStateOf(ItemType.RECORD)
            )
    }
}
