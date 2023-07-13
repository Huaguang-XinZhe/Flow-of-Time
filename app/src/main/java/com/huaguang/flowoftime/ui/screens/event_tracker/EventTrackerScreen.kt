package com.huaguang.flowoftime.ui.screens.event_tracker

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.ui.components.EventInputField
import com.huaguang.flowoftime.ui.components.EventTrackerMediator
import com.huaguang.flowoftime.ui.components.MyList
import com.huaguang.flowoftime.ui.components.duration_slider.DurationSlider
import com.huaguang.flowoftime.ui.components.event_buttons.EventButtons
import com.huaguang.flowoftime.ui.components.header.HeaderRow
import kotlinx.coroutines.launch

@Composable
fun EventTrackerScreen(mediator: EventTrackerMediator) {
    val listState = rememberLazyListState()
    val isInputShow by mediator.sharedState.isInputShow
    val scrollIndex by mediator.sharedState.scrollIndex
    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollIndex) {
        scope.launch {
            listState.animateScrollToItem(scrollIndex)
            Log.i("打标签喽", "滚动到索引：$scrollIndex")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
//            .padding(10.dp) 注意，如果此处加 padding，那么上下左右都会加 padding，输入框和软键盘的连接处也会加！！！
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeaderRow(mediator.headerViewModel)

        DurationSlider(mediator.durationSliderViewModel)

//        EventList(mediator, listState, scrollIndex)
        MyList(mediator = mediator)
        
        if (isInputShow) {
            EventInputField(mediator)
        }

        if (!isInputShow) {
            EventButtons(mediator)
        }
    }
}




