package com.huaguang.flowoftime.ui.pages.display_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class DisplayListPageViewModel( // 注意：综合的 ViewModel 不要使用依赖注入，否则会报错！（已经在 Fragment 中构建实例了）
    val inputViewModel: EventInputViewModel,
    val repository: EventRepository,
) : ViewModel() {

    private val _recentTwoDaysCombinedEventsFlow = MutableStateFlow<List<CombinedEvent?>>(listOf(null)) // 最开始什么都没有就为空值
    val recentTwoDaysCombinedEventsFlow: StateFlow<List<CombinedEvent?>>
        get() = _recentTwoDaysCombinedEventsFlow.asStateFlow() // TODO: 这里用 getter 和不用 getter 有什么区别？


    init {
        viewModelScope.launch {
            // 这里可以用 filterNotNull 筛除 null 值，使数据库为空时 UI 观察不到，不需要观察到。
            repository.getRecentTwoDaysCombinedEventsFlow().filterNotNull().collect { recentTwoDaysCombinedEvents ->
                _recentTwoDaysCombinedEventsFlow.value = recentTwoDaysCombinedEvents
            }
        }
    }


}