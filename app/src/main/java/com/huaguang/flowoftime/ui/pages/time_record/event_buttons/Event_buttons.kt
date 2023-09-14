package com.huaguang.flowoftime.ui.pages.time_record.event_buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.ui.pages.time_record.LocalCheckedLiveData
import com.huaguang.flowoftime.ui.pages.time_record.LocalEventControl
import com.huaguang.flowoftime.ui.pages.time_record.LocalSelectedTime
import com.huaguang.flowoftime.ui.widget.LongPressButton
import com.huaguang.flowoftime.ui.widget.LongPressTextButton
import java.time.LocalDateTime

@Composable
fun EventButtons(
    viewModel: EventButtonsViewModel,
    modifier: Modifier = Modifier
) {

    val eventControl = LocalEventControl.current
    val selectedTime = LocalSelectedTime.current
    val checked = LocalCheckedLiveData.current

    ConstraintLayout (
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        val (undoButtonRef, buttonRowRef) = createRefs()

        UnDoButton(
            viewModel = viewModel,
            checked = checked,
            modifier = Modifier.constrainAs(undoButtonRef) {
                start.linkTo(parent.start, 16.dp)
                // 为了竖直居中
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            }
        )

        ButtonsRow(
            viewModel = viewModel,
            eventControl = eventControl,
            selectedTime = selectedTime,
            checked = checked,
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
    checked: MutableLiveData<Boolean>,
    modifier: Modifier = Modifier
) {
    if (!viewModel.undoStack.undoShow) return

    IconButton(
        onClick = { viewModel.onUndoButtonClick(checked) },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.undo_filled),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ButtonsRow(
    viewModel: EventButtonsViewModel,
    eventControl: EventControl,
    selectedTime: MutableState<LocalDateTime?>?,
    checked: MutableLiveData<Boolean>,
    modifier: Modifier = Modifier
) {
    if (viewModel.inputState.show.value) return // 输入框弹起，就不显示

    Row(
        modifier = modifier
    ) {
        viewModel.buttonsState.apply {
            if (mainShow.value) {
                LongPressButton(
                    text = mainText.value,
                    onClick = { viewModel.onMainButtonClick(eventControl, selectedTime, checked) },
                    onLongClick = { viewModel.onMainButtonLongClick(eventControl) },
                )
            }

            if (subShow.value) {
                LongPressTextButton(
//                    text = viewModel.getDisplayTextForSub(subText.value),
                    text = subText.value,
                    onClick = { viewModel.onSubButtonClick(eventControl, selectedTime, checked) },
                    onLongClick = { viewModel.onSubButtonLongClick(eventControl) },
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}


