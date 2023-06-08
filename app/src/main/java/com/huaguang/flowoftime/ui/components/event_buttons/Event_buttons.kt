package com.huaguang.flowoftime.ui.components.event_buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.ui.components.EventTrackerMediator
import com.huaguang.flowoftime.widget.LongPressButton
import com.huaguang.flowoftime.widget.LongPressTextButton

@Composable
fun EventButtons(mediator: EventTrackerMediator) {
    val mainEventButtonText by mediator.eventButtonsViewModel.mainEventButtonText
    val subEventButtonText by mediator.eventButtonsViewModel.subEventButtonText
    val mainButtonShow by mediator.eventButtonsViewModel.mainButtonShow.observeAsState()
    val subButtonShow by mediator.eventButtonsViewModel.subButtonShow.observeAsState()
    val initialized by mediator.initialized

    if (initialized) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            if (mainButtonShow == true) {
                LongPressButton(
                    onClick = { mediator.toggleMainEvent() },
                    onLongClick = { mediator.onMainButtonLongClick() },
                    text = mainEventButtonText
                )
            }

            if (subButtonShow == true) {
                LongPressTextButton(
                    onClick = { mediator.toggleSubEvent() },
                    onLongClick = { mediator.onSubButtonLongClick() },
                    text = subEventButtonText,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}