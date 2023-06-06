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
import androidx.compose.material.DismissState
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
import com.huaguang.flowoftime.ui.theme.DarkGray24
import com.huaguang.flowoftime.ui.theme.LightRed6
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun EventList(
    viewModel: EventsViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Log.i("打标签喽", "EventList 重组！！！")
//    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val eventsWithSubEvents by viewModel.eventsWithSubEvents.collectAsState(emptyList())
    
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
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
                    event = event,
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
    val currentEvent by viewModel.currentEvent
    val initialized by viewModel.initialized

    if (initialized && currentEvent != null) {
        currentEvent!!.let {
            if (it.name != "￥为减少重组，优化频闪，不显示的特别设定￥" && it.endTime != LocalDateTime.MIN) {
                CustomSwipeToDismiss(
                    viewModel = viewModel,
                    dismissed = { viewModel.deleteItem(it) }
                ) {
                    EventItem(
                        event = it,
                        viewModel = viewModel
                    )
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomSwipeToDismiss(
    event: Event? = null,
    viewModel: EventsViewModel,
    dismissed: () -> Unit,
    dismissContent: @Composable (RowScope.() -> Unit)
) {
    Log.i("打标签喽", "CustomSwipeToDismiss 重组了！！！")

    val context = LocalContext.current
    val dismissState = rememberDismissState()
    val isItemClicked = remember { mutableStateOf(false) }
    val isInputShow by viewModel.isInputShow

    val direction = if (isItemClicked.value) {
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
            .clickable( // 弹出输入框时禁止点击解除限制，滑动删除
                // 已经插入数据库，且子事项正在计时的主事项禁止点击
                enabled = !isInputShow && event?.let { it.endTime != null } ?: true
            ) {
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
            SwipeBackground(dismissState = dismissState)
        },
        dismissContent = dismissContent
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeBackground(dismissState: DismissState) {
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
}

@Composable
fun EventItem(
    event: Event,
    subEvents: List<Event> = listOf(),
    viewModel: EventsViewModel
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
            EventTime(
                event = event,
                viewModel = viewModel,
                startTimeState = startTimeState,
                endTimeState = endTimeState,
                durationState = durationState
            )
        }

        EventName(
            event = event,
            viewModel = viewModel,
            showTime = showTime,
            modifier = Modifier.weight(1f)
        )

        if (isShowTail.value) {
            if (showTime) {
                EventTime(
                    isEndTime = true,
                    event = event,
                    viewModel = viewModel,
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

@Composable
fun EventName(
    event: Event,
    viewModel: EventsViewModel,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpansion by remember { mutableStateOf(false) }
    var isShowIcon by remember { mutableStateOf(false) }
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
        onTextLayout = { textLayoutResult ->  // 文本布局完成时的回调函数
            if (textLayoutResult.hasVisualOverflow) {  // 如果文本溢出
                isShowIcon = true
            }
        },
        modifier = if (event.name.length > 10 && event.parentId == null) modifier else Modifier
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
    )

    if (isShowIcon) {
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
fun EventTime(
    isEndTime: Boolean = false,
    event: Event,
    viewModel: EventsViewModel,
    startTimeState: MutableState<LocalDateTime>,
    endTimeState: MutableState<LocalDateTime?>,
    durationState: MutableState<Duration?>
) {
    val text = if (isEndTime) {
        endTimeState.value?.let { formatLocalDateTime(it) } ?: "..."
    } else {
        formatLocalDateTime(startTimeState.value)
    }

    DraggableText(
        modifier = Modifier.padding(end = if (isEndTime) 0.dp else 5.dp),
        text = text,
        isEndTime = isEndTime,
        viewModel = viewModel,
        event = event,
        onDragDelta = { dragValue ->
            if (isEndTime) {
                endTimeState.value = endTimeState.value?.plusMinutes(dragValue.toLong())
                durationState.value = durationState.value?.plusMinutes(dragValue.toLong())
            } else {
                startTimeState.value = startTimeState.value.plusMinutes(dragValue.toLong())
                durationState.value = durationState.value?.minusMinutes(dragValue.toLong())
            }
        }
    ) {
        val updatedEvent = event.copy(
            startTime = startTimeState.value,
            endTime = endTimeState.value,
            duration = durationState.value
        )

        viewModel.updateTimeAndState(updatedEvent, event.duration)
    }
}