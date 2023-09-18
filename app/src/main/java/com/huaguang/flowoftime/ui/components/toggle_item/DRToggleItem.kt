package com.huaguang.flowoftime.ui.components.toggle_item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.state.ItemState

@Composable
fun DRToggleItem(
    itemState: ItemState,
    combinedEvent: CombinedEvent?,
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier,
) {
    if (itemState.mode.value == Mode.DISPLAY) {
        DisplayEventItem(
            combinedEvent = combinedEvent,
            viewModel = viewModel,
            itemState = itemState,
            modifier = modifier,
        )
    } else {
        RecordingEventItem(
            combinedEvent = combinedEvent,
            viewModel = viewModel,
            itemState = itemState,
            modifier = modifier,
        )
    }
}