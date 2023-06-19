package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.huaguang.flowoftime.ListItem
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyList(listItems: List<ListItem>) {
    LazyColumn {

        stickyHeader {
            Text(
                text = "Header",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF464549))
            )
        }

        itemsIndexed(listItems) { index, item ->
            when (item) {
                is ListItem.DateItem -> DateItem(item)
                is ListItem.MainItem -> MainItem(item)
                is ListItem.SubItem -> SubItem(item)
                is ListItem.Interval -> Interval(item)
            }
        }

        

    }
}


