package com.huaguang.flowoftime.data.models

import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.ui.state.ItemState
import java.time.LocalDateTime

data class ButtonActionParams(
    val eventControl: EventControl,
    val selectedTime: MutableState<LocalDateTime?>?,
    val checked: MutableLiveData<Boolean>,
    val displayItemState: ItemState,
    val recordingItemState: ItemState
)
