package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.ListItem
import com.huaguang.flowoftime.ui.components.current_item.CurrentItem
import com.huaguang.flowoftime.widget.CustomSwipeToDismiss

@Composable
fun ColumnScope.EventList(
    mediator: EventTrackerMediator,
    listState: LazyListState,
    scrollIndex: Int,
) {

//    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val events by mediator.eventsFlow.collectAsState(emptyList())

    Box(
        modifier = Modifier.weight(1f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter), // 这正是为 LazyColumn 外套一个 Box 的原因
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(events) { index, event ->
                CustomSwipeToDismiss(
                    event = event,
                    sharedState = mediator.sharedState,
                    dismissed = {
//                        mediator.deleteItem(event, subEvents)
                    }
                ) {
//                    EventItem(event = event, subEvents = subEvents, mediator = mediator)
                }
            }

            item {
                CurrentItem(mediator = mediator)
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.MyList(mediator: EventTrackerMediator) {
    val eventsByDate by mediator.eventsByDateFlow.collectAsState(mapOf())

    LazyColumn(
        modifier = Modifier.weight(1f)
    ) {
        eventsByDate.forEach { (date, events) -> // 一个 Map 里边含有多个键值对（循环创建这些键、值 UI）

            stickyHeader {// 创建日期（键）UI
                DateItem(date)
            }

            val listItems = events.map { event -> // 在创建值 UI 前，先将值映射成另一个值！
                if (event.parentId == null) {
                    ListItem.MainItem(event)
                } else {
                    ListItem.SubItem(event)
                }
            }

            itemsIndexed(listItems) { index, item -> // 创建事件列表（值）UI
                when (item) {
                    is ListItem.MainItem -> MainItem(item)
                    is ListItem.SubItem -> SubItem(item)
                }
            }

        }

    }
}


