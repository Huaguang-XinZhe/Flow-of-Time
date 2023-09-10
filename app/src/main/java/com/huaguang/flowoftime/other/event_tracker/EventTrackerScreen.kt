package com.huaguang.flowoftime.other.event_tracker

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
import com.huaguang.flowoftime.other.EventList
import com.huaguang.flowoftime.other.EventTrackerMediator
import com.huaguang.flowoftime.other.duration_slider.DurationSlider
import com.huaguang.flowoftime.other.header.HeaderRow
import com.huaguang.flowoftime.ui.state.SharedState
import kotlinx.coroutines.launch

@Composable
fun EventTrackerScreen(mediator: EventTrackerMediator) {
    val listState = rememberLazyListState()
//    val isInputShow by mediator.sharedState.isInputShow

    HandleScrollEffect(mediator.sharedState, listState)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderRow(mediator.headerViewModel)

        DurationSlider(mediator.durationSliderViewModel)

        EventList(mediator, listState, Modifier.weight(1f))

//        if (isInputShow) {
////            EventInputField(mediator)
//        }
//
//        if (!isInputShow) {
////            EventButtons(mediator)
//        }
    }
}

@Composable
fun HandleScrollEffect(
    sharedState: SharedState,
    listState: LazyListState
) {
    val scrollIndex by sharedState.scrollIndex
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




