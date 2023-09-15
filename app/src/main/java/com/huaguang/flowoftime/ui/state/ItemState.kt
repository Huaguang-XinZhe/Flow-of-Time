package com.huaguang.flowoftime.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.huaguang.flowoftime.BlockType
import com.huaguang.flowoftime.Mode

data class ItemState(
    val block: BlockType,
    val mode: MutableState<Mode>,
) {
    companion object {
        fun initialDisplay() = ItemState(
            block = BlockType.DISPLAY,
            mode = mutableStateOf(Mode.DISPLAY)
        )

        fun initialRecording() = ItemState(
            block = BlockType.RECORDING,
            mode = mutableStateOf(Mode.RECORD)
        )
    }
}
