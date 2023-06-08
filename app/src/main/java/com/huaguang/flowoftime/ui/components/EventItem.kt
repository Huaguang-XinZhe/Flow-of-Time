package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.components.event_name.EventName
import com.huaguang.flowoftime.ui.components.event_time.DraggableEventTime
import com.huaguang.flowoftime.ui.theme.DarkGray24
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.isCoreEvent

@Composable
fun EventItem(
    event: Event,
    subEvents: List<Event> = listOf(),
    mediator: EventTrackerMediator
) {
    val cardColors = if (isCoreEvent(event.name)) {
        CardDefaults.cardColors(
            containerColor = DarkGray24,
            contentColor = Color.White
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            EventItemRow(mediator, event = event, showTime = true)

            // 插入的临时事件的 UI
            for (subEvent in subEvents) {
                EventItemRow(
                    mediator = mediator,
                    event = subEvent,
                    showTime = false,  // 添加了一些左侧的 padding 以便缩进
                    modifier = Modifier.padding(start = 30.dp)
                )
            }
        }
    }
}

@Composable
fun EventItemRow(
    mediator: EventTrackerMediator,
    event: Event,
    showTime: Boolean,
    modifier: Modifier = Modifier,
) {
    val startTimeState = remember { mutableStateOf(event.startTime) }
    val endTimeState = remember(event.endTime) { mutableStateOf(event.endTime) }
    val durationState = remember(event.duration) { mutableStateOf(event.duration) }
    val isShowTail = remember { mutableStateOf(true) }

    // 这么写的目的就是为了让当前 event（Item）的变化能够通知到上面的状态，而没有变化的其他 event（Item）就维持 remember 里面的状态值。
    // 这就对当前 Item 和其他 Item 做出了区分，同时还能做到在 Composable 外边改变它的状态值。
    LaunchedEffect(event.name) {
        isShowTail.value = event.name != "起床"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showTime) {
            DraggableEventTime(
                event = event,
                mediator = mediator,
                startTimeState = startTimeState,
                endTimeState = endTimeState,
                durationState = durationState
            )
        }

        EventName(
            event = event,
            viewModel = mediator.eventNameViewModel,
            showTime = showTime,
            modifier = Modifier.weight(1f)
        )

        if (isShowTail.value) {
            if (showTime) {
                DraggableEventTime(
                    isEndTime = true,
                    event = event,
                    mediator = mediator,
                    startTimeState = startTimeState,
                    endTimeState = endTimeState,
                    durationState = durationState
                )
            }

            Text(
                text = durationState.value?.let { formatDurationInText(it) } ?: "...",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
