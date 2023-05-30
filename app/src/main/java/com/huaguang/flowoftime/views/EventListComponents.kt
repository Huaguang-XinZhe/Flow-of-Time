package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventWithSubEvents
import com.huaguang.flowoftime.names
import com.huaguang.flowoftime.ui.theme.DarkGray24
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import java.time.Duration

@Composable
fun EventList(
    viewModel: EventsViewModel,
    listState: LazyListState,
    eventsWithSubEvents: List<EventWithSubEvents>,
    isEventNameNotClicked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = listState
        ) {
            items(
                items = eventsWithSubEvents,
                key = { eventWithSubEvents ->
                    // Use the event's id as the key
                    eventWithSubEvents.event.id
                }
            ) { (event, subEvents) ->
                CustomSwipeToDismiss(
                    dismissed = { viewModel.deleteItem(event, subEvents) },
                    isEventNameNotClicked = isEventNameNotClicked
                ) {
                    EventItem(event, subEvents, viewModel)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomSwipeToDismiss(
    dismissed: () -> Unit,
    isEventNameNotClicked: Boolean,
    dismissContent: @Composable (RowScope.() -> Unit)
) {
    val dismissState = rememberDismissState()
    if (dismissState.isDismissed(DismissDirection.StartToEnd)) { dismissed() }

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier.padding(8.dp),
        directions = if (isEventNameNotClicked) setOf(DismissDirection.StartToEnd) else setOf(),
        dismissThresholds = {
            FractionalThreshold(0.35f)
        },
        background = {
            val isDefault = dismissState.targetValue == DismissValue.Default
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.LightGray
                    DismissValue.DismissedToEnd -> Color.Red
                    else -> Color.Green
                }
            )
            val scale by animateFloatAsState(
                // DismissValue.Default 是滑块达到阈值之前的状态
                if (isDefault) 0.75f else 1f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize() // 背景部分不撑到父容器那么大，就只会是刚刚好包含 Icon 的大小
                    .clip(RoundedCornerShape(12.dp)) //必须放在这里，如果放在 SwipeToDismiss，会把 Card 的阴影给覆盖了。
                    .background(color)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.scale(scale),
                    tint = if (isDefault) Color.Black else Color.White
                )
            }
        },
        dismissContent = dismissContent
    )
}

@Composable
fun EventItem(
    event: Event,
    subEvents: List<Event> = listOf(),
    viewModel: EventsViewModel
) {
    val cardColors = if (names.contains(event.name)) {
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
            EventItemRow(event = event, showTime = true, viewModel)

            // 插入的临时事件的 UI
            for (subEvent in subEvents) {
                EventItemRow(
                    event = subEvent,
                    showTime = false,
                    viewModel = viewModel,  // 添加了一些左侧的 padding 以便缩进
                    modifier = Modifier.padding(start = 30.dp)
                )
            }
        }
    }
}

@Composable
fun EventItemRow(
    event: Event,
    showTime: Boolean,
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var duration by remember { mutableStateOf(event.duration) }
    var isExpansion by remember { mutableStateOf(false) }
    val selectedEventIdsMap by viewModel.selectedEventIdsMap

    val endTimeText = if (endTime != null) {
        if (endTime == startTime) "" else {
            formatLocalDateTime(endTime!!)
        }
    } else "..."
    val durationText = if (duration != null) {
        if (duration == Duration.ZERO) "" else {
            formatDurationInText(duration!!)
        }
    } else "..."
    val isEventNameExceedsLimit = event.name.length > 10
    val painter = if (!isExpansion) {
        painterResource(id = R.drawable.expansion)
    } else {
        painterResource(id = R.drawable.contraction)
    }

    LaunchedEffect(event.endTime, event.duration) {
        endTime = event.endTime
        duration = event.duration
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showTime) {
            DraggableText(
                text = formatLocalDateTime(startTime),
                onDragDelta = { dragValue ->
                    startTime = startTime.plusMinutes(dragValue.toLong())

                    if (duration != null && event.name != "起床") {
                        val delta = Duration.between(startTime, event.startTime)
                        duration = event.duration!! + delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(startTime = startTime, duration = duration)
                    val lastDelta = duration?.minus(event.duration)
                    viewModel.updateTimeAndState(updatedEvent, lastDelta)
                },
                modifier = Modifier.padding(end = 5.dp),
                viewModel = viewModel
            )
        }

        Text(
            text = if (showTime) event.name else "……${event.name}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = if (!isExpansion) 1 else 3,
            overflow = if (!isExpansion) TextOverflow.Ellipsis else TextOverflow.Visible,
            modifier = Modifier
                .clickable {
                    viewModel.onNameTextClicked(event)
                }
                .let { modifier ->
                    if (selectedEventIdsMap[event.id] == true) { // 为了与其他 item 区分开来，这里只能用 selectedEventIdsMap
                        modifier
                            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
                            .padding(3.dp)
                    } else modifier
                }
                .then(if (isEventNameExceedsLimit) modifier.weight(1f) else modifier)
        )

        if (isEventNameExceedsLimit) {
            Icon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 5.dp)
                    .clickable {
                        isExpansion = !isExpansion
                    }
            )
        }

        if (showTime) {
            DraggableText(
                text = endTimeText,
                onDragDelta = { dragValue ->
                    if (endTime != null && event.name != "起床") {
                        endTime = endTime!!.plusMinutes(dragValue.toLong())
                        val delta = Duration.between(endTime, event.endTime)
                        duration = event.duration!! - delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(endTime = endTime, duration = duration)
                    val lastDelta = duration!! - event.duration
                    Log.i("打标签喽", "endTime: lastDelta = $lastDelta")
                    viewModel.updateTimeAndState(updatedEvent, lastDelta)
                },
                modifier = Modifier.padding(start = 5.dp),
                enabled = endTime != null,
                viewModel = viewModel,
                isShadow = endTimeText != "" && endTimeText != "..."
            )
        }

        Text(
            text = durationText,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun EventTime() {

}