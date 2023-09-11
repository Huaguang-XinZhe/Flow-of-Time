package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtons
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulator
import java.time.LocalDateTime

val LocalEventControl = compositionLocalOf<EventControl> { error("没有提供实现 EventControl 接口的对象！") }
val LocalButtonsStateControl = compositionLocalOf<ButtonsStateControl> {
    error("没有提供实现 ButtonsStateControl 接口的对象！")
}
val LocalSelectedTime = compositionLocalOf<MutableState<LocalDateTime?>?> { null }

@Composable
fun TimeRecordPage(
    pageViewModel: TimeRecordPageViewModel,
) {
    val customTimeState = remember { mutableStateOf<CustomTime?>(null) }
    val selectedTime = remember { mutableStateOf<LocalDateTime?>(null) }

    ConstraintLayout(
        modifier = Modifier.padding(vertical = 10.dp)
    ) {

        val (topBar, itemColumn, timeRegulator,
            eventButtons, eventInput, floatingButton) = createRefs()

        RecordPageTopBar(modifier = Modifier.constrainAs(topBar) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }) // 完全独立，不需要和其他组件交互

        CompositionLocalProvider(
            LocalSelectedTime provides selectedTime,
            LocalEventControl provides pageViewModel.eventControl,
            LocalButtonsStateControl provides pageViewModel.eventButtonsViewModel.buttonsStateControl
        ) {
            DisplayAndRecordingItemColumn(
                viewModel = pageViewModel.eventInputViewModel,
                customTimeState = customTimeState,
                modifier = Modifier.constrainAs(itemColumn) {
                    top.linkTo(topBar.bottom, 5.dp)
                    start.linkTo(parent.start)
                }
            )

            EventButtons(
                viewModel = pageViewModel.eventButtonsViewModel,
                modifier = Modifier.constrainAs(eventButtons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
            )

            TimeRegulator(
                customTimeState = customTimeState,
                viewModel = pageViewModel.timeRegulatorViewModel,
                modifier = Modifier.constrainAs(timeRegulator) {
                    bottom.linkTo(eventButtons.top, 10.dp)
                    start.linkTo(parent.start)
                }
            )

            EventInputField(
                viewModel = pageViewModel.eventInputViewModel,
                modifier = Modifier.constrainAs(eventInput) {
                    bottom.linkTo(timeRegulator.top, 100.dp)
                    start.linkTo(parent.start)
                }
            )

            CoreFloatingButton(
                viewModel = pageViewModel.eventInputViewModel,
                modifier = Modifier.constrainAs(floatingButton) {
                    bottom.linkTo(timeRegulator.top, 20.dp)
                    end.linkTo(parent.end, 16.dp)
                }
            )

            InputAlertDialog(viewModel = pageViewModel.eventInputViewModel)
        }
    }
}
