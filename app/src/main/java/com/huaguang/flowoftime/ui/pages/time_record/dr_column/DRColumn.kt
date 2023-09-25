package com.huaguang.flowoftime.ui.pages.time_record.dr_column

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.pages.time_record.LocalDisplayItemState
import com.huaguang.flowoftime.ui.pages.time_record.LocalRecordingItemState

@Composable
fun DRColumn(
    modifier: Modifier = Modifier,
    viewModel: DRColumnViewModel = viewModel()
) {
    val secondLatestCombinedEvent by viewModel.secondLatestCombinedEventFlow.collectAsState(null)
    // 已经过滤空值，event 接受到 Flow 的值后将始终为非空。尽管如此，初始值还是空的，这意味着最初的显示 event 为 null。
    val combinedEvent by viewModel.currentCombinedEventFlow.collectAsState(initial = null)
    val lazyColumnState = rememberLazyListState()
    val dynamicHeight = if (viewModel.inputState.show.value) 340.dp else 460.dp
    val displayItemState = LocalDisplayItemState.current
    val recordingItemState = LocalRecordingItemState.current
    val dashButtonShow = remember { mutableStateOf(true) }

//    LaunchedEffect(viewModel.scrollTrigger.value) {
//        val offset = viewModel.scrollOffset.floatValue
////        RDALogger.info("offset = $offset")
//        if (offset == 0f) return@LaunchedEffect // 初始化的时候不需要滑动
//
//        delay(100) // 先等软键盘弹上来
//        lazyColumnState.scrollBy(offset)
//    }

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
                dashButtonShow = dashButtonShow,
            )
        }

        item {
            DRToggleItem(
                itemState = recordingItemState,
                combinedEvent = combinedEvent,
                dashButtonShow = dashButtonShow,
            )
        }

    }
}
