package com.huaguang.flowoftime.ui.components.event_buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.EventsViewModel
import com.huaguang.flowoftime.widget.LongPressButton
import com.huaguang.flowoftime.widget.LongPressTextButton

@Composable
fun EventButtons(viewModel: EventsViewModel) {
    val mainEventButtonText by viewModel.mainEventButtonText
    val subEventButtonText by viewModel.subEventButtonText
    val mainButtonShow by viewModel.mainButtonShow.observeAsState()
    val subButtonShow by viewModel.subButtonShow.observeAsState()
    val initialized by viewModel.initialized

    if (initialized) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            if (mainButtonShow == true) {
                LongPressButton(
                    onClick = { viewModel.toggleMainEvent() },
                    onLongClick = { viewModel.onMainButtonLongClick() },
                    text = mainEventButtonText
                )
            }

            if (subButtonShow == true) {
                LongPressTextButton(
                    onClick = { viewModel.toggleSubEvent() },
                    onLongClick = { viewModel.onSubButtonLongClick() },
                    text = subEventButtonText,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}