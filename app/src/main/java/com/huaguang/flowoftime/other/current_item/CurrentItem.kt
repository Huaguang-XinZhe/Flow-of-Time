package com.huaguang.flowoftime.other.current_item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.huaguang.flowoftime.other.EventItem
import com.huaguang.flowoftime.other.EventTrackerMediator
import com.huaguang.flowoftime.other.widget.CustomSwipeToDismiss
import java.time.LocalDateTime

@Composable
fun CurrentItem(mediator: EventTrackerMediator) {
    val currentEvent by mediator.currentItemViewModel.currentEvent
    val initialized by mediator.initialized

    if (initialized && currentEvent != null) {
        currentEvent!!.let {
            if (it.name != "￥为减少重组，优化频闪，不显示的特别设定￥" && it.endTime != LocalDateTime.MIN) {
                CustomSwipeToDismiss(
                    sharedState = mediator.sharedState,
                    dismissed = { mediator.deleteItem(it) }
                ) {
                    EventItem(
                        event = it,
                        mediator = mediator
                    )
                }

            }
        }
    }
}