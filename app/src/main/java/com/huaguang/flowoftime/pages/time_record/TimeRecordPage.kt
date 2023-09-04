package com.huaguang.flowoftime.pages.time_record

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtons
import com.huaguang.flowoftime.pages.time_record.recording_event_item.RecordingEventItem
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulator
import com.huaguang.flowoftime.ui.components.DisplayEventItem
import com.huaguang.flowoftime.ui.components.EventDisplay
import java.time.LocalDateTime

@Composable
fun TimeRecordPage(
    pageViewModel: TimeRecordPageViewModel,
) {
    val eventDisplay = pageViewModel.getEventDisplay()
    val selectedTime = remember { mutableStateOf<LocalDateTime>(LocalDateTime.now()) }

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
            eventDisplay = pageViewModel.getLastEventDisplay(),
            iconRepository = pageViewModel.iconRepository,
            modifier = Modifier.constrainAs(displayItem) {
                top.linkTo(topBar.bottom)
                start.linkTo(parent.start)
            }
        )

        EventRecordingSection(
            pageViewModel = pageViewModel,
            eventDisplay = eventDisplay,
            selectedTime = selectedTime,
            modifier = Modifier.constrainAs(recordingSection) {
                top.linkTo(displayItem.bottom, 10.dp)
                start.linkTo(parent.start)
            }
        )

        // 需要和 RecordingEventItem 交互
        CompositionLocalProvider(
            LocalEventControl provides pageViewModel.eventControl
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
        TimeRegulator(
            time = selectedTime,
            viewModel = pageViewModel.timeRegulatorViewModel,
            modifier = Modifier.constrainAs(timeRegulator) {
                bottom.linkTo(eventButtons.top, 10.dp)
                start.linkTo(parent.start)
            }
        )

    }

}

@Composable
fun EventRecordingSection(
    pageViewModel: TimeRecordPageViewModel,
    eventDisplay: EventDisplay,
    selectedTime: MutableState<LocalDateTime>,
    modifier: Modifier = Modifier
) {
    if (pageViewModel.eventStop) {
        DisplayEventItem(
            eventDisplay = eventDisplay,
            iconRepository = pageViewModel.iconRepository,
            modifier = modifier
        )
    } else {
        CompositionLocalProvider(
            LocalSelectedTime provides selectedTime
        ) {
            RecordingEventItem(
                eventDisplay = eventDisplay,
                viewModel = pageViewModel.recordingEventItemViewModel,
                modifier = modifier
            )
        }
    }
}
