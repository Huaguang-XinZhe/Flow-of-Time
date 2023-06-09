package com.huaguang.flowoftime.ui.components

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
import com.huaguang.flowoftime.ui.components.current_item.CurrentItem
import com.huaguang.flowoftime.widget.CustomSwipeToDismiss

@Composable
fun EventList(
    mediator: EventTrackerMediator,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

//    val lazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
    val eventsWithSubEvents by mediator.eventsWithSubEvents.collectAsState(emptyList())

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
                    sharedState = mediator.sharedState,
                    dismissed = { mediator.deleteItem(event, subEvents) }
                ) {
                    EventItem(event = event, subEvents = subEvents, mediator = mediator)
                }
            }

            item {
                CurrentItem(mediator = mediator)
            }
        }
    }
}




