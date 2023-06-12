package com.huaguang.flowoftime.ui.components.event_time

import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.ItemSelectionTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventTimeViewModel @Inject constructor() : ViewModel() {

    val selectionTracker = ItemSelectionTracker()

    fun unBorder(eventId: Long) {
        selectionTracker.cancelSelection(eventId) // 取消滑块阴影，禁止点击
    }

}