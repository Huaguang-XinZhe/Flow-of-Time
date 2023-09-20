package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.MutableLiveData
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.custom_interface.ButtonsStateControl
import com.huaguang.flowoftime.custom_interface.EventControl
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtons
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulator
import com.huaguang.flowoftime.ui.state.ItemState
import java.time.LocalDateTime

val LocalEventControl = compositionLocalOf<EventControl> { error("没有提供实现 EventControl 接口的对象！") }
val LocalButtonsStateControl = compositionLocalOf<ButtonsStateControl> {
    error("没有提供实现 ButtonsStateControl 接口的对象！")
}
val LocalSelectedTime = compositionLocalOf<MutableState<LocalDateTime?>?> { null }
val LocalCheckedLiveData = compositionLocalOf { MutableLiveData(true) }
val LocalDisplayItemState = compositionLocalOf { ItemState.initialDisplay() }
val LocalRecordingItemState = compositionLocalOf { ItemState.initialRecording() }
val LocalCustomTimeState = compositionLocalOf { mutableStateOf<CustomTime?>(null) }


@Composable
fun TimeRecordPage(
    viewModel: TimeRecordPageViewModel,
    onNavigation: (String) -> Unit
) {
    val customTimeState = remember { mutableStateOf<CustomTime?>(null) }
    val selectedTime = remember { mutableStateOf<LocalDateTime?>(null) }
    val displayItemState = remember { ItemState.initialDisplay() } // 内有默认值，决定它们第一次展示什么
    val recordingItemState = remember { ItemState.initialRecording() }

    RDALogger.info("Record 页重新组合")

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize() // 在放入脚手架的 content 中时，如果没有指定，那页面的高度就会默认包裹内容，所以必须指定！平常也最好指定。
            .padding(vertical = 5.dp)
    ) {

        val (topBar, itemColumn, timeRegulator,
            eventButtons, eventInput, floatingButton) = createRefs()

        RecordPageTopBar(
            modifier = Modifier.constrainAs(topBar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            },
            onNavigation = onNavigation
        ) // 完全独立，不需要和其他组件交互

        CompositionLocalProvider(
            LocalSelectedTime provides selectedTime,
            LocalEventControl provides viewModel.eventControl,
            LocalButtonsStateControl provides viewModel.buttonsViewModel.buttonsStateControl,
            LocalCheckedLiveData provides viewModel.regulatorViewModel.checkedLiveData,
            LocalCustomTimeState provides customTimeState,
            LocalDisplayItemState provides displayItemState,
            LocalRecordingItemState provides recordingItemState,
        ) {
            DisplayAndRecordingItemColumn(
                modifier = Modifier.constrainAs(itemColumn) {
                    top.linkTo(topBar.bottom, 5.dp)
                    start.linkTo(parent.start)
                }
            )

            EventButtons(
                modifier = Modifier.constrainAs(eventButtons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                },
            )

            TimeRegulator(
                modifier = Modifier.constrainAs(timeRegulator) {
                    bottom.linkTo(eventButtons.top, 10.dp)
                    start.linkTo(parent.start)
                },
            )

            CoreFloatingButton(
                modifier = Modifier.constrainAs(floatingButton) {
                    bottom.linkTo(timeRegulator.top, 20.dp)
                    end.linkTo(parent.end, 16.dp)
                }
            )

            EventInputField(
                modifier = Modifier.constrainAs(eventInput) {
                    bottom.linkTo(parent.bottom, 200.dp) // 必须指定一个值，这样软键盘在弹出时就不会把整个窗口往上推的过高！
                    start.linkTo(parent.start)
                },
            )

            CoreNameInputAlertDialog()

            ClassNameInputAlertDialog()
        }
    }
}
