package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventTrackerScreen(viewModel: EventsViewModel) {
    val isTracking by viewModel.isTracking.observeAsState(false)
    val listState = rememberLazyListState()
    val eventsWithSubEvents by viewModel.eventsWithSubEvents.collectAsState(emptyList())

    HandleScrollEffect(viewModel, listState)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderRow(viewModel)

        DurationSlider(viewModel)

        EventList(viewModel, listState, eventsWithSubEvents, Modifier.weight(1f))

        if (isTracking) {
            EventInputField(viewModel)
        }

        EventButtons(viewModel)
    }
}

@Composable
fun HandleScrollEffect(
    viewModel: EventsViewModel,
    listState: LazyListState
) {
    val scrollIndex by viewModel.scrollIndex.observeAsState()
    val scope = rememberCoroutineScope()
    val firstLaunch = remember { mutableStateOf(true) }

    LaunchedEffect(scrollIndex) {
        scrollIndex?.let { index ->
            scope.launch {
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
}

@Composable
fun HeaderRow(viewModel: EventsViewModel) {
    val isAlarmSet by viewModel.isAlarmSet.observeAsState()
    val isImportExportEnabled by viewModel.isImportExportEnabled.observeAsState()
    var showDialog by remember { mutableStateOf(false) }
    val isOneDayButtonClicked by viewModel.isOneDayButtonClicked.collectAsState()

    val toggleButtonText = if (isOneDayButtonClicked) "RecentTwoDays" else "OneDay"

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

        OutlinedButton(
            onClick = { viewModel.toggleListDisplayState() },
            modifier = Modifier.padding(start = 5.dp)
        ) {
            Text(toggleButtonText)
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
    var textFieldState by remember {
        mutableStateOf(TextFieldValue(text = newEventName ?: ""))
    }

    Column {
        IconButton(
            onClick = {
                viewModel.undoTiming()
            },
            modifier = Modifier.size(36.dp)
                .padding(start = 10.dp, bottom = 5.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.revocation),
                contentDescription = null,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textFieldState,
                onValueChange = {
                    textFieldState = it
                    viewModel.newEventName.value = it.text
                },
                label = { Text("事件名称") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused) {
                            // 这将全选文本
                            textFieldState = textFieldState.copy(
                                selection = TextRange(0, textFieldState.text.length)
                            )
                        }
                    }
            )

            Button(onClick = { viewModel.onConfirm() }) {
                Text("确认")
            }
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {
        if (mainButtonShow == true) {
            LongPressButton(
                onClick = { viewModel.toggleMainEvent() },
                onLongClick = { viewModel.onMainButtonLongClick() },
                text = mainEventButtonText ?: "开始"
            )
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
            text = FOCUS_EVENT_DURATION_THRESHOLD.minus(remainingDuration ?: FOCUS_EVENT_DURATION_THRESHOLD)
                ?.let { formatDurationInText(it) } ?: "...",
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )
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


