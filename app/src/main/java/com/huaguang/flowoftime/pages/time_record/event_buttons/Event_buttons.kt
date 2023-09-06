package com.huaguang.flowoftime.pages.time_record.event_buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.pages.time_record.LocalEventControl
import com.huaguang.flowoftime.pages.time_record.LocalSelectedTime
import com.huaguang.flowoftime.widget.LongPressButton
import com.huaguang.flowoftime.widget.LongPressTextButton
import java.time.LocalDateTime

@Composable
fun EventButtons(
    viewModel: EventButtonsViewModel,
    modifier: Modifier = Modifier
) {

    val eventControl = LocalEventControl.current
    val selectedTime = LocalSelectedTime.current

    ConstraintLayout (
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        val (undoButtonRef, buttonRowRef) = createRefs()

        UnDoButton(
            viewModel = viewModel,
            modifier = Modifier.constrainAs(undoButtonRef) {
                start.linkTo(parent.start, 16.dp)
                // 为了竖直居中
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        StartOrInsertButtonRow(
            viewModel = viewModel,
            eventControl = eventControl,
            selectedTime = selectedTime,
            modifier = Modifier.constrainAs(buttonRowRef) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                // 为了竖直居中
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )
    }

}

@Composable
fun UnDoButton(
    viewModel: EventButtonsViewModel,
    modifier: Modifier = Modifier
) {
    val undoFilledIconShow by viewModel.undoFilledIconShow

    if (undoFilledIconShow) {
        IconButton(
            onClick = { viewModel.onUndoFilledClick() },
            modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = R.drawable.undo_filled),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StartOrInsertButtonRow(
    viewModel: EventButtonsViewModel,
    eventControl: EventControl,
    selectedTime: MutableState<LocalDateTime?>?,
    modifier: Modifier = Modifier
) {
    val mainEventButtonText by viewModel.mainButtonText
    val subEventButtonText by viewModel.subButtonText
    val mainButtonShow by viewModel.mainButtonShow.observeAsState()
    val subButtonShow by viewModel.subButtonShow.observeAsState()

    Row(
        modifier = modifier
    ) {
        if (mainButtonShow == true) {
            LongPressButton(
                onClick = { viewModel.onMainButtonClick(eventControl, selectedTime) },
                onLongClick = { viewModel.onMainButtonLongClick(eventControl) },
                text = mainEventButtonText
            )
        }

        if (subButtonShow == true) {
            LongPressTextButton(
                text = subEventButtonText,
                onClick = { viewModel.onSubButtonClick(eventControl, selectedTime) },
                onLongClick = { viewModel.onSubButtonLongClick(eventControl) },
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}


