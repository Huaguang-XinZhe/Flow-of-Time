package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.utils.formatDuration
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewModel.EventsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventTrackerScreen(viewModel: EventsViewModel) {
    val textState = remember { mutableStateOf("") }
    val isTracking by viewModel.isTracking.observeAsState()
    val isAlarmSet by viewModel.isAlarmSet.observeAsState()
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
        AlarmSetIcon(isAlarmSet == true)

        DurationSlider(viewModel = viewModel)

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.align(Alignment.BottomCenter),
                state = listState
            ) {
                items(eventsWithSubEvents) {(event, subEvents) ->
                    EventItem(event, subEvents)
                }
            }
        }

        if (isTracking == true) {
            EventInputField(textState) { viewModel.onConfirm(it) }
        }

        EventButtons(viewModel)
    }
}

@Composable
fun AlarmSetIcon(isAlarmSet: Boolean) {
    if (isAlarmSet) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInputField(
    textState: MutableState<String>,
    onConfirm: (MutableState<String>) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Row {
        TextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            label = { Text("事件名称") },
            modifier = Modifier.focusRequester(focusRequester)
        )

        Button(onClick = { onConfirm(textState) }) {
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
            text = remainingDuration?.let { formatDuration(it) } ?: "8 小时",
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
    }
}


@Composable
fun EventItemRow(
    event: Event,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (showTime) {
            Text(
                text = formatLocalDateTime(event.startTime),
                modifier = Modifier.padding(end = 5.dp)
            )
        }

        Text(
            text = if (showTime) event.name else "……${event.name}",
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (showTime) {
            Text(
                text = event.endTime?.let { formatLocalDateTime(it) } ?: "...",
                modifier = Modifier.padding(start = 5.dp)
            )
        }

        Text(
            text = event.duration?.let { formatDuration(it) } ?: "...",
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
fun EventItem(event: Event, subEvents: List<Event> = listOf()) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            EventItemRow(event = event, showTime = true)

            // 插入的临时事件的 UI
            for (subEvent in subEvents) {
                EventItemRow(
                    event = subEvent,
                    showTime = false,
                    modifier = Modifier.padding(start = 30.dp)  // 添加了一些左侧的 padding 以便缩进
                )
            }
        }
    }
}
