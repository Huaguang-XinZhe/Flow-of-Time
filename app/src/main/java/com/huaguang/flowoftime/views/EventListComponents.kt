package com.huaguang.flowoftime.views

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.names
import com.huaguang.flowoftime.ui.theme.DarkGray24
import com.huaguang.flowoftime.ui.theme.LightRed6
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun EventList(
    viewModel: EventsViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
//    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val eventsWithSubEvents by viewModel.eventsWithSubEvents.collectAsState(emptyList())
    
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .align(Alignment.BottomCenter),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = eventsWithSubEvents,
                key = { eventWithSubEvents ->
                    // Use the event's id as the key
                    eventWithSubEvents.event.id
                }
            ) { (event, subEvents) ->
                CustomSwipeToDismiss(
                    viewModel = viewModel,
                    dismissed = { viewModel.deleteItem(event, subEvents) }
                ) {
                    EventItem(event = event, subEvents = subEvents, viewModel = viewModel)
                }
            }

            item {
                CurrentItem(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CurrentItem(viewModel: EventsViewModel) {
    Log.i("打标签喽", "CurrentItem 重组！")
    val currentEvent by viewModel.currentEventState
    val initialized by viewModel.initialized

    if (initialized) {
        currentEvent?.let {
            if (it.name != "&currentEvent不显示&" && it.endTime != LocalDateTime.MIN) {
                EventItem(
                    modifier = Modifier
                        .padding(8.dp, 8.dp, 8.dp, 16.dp),
                    event = it,
                    viewModel = viewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomSwipeToDismiss(
    viewModel: EventsViewModel,
    dismissed: () -> Unit,
    dismissContent: @Composable (RowScope.() -> Unit)
) {
    val context = LocalContext.current
    val dismissState = rememberDismissState()
    val isItemClicked = remember { mutableStateOf(false) }
    val isEventNameNotClicked by viewModel.isEventNameNotClicked

    val direction = if (isEventNameNotClicked && isItemClicked.value) {
        setOf(DismissDirection.StartToEnd)
    } else setOf()
    val borderModifier = if (isItemClicked.value) {
        Modifier.border(2.dp, Color.Red, RoundedCornerShape(12.dp))
    } else Modifier

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) { dismissed() }

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                isItemClicked.value = !isItemClicked.value
                if (isItemClicked.value) {
                    Toast
                        .makeText(context, "解除限制，可右滑删除", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .then(borderModifier),
        directions = direction,
        dismissThresholds = {
            FractionalThreshold(0.35f)
        },
        background = {
            val isDefault = dismissState.targetValue == DismissValue.Default
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> LightRed6
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
    modifier: Modifier = Modifier,
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
        colors = cardColors,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            EventItemRow(viewModel, event = event, showTime = true)

            // 插入的临时事件的 UI
            for (subEvent in subEvents) {
                EventItemRow(
                    viewModel = viewModel,
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
    viewModel: EventsViewModel,
    event: Event,
    showTime: Boolean,
    modifier: Modifier = Modifier,
) {
    val startTimeState = remember { mutableStateOf(event.startTime) }
    val endTimeState = remember { mutableStateOf(event.endTime) }
    val durationState = remember { mutableStateOf(event.duration) }
    val isShowTail = remember { mutableStateOf(true) }

    // 这么写的目的就是为了让当前 event（Item）的变化能够通知到上面的状态，而没有变化的其他 event（Item）就维持 remember 里面的状态值。
    // 这就对当前 Item 和其他 Item 做出了区分，同时还能做到在 Composable 外边改变它的状态值。
    LaunchedEffect(event.endTime, event.duration, event.name) {
        endTimeState.value = event.endTime
        durationState.value = event.duration
        isShowTail.value = event.name != "起床"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showTime) {
            EventStartTime(
                event = event,
                viewModel = viewModel,
                startTimeState = startTimeState,
                durationState = durationState
            )
        }

        EventName(
            event = event,
            viewModel = viewModel,
            showTime = showTime,
            modifier = if (event.name.length >= 9) Modifier.weight(1f) else Modifier
        )

        if (isShowTail.value) {
            if (showTime) {
                EventEndTime(
                    event = event,
                    viewModel = viewModel,
                    endTimeState = endTimeState,
                    endTimeText = endTimeState.value?.let { formatLocalDateTime(it) } ?: "...",
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

@Composable
fun EventName(
    event: Event,
    viewModel: EventsViewModel,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpansion by remember { mutableStateOf(false) }
    val selectedEventIdsMap by viewModel.selectedEventIdsMap

    val painter = if (!isExpansion) {
        painterResource(id = R.drawable.expansion)
    } else {
        painterResource(id = R.drawable.contraction)
    }

    Text(
        text = if (showTime) event.name else "……${event.name}",
        style = MaterialTheme.typography.titleMedium,
        maxLines = if (!isExpansion) 1 else 3,
        overflow = if (!isExpansion) TextOverflow.Ellipsis else TextOverflow.Visible,
        modifier = Modifier
            .padding(end = 5.dp)
            .clickable {
                viewModel.onNameTextClicked(event)
            }
            .then(
                if (selectedEventIdsMap[event.id] == true) { // 为了与其他 item 区分开来，这里只能用 selectedEventIdsMap
                    Modifier
                        .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                } else Modifier
            )
            .then(modifier)
    )

    if (event.name.length >= 9) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 5.dp)
                .clickable {
                    isExpansion = !isExpansion
                }
        )
    }
}

@Composable
fun EventStartTime(
    event: Event,
    viewModel: EventsViewModel,
    startTimeState: MutableState<LocalDateTime>,
    durationState: MutableState<Duration?>
) {
    DraggableText(
        modifier = Modifier.padding(end = 5.dp),
        text = formatLocalDateTime(startTimeState.value),
        viewModel = viewModel,
        onDragDelta = { dragValue ->
            startTimeState.value = startTimeState.value.plusMinutes(dragValue.toLong())

            if (durationState.value != null && event.name != "起床") {
                val delta = Duration.between(startTimeState.value, event.startTime)
                durationState.value = event.duration!! + delta
            }
        }
    ) {
        val updatedEvent =
            event.copy(startTime = startTimeState.value, duration = durationState.value)
        val lastDelta = durationState.value?.minus(event.duration)
        viewModel.updateTimeAndState(updatedEvent, lastDelta)
    }
}

@Composable
fun EventEndTime(
    event: Event,
    viewModel: EventsViewModel,
    endTimeState: MutableState<LocalDateTime?>,
    endTimeText: String,
    durationState: MutableState<Duration?>
) {
    DraggableText(
        text = endTimeText,
        enabled = endTimeState.value != null,
        isShadow = endTimeText != "" && endTimeText != "...",
        viewModel = viewModel,
        onDragDelta = { dragValue ->
            if (endTimeState.value != null && event.name != "起床") {
                endTimeState.value = endTimeState.value!!.plusMinutes(dragValue.toLong())
                val delta = Duration.between(endTimeState.value, event.endTime)
                durationState.value = event.duration!! - delta
            }
        }
    ) {
        val updatedEvent =
            event.copy(endTime = endTimeState.value, duration = durationState.value)
        val lastDelta = durationState.value!! - event.duration
        viewModel.updateTimeAndState(updatedEvent, lastDelta)
    }
}