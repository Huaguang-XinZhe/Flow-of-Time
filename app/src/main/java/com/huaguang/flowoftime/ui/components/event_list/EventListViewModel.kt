package com.huaguang.flowoftime.ui.components.event_list

import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.dao.EventDao
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.utils.isCoreEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventListViewModel @Inject constructor(
    private val eventDao: EventDao,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    val dismissedItems = mutableSetOf<Long>() // 为了防止删除逻辑多次执行

    fun deleteItem(event: Event, subEvents: List<Event> = listOf()) {
        if (dismissedItems.contains(event.id)) return

        val isCoreEvent = isCoreEvent(event.name)

        if (event.id != 0L) { // 删除项已经存入数据库中了，排除已经插入了子事件的主事件（有点复杂，不处理这样的场景）
            Log.i("打标签喽", "删除已经入库的条目")
            viewModelScope.launch(Dispatchers.IO) {
                eventDao.deleteEvent(event.id)
                for (subEvent in subEvents) {
                    eventDao.deleteEvent(subEvent.id)
                }
            }

            if (isCoreEvent) { // 删除的是当下核心事务
                coreDuration.value -= event.duration
            }
        } else { // 删除的是当前项（正在计时）
            resetState(isCoreEvent, true)
        }

        // 在删除完成后，将该 id 添加到已删除项目的记录器中
        if (event.id != 0L) { // 当前项的 id 始终是 0，就不要加进来限制执行次数了。
            dismissedItems.add(event.id)
        }
    }

}