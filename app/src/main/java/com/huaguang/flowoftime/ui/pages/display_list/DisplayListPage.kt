package com.huaguang.flowoftime.ui.pages.display_list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // 为避免设值时触发重组，不能使用 MutableStateMapOf，只能使用普通的可变 Map。
    val toggleMap = remember { mutableMapOf<Long, ItemState>() } // Map 必须放在外边，如果放在 items 块内，对于每个 item 就都会创建一个 map

    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(recentTwoDaysCombinedEvents) { item: CombinedEvent? ->
            val eventId = item!!.event.id
            if (toggleMap[eventId] == null) { // items 块会多次执行（列表滑动引起），为保证状态实例的唯一性，必须进行 null 检查，只有为 null 时才重新创建
                toggleMap[eventId] = ItemState.initialDisplay()
            }

            DRToggleItem(
                itemState = toggleMap[eventId] ?: ItemState.initialDisplay(),
                combinedEvent = item,
                viewModel = viewModel.inputViewModel
            )

        }
    }

    EventInputField(
        viewModel = viewModel.inputViewModel,
        modifier = Modifier.padding(top = 300.dp)
    )

    ClassNameInputAlertDialog(
        viewModel = viewModel.inputViewModel
    )

}