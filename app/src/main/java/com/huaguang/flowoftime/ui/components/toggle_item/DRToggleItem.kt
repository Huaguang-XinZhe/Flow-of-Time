package com.huaguang.flowoftime.ui.components.toggle_item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.state.ItemState

@Composable
fun DRToggleItem(
    modifier: Modifier = Modifier,
    itemState: ItemState,
    combinedEvent: CombinedEvent?,
) {
    if (itemState.mode.value == Mode.DISPLAY) {
        DisplayEventItem(
            modifier = modifier,
            combinedEvent = combinedEvent,
            itemState = itemState,
        )
    } else {
        RecordingEventItem(
            modifier = modifier,
            combinedEvent = combinedEvent,
            itemState = itemState,
        )
    }
}