package com.huaguang.flowoftime.views

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.hourThreshold
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewModel.EventsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration

@Composable
fun EventTrackerScreen(viewModel: EventsViewModel) {
    val isTracking by viewModel.isTracking.observeAsState()
    val scrollIndex by viewModel.scrollIndex.observeAsState()
    val scope = rememberCoroutineScope()
    val firstLaunch = remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val eventsWithSubEvents by viewModel.eventsWithSubEvents.collectAsState(emptyList())

    LaunchedEffect(scrollIndex) {
        scrollIndex?.let { index ->
            scope.launch {
                // 如果是首次启动，添加延迟
                if (firstLaunch.value) {
                    Log.i("打标签喽", "定位延迟！！！")
                    delay(200)
                    firstLaunch.value = false
                }
                listState.animateScrollToItem(index)
                Log.i("打标签喽", "滚动到索引：$index")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OtherRow(viewModel)

        DurationSlider(viewModel)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.align(Alignment.BottomCenter),
                state = listState
            ) {
                items(eventsWithSubEvents) {(event, subEvents) ->
                    EventItem(event, subEvents, viewModel)
                }
            }
        }

        if (isTracking == true) {
            EventInputField(viewModel)
        }

        EventButtons(viewModel)
    }
}

@Composable
fun OtherRow(viewModel: EventsViewModel) {
    val isAlarmSet by viewModel.isAlarmSet.observeAsState()
    val isImportExportEnabled by viewModel.isImportExportEnabled.observeAsState()
    var showDialog by remember { mutableStateOf(false) }

    Row {
        if (isAlarmSet == true) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null
            )
        }

        Button(
            onClick = { showDialog = true },
            enabled = isImportExportEnabled ?: true
        ) {
            Text("导入")
        }

        Button(
            onClick = { viewModel.exportEvents() },
            enabled = isImportExportEnabled ?: true,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("导出")
        }
    }

    if (showDialog) {
        ImportEventsDialog(
            onDismiss = { showDialog = false },
            onImport = { text -> viewModel.importEvents(text) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInputField(viewModel: EventsViewModel) {
    val newEventName by viewModel.newEventName.observeAsState()
    val focusRequester = remember { FocusRequester() }

    Row {
        TextField(
            value = newEventName ?: "",
            onValueChange = { newValue -> viewModel.newEventName.value = newValue },
            label = { Text("事件名称") },
            modifier = Modifier.focusRequester(focusRequester)
        )

        Button(onClick = { viewModel.onConfirm() }) {
            Text("确认")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun EventButtons(viewModel: EventsViewModel) {
    val mainEventButtonText by viewModel.mainEventButtonText.observeAsState()
    val subEventButtonText by viewModel.subEventButtonText.observeAsState()
    val mainButtonShow by viewModel.mainButtonShow.observeAsState()
    val subButtonShow by viewModel.subButtonShow.observeAsState()

    Row {
        if (mainButtonShow == true) {
            Button(onClick = { viewModel.toggleMainEvent() }) {
                Text(text = mainEventButtonText ?: "开始")
            }
        }

        if (subButtonShow == true) {
            TextButton(
                onClick = { viewModel.toggleSubEvent() },
                modifier = Modifier.padding(start = 5.dp)
            ) {
                Text(text = subEventButtonText ?: "插入")
            }
        }
    }
}


@Composable
fun DurationSlider(viewModel: EventsViewModel) {
    val rate by viewModel.rate.collectAsState()
    val remainingDuration by viewModel.remainingDuration.collectAsState()
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "当下核心事务：",
            modifier = Modifier.padding(start = 8.dp)
        )

        Slider(
            value = rate ?: 0f,
            enabled = false,
            onValueChange = {  },
            modifier = Modifier.weight(1f)
        )

        Text(
            text = rate?.let { "%.1f".format(it.times(100)) + "%" } ?: "0%",
            modifier = Modifier.padding(start = 8.dp)
        )

        Text(
            text = hourThreshold.minus(remainingDuration)?.let { formatDurationInText(it) } ?: "...",
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
    }
}


@SuppressLint("UnrememberedMutableState")
@Composable
fun EventItemRow(
    event: Event,
    showTime: Boolean,
    viewModel: EventsViewModel,
    modifier: Modifier = Modifier
) {
    Log.i("打标签喽", "EventItemRow 重组执行！id = ${event.id}")
    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var duration by remember { mutableStateOf(event.duration) }
    val selectedEventIdsMap by viewModel.selectedEventIdsMap.observeAsState(mutableMapOf())

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
                modifier = Modifier.padding(end = 5.dp),
                onDragDelta = { dragValue ->
                    startTime = startTime.plusMinutes(dragValue.toLong())

                    if (duration != null) {
                        val delta = Duration.between(startTime, event.startTime)
                        Log.i("打标签喽", "delta = $delta")
                        duration = event.duration!! + delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(startTime = startTime, duration = duration)
                    viewModel.updateTimeToDB(updatedEvent)
                }
            )
        }

        Text(
            text = if (showTime) event.name else "……${event.name}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable {
                viewModel.onNameTextClicked(event)
            }.let { modifier ->
                if (selectedEventIdsMap[event.id] == true) {
                    modifier
                        .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
                        .padding(3.dp)
                } else modifier
            }
        )

        if (showTime) {
            DraggableText(
                text = endTime?.let { formatLocalDateTime(it) } ?: "...",
                modifier = Modifier.padding(start = 5.dp),
                onDragDelta = { dragValue ->
                    // 还没有结束时间的时候禁止拖动
                    if (endTime != null) {
                        endTime = endTime!!.plusMinutes(dragValue.toLong())
                        val delta = Duration.between(endTime, event.endTime)
                        duration = event.duration!! - delta
                    }
                },
                onDragStopped = {
                    val updatedEvent = event.copy(endTime = endTime, duration = duration)
                    viewModel.updateTimeToDB(updatedEvent)
                }
            )
        }

        Text(
            text = duration?.let { formatDurationInText(it) } ?: "...",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 8.dp)
        )

        if (event.hasTriggeredReminder && showTime) {
            Text(
                text = "⏰",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun EventItem(
    event: Event,
    subEvents: List<Event> = listOf(),
    viewModel: EventsViewModel
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(4.dp)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ImportEventsDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入时间记录") },
        text = {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("请输入要导入的源文本") },
                modifier = Modifier.heightIn(min = 56.dp, max = 560.dp)  // Assuming each line is 56.dp
            )
        },
        confirmButton = {
            Button(onClick = {
                onImport(inputText)
                onDismiss()
            }) {
                Text("导入")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}


@Composable
fun DraggableText(
    text: String,
    onDragDelta: (Float) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val speedList = remember { mutableStateListOf<Float>() }
    val lastDragTime = remember { mutableStateOf<Long?>(null) }
    val lastDelta = remember { mutableStateOf(0f) }

    Text(
        text = text,
        modifier = modifier.draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                val currentTime = System.currentTimeMillis()
                val timeDiff = if (lastDragTime.value != null) currentTime - lastDragTime.value!! else 0
                lastDragTime.value = currentTime

                val speed = if (timeDiff != 0L) delta / timeDiff else 0f
                speedList.add(speed)

                lastDelta.value = delta
            },
            onDragStarted = { speedList.clear() },
            onDragStopped = {
                val maxSpeed = speedList.maxOrNull() ?: 0f
                Log.i("打标签喽", "maxSpeed = $maxSpeed")
                val dragCoefficient = if (maxSpeed > 6) 10f else 2f
                val direction = if (lastDelta.value > 0) 1 else -1
                val dragValue = dragCoefficient * direction
                onDragDelta(dragValue)

                onDragStopped()
            }
        )
    )
}





