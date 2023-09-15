package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.custom_interface.EventControl
import java.time.LocalDateTime

data class ButtonActionParams(
    val eventControl: EventControl,
    val selectedTime: MutableState<LocalDateTime?>?,
    val checked: MutableLiveData<Boolean>,
    val displayItemState: MutableState<ItemType>,
    val recordingItemState: MutableState<ItemType>
)
