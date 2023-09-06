package com.huaguang.flowoftime.pages.time_record

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtons
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventControl
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulator
import com.huaguang.flowoftime.ui.components.DisplayEventItem
import java.time.LocalDateTime

val LocalEventControl = compositionLocalOf<EventControl> { error("没有提供实现 EventControl 接口的对象！") }
val LocalSelectedTime = compositionLocalOf<MutableState<LocalDateTime?>?> { null }

@Composable
fun TimeRecordPage(
    pageViewModel: TimeRecordPageViewModel,
) {
    // 已经过滤空值，event 接受到 Flow 的值后将始终为非空。尽管如此，初始值还是空的，这意味着最初的显示 event 为 null。
    val event by pageViewModel.currentEventFlow.collectAsState(initial = null)
//    RDALogger.info("TimeRecordPage: event = $event")
    val customTimeState = remember { mutableStateOf<CustomTime?>(null) }
    val selectedTime = remember { mutableStateOf<LocalDateTime?>(null) }
    val lastEventState = remember {  mutableStateOf<Event?>(null) }

    LaunchedEffect(event?.id) {
        lastEventState.value = pageViewModel.eventRepository.getLastEvent(event?.id)
    }

    ConstraintLayout(
        modifier = Modifier.padding(vertical = 10.dp)
    ) {

        val (topBar, displayItem, recordingSection, timeRegulator, eventButtons) = createRefs()

        RecordPageTopBar(modifier = Modifier.constrainAs(topBar) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
        }) // 完全独立，不需要和其他组件交互

        // 和数据源交互即可
        DisplayEventItem(
            event = lastEventState.value,
            iconRepository = pageViewModel.iconRepository,
            eventRepository = pageViewModel.eventRepository,
            modifier = Modifier.constrainAs(displayItem) {
                top.linkTo(topBar.bottom)
                start.linkTo(parent.start)
            }
        )

        if (event != null) {
            CompositionLocalProvider(
                LocalSelectedTime provides selectedTime
            ) {
                RecordingEventItem(
                    event = event!!,
                    customTimeState = customTimeState,
                    repository = pageViewModel.eventRepository,
                    modifier = Modifier.constrainAs(recordingSection) {
                        val reference = if (lastEventState.value?.duration == null) topBar else displayItem

                        top.linkTo(reference.bottom, 10.dp)
                        start.linkTo(parent.start)
                    }
                )
            }
        }

        // 需要和 RecordingEventItem 交互
        CompositionLocalProvider(
            LocalEventControl provides pageViewModel.eventControl,
            LocalSelectedTime provides selectedTime
        ) {
            EventButtons(
                viewModel = pageViewModel.eventButtonsViewModel,
                modifier = Modifier.constrainAs(eventButtons) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
            )
        }

        // 需要和 RecordingEventItem 交互
        CompositionLocalProvider(
            LocalSelectedTime provides selectedTime
        ) {
            TimeRegulator(
                CustomTimeState = customTimeState,
                viewModel = pageViewModel.timeRegulatorViewModel,
                modifier = Modifier.constrainAs(timeRegulator) {
                    bottom.linkTo(eventButtons.top, 10.dp)
                    start.linkTo(parent.start)
                }
            )
        }

    }

}

