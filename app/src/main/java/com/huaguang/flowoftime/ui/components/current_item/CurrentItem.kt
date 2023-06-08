package com.huaguang.flowoftime.ui.components.current_item

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.huaguang.flowoftime.ui.components.event_list.EventItem
import com.huaguang.flowoftime.ui.screens.event_tracker.EventTrackerScreenViewModel
import com.huaguang.flowoftime.widget.CustomSwipeToDismiss
import java.time.LocalDateTime

@Composable
fun CurrentItem(viewModel: EventTrackerScreenViewModel) {
    Log.i("打标签喽", "CurrentItem 重组！")
    val currentEvent by viewModel.currentItemViewModel.currentEvent
    val initialized by viewModel.initialized

    if (initialized && currentEvent != null) {
        currentEvent!!.let {
            if (it.name != "￥为减少重组，优化频闪，不显示的特别设定￥" && it.endTime != LocalDateTime.MIN) {
                CustomSwipeToDismiss(
                    viewModel = viewModel,
                    dismissed = { viewModel.deleteItem(it) }
                ) {
                    EventItem(
                        event = it,
                        viewModel = viewModel
                    )
                }

            }
        }
    }
}