package com.huaguang.flowoftime.ui.components.event_buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.components.EventTrackerMediator
import com.huaguang.flowoftime.widget.LongPressButton
import com.huaguang.flowoftime.widget.LongPressTextButton

@Composable
fun EventButtons(mediator: EventTrackerMediator) {
  val initialized by mediator.initialized

    if (initialized) {
        ConstraintLayout (
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
        ) {
            val (undoButtonRef, buttonRowRef) = createRefs()

            UnDoButton(
                mediator = mediator,
                modifier = Modifier.constrainAs(undoButtonRef) {
                    start.linkTo(parent.start, 16.dp)
                    // 为了竖直居中
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
            )

            StartOrInsertButtonRow(
                mediator = mediator,
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
}

@Composable
fun UnDoButton(
    mediator: EventTrackerMediator,
    modifier: Modifier = Modifier
) {
    val undoFilledIconShow by mediator.eventButtonsViewModel.undoFilledIconShow

    if (undoFilledIconShow) {
        IconButton(
            onClick = { mediator.onUndoFilledClicked() },
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
    mediator: EventTrackerMediator,
    modifier: Modifier = Modifier
) {
    val mainEventButtonText by mediator.eventButtonsViewModel.mainButtonText
    val subEventButtonText by mediator.eventButtonsViewModel.subButtonText
    val mainButtonShow by mediator.eventButtonsViewModel.mainButtonShow.observeAsState()
    val subButtonShow by mediator.eventButtonsViewModel.subButtonShow.observeAsState()

    Row(
        modifier = modifier
    ) {
        if (mainButtonShow == true) {
            LongPressButton(
                onClick = { mediator.onMainButtonClicked() },
                onLongClick = { mediator.onMainButtonLongClicked() },
                text = mainEventButtonText
            )
        }

        if (subButtonShow == true) {
            LongPressTextButton(
                text = subEventButtonText,
                onClick = { mediator.onSubButtonClicked() },
                onLongClick = { mediator.onSubButtonLongClicked() },
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}


