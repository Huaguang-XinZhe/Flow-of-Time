package com.huaguang.flowoftime.custom_interface

import com.huaguang.flowoftime.ui.state.ItemState

interface ButtonsStateControl {
    fun subjectTiming()

    fun followTiming()

    fun stepTiming()

    fun resetItemState(
        displayItemState: ItemState,
        recordingItemState: ItemState,
    )
}