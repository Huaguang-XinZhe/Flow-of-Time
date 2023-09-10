package com.huaguang.flowoftime.state

import androidx.compose.runtime.MutableState
import com.huaguang.flowoftime.data.sources.SPHelper

data class ButtonsState(
    val mainText: MutableState<String>,
    val mainShow: MutableState<Boolean>,
    val subText: MutableState<String>,
    val subShow: MutableState<Boolean>,
    val undoShow: MutableState<Boolean>,
) {
    companion object {
        fun initialValue(spHelper: SPHelper) = spHelper.getButtonsState()
    }
}
