package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.ui.components.DisplayEventItem
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import kotlinx.coroutines.delay

@Composable
fun DisplayAndRecordingItemColumn(
    viewModel: EventInputViewModel,
    customTimeState: MutableState<CustomTime?>,
    modifier: Modifier = Modifier
) {
    val secondLatestCombinedEvent by viewModel.secondLatestCombinedEventFlow.collectAsState(null)
    // 已经过滤空值，event 接受到 Flow 的值后将始终为非空。尽管如此，初始值还是空的，这意味着最初的显示 event 为 null。
    val combinedEvent by viewModel.currentCombinedEventFlow.collectAsState(initial = null)
    val lazyColumnState = rememberLazyListState()
    val dynamicHeight = if (viewModel.inputState.show.value) 340.dp else 460.dp
    val displayItemState = LocalDisplayItemState.current
    val recordingItemState = LocalRecordingItemState.current

        LaunchedEffect(viewModel.scrollTrigger.value) {
        val offset = viewModel.scrollOffset.value
//        RDALogger.info("offset = $offset")
        if (offset == 0f) return@LaunchedEffect // 初始化的时候不需要滑动

        delay(100) // 先等软键盘弹上来
        lazyColumnState.scrollBy(offset)
    }

    LazyColumn(
        state = lazyColumnState,
        modifier = modifier
            .fillMaxWidth()  // 只要加了这个，滑动没有内容的区域也才可以滚动
            .height(dynamicHeight) // 没有输入框的时候可以到 460
    ) {
        item {
            DRToggleItem(
                itemState = displayItemState,
                combinedEvent = secondLatestCombinedEvent,
                customTimeState = customTimeState,
                viewModel = viewModel
            )
        }

        item {
            DRToggleItem(
                itemState = recordingItemState,
                combinedEvent = combinedEvent,
                customTimeState = customTimeState, // TODO: 这个似乎可以优化
                viewModel = viewModel
            )
        }

    }
}

@Composable
fun DRToggleItem(
    itemState: MutableState<ItemType>,
    combinedEvent: CombinedEvent?,
    customTimeState: MutableState<CustomTime?>,
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier
) {
    if (itemState.value == ItemType.DISPLAY) {
        DisplayEventItem(
            combinedEvent = combinedEvent,
            viewModel = viewModel,
            itemState = itemState,
            modifier = modifier,
        )
    } else {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 10.dp)
        ) {
            RecordingEventItem(
                combinedEvent = combinedEvent,
                customTimeState = customTimeState,
                viewModel = viewModel,
                itemState = itemState,
                modifier = modifier.padding(5.dp),
            )
        }
    }
}