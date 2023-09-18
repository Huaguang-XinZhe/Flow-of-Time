package com.huaguang.flowoftime.ui.pages.display_list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.huaguang.flowoftime.BlockType
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.pages.time_record.ClassNameInputAlertDialog
import com.huaguang.flowoftime.ui.state.ItemState

@Composable
fun DisplayListPage(
    viewModel: DisplayListPageViewModel,
) {
    val recentTwoDaysCombinedEvents by viewModel.recentTwoDaysCombinedEventsFlow.collectAsState()
    if (recentTwoDaysCombinedEvents.contains(null)) return // 列表中含有空值就返回，不显示 UI

    val itemState = remember { ItemState(BlockType.DISPLAY, mutableStateOf(Mode.DISPLAY)) } // 初始值

    LazyColumn {
        items(recentTwoDaysCombinedEvents) { item: CombinedEvent? ->

            DRToggleItem(
                itemState = itemState,
                combinedEvent = item,
                viewModel = viewModel.inputViewModel
            )

        }
    }

    EventInputField(
        viewModel = viewModel.inputViewModel,
    )

    ClassNameInputAlertDialog(
        viewModel = viewModel.inputViewModel
    )

}