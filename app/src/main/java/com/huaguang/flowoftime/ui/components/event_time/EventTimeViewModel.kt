package com.huaguang.flowoftime.ui.components.event_time

import androidx.lifecycle.AndroidViewModel
import com.huaguang.flowoftime.ItemSelectionTracker
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.dao.EventDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventTimeViewModel @Inject constructor(
    private val eventDao: EventDao,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    val selectionTracker = ItemSelectionTracker()

    fun unBorder(eventId: Long) {
        selectionTracker.cancelSelection(eventId) // 取消滑块阴影，禁止点击
    }

}