package com.huaguang.flowoftime.ui.screens.event_tracker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.EventsViewModel
import com.huaguang.flowoftime.ui.components.duration_slider.DurationSlider
import com.huaguang.flowoftime.ui.components.event_buttons.EventButtons
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.components.event_list.EventList
import com.huaguang.flowoftime.ui.components.header.HeaderRow
import kotlinx.coroutines.launch

@Composable
fun EventTrackerScreen(viewModel: EventTrackerScreenViewModel) {
    val listState = rememberLazyListState()
    val isInputShow by viewModel.isInputShow

    HandleScrollEffect(viewModel, listState)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderRow(viewModel)

        DurationSlider(viewModel)

        EventList(viewModel, listState, Modifier.weight(1f))

        if (isInputShow) {
            EventInputField(viewModel)
        }

        if (!isInputShow) {
            EventButtons(viewModel)
        }
    }
}

@Composable
fun HandleScrollEffect(
    viewModel: EventsViewModel,
    listState: LazyListState
) {
    val scrollIndex by viewModel.scrollIndex
    val scope = rememberCoroutineScope()
    val firstLaunch = remember { mutableStateOf(true) }

    LaunchedEffect(scrollIndex) {
        scope.launch {
            if (firstLaunch.value) {
                Log.i("打标签喽", "不延迟！！！")
                firstLaunch.value = false
            }
            listState.animateScrollToItem(scrollIndex)
            Log.i("打标签喽", "滚动到索引：$scrollIndex")
        }
    }
}




