package com.huaguang.flowoftime.custom_interface

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.ItemType

interface ButtonsStateControl {
    fun subjectTiming()

    fun followTiming()

    fun stepTiming()

    fun resetItemState(
        displayItemState: MutableState<ItemType>,
        recordingItemState: MutableState<ItemType>,
    )
}