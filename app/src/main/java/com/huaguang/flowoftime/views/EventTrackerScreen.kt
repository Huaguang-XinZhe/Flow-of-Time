package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.viewModel.EventsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTrackerScreen(viewModel: EventsViewModel) {
    Log.i("打标签喽", "页面重组！！！")

    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val events by viewModel.events.collectAsState(emptyList())
    val buttonText by viewModel.buttonText.observeAsState()
    val isTracking by viewModel.isTracking.observeAsState()

    Column {
        Button(onClick = { viewModel.onClick() }) {
            Text(text = buttonText ?: "开始")
        }

        if (isTracking == true) {
            Row {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("事件名称") },
                    modifier = Modifier.focusRequester(focusRequester)
                )

                Button(onClick = {
                    viewModel.onConfirm(text)
                }) {
                    Text("确认")
                }
            }

            LaunchedEffect(isTracking) {
                focusRequester.requestFocus()
            }
        }

        LazyColumn {
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Column {
            Text(text = "事件名称: ${event.name}")
            Text(text = "开始时间: ${event.startTime}")
            Text(text = "结束时间: ${event.endTime ?: "正在进行中..."}")
            Text(text = "用时: ${event.duration}")
            Text(text = "是否触发过提醒: ${if (event.hasTriggeredReminder) "是" else "否"}")
        }
    }
}
