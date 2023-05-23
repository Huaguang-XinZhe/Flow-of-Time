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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.utils.formatDuration
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.viewModel.EventsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTrackerScreen(viewModel: EventsViewModel) {
    Log.i("打标签喽", "页面重组！！！")
    val context = LocalContext.current

    val textState = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val events by viewModel.events.collectAsState(emptyList())
    val buttonText by viewModel.buttonText.observeAsState()
    val isTracking by viewModel.isTracking.observeAsState()
    val scrollIndex by viewModel.scrollIndex.observeAsState()
    val isAlarmSet by viewModel.isAlarmSet.observeAsState()

    LaunchedEffect(scrollIndex) {
        scrollIndex?.let { index ->
            scope.launch {
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
        if (isAlarmSet == true) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.align(Alignment.BottomCenter),
                state = listState
            ) {
                items(events) { event ->
                    EventItem(event)
                }
            }
        }

        if (isTracking == true) {
            Row {
                TextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    label = { Text("事件名称") },
                    modifier = Modifier.focusRequester(focusRequester)
                )

                Button(onClick = {
                    viewModel.onConfirm(textState)
                }) {
                    Text("确认")
                }
            }

            LaunchedEffect(isTracking) {
                focusRequester.requestFocus()
            }
        }

        Button(onClick = { viewModel.onClick() }) {
            Text(text = buttonText ?: "开始")
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = "事件名称: ${event.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "开始时间: ${ formatLocalDateTime(event.startTime) }")
            Text(text = "结束时间: ${ event.endTime?.let { formatLocalDateTime(it) } ?: "正在进行中..." }")
            Text(text = "用时: ${ event.duration?.let { formatDuration(it) } }")
            Text(text = "是否触发过提醒: ${ if (event.hasTriggeredReminder) "是" else "否" }")
        }
    }
}
