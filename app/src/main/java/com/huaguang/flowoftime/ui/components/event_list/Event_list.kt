package com.huaguang.flowoftime.ui.components.event_list

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.huaguang.flowoftime.EventsViewModel
import com.huaguang.flowoftime.widget.CustomSwipeToDismiss

@Composable
fun EventList(
    viewModel: EventsViewModel,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Log.i("打标签喽", "EventList 重组！！！")
//    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val eventsWithSubEvents by viewModel.eventsWithSubEvents.collectAsState(emptyList())

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = eventsWithSubEvents,
                key = { eventWithSubEvents ->
                    // Use the event's id as the key
                    eventWithSubEvents.event.id
                }
            ) { (event, subEvents) ->
                CustomSwipeToDismiss(
                    event = event,
                    viewModel = viewModel,
                    dismissed = { viewModel.deleteItem(event, subEvents) }
                ) {
                    EventItem(event = event, subEvents = subEvents, viewModel = viewModel)
                }
            }

            item {
                CurrentItem(viewModel = viewModel)
            }
        }
    }
}




