package com.huaguang.flowoftime.ui.pages.display_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DRListViewModel @Inject constructor(
    val repository: EventRepository,

) : ViewModel() {

    private val _recentTwoDaysCombinedEventsFlow = MutableStateFlow<List<CombinedEvent?>>(listOf(null)) // 最开始什么都没有就为空值
    val recentTwoDaysCombinedEventsFlow: StateFlow<List<CombinedEvent?>>
        get() = _recentTwoDaysCombinedEventsFlow.asStateFlow() // TODO: 这里用 getter 和不用 getter 有什么区别？
    private val _latestXXXIntervalDaysFlow = MutableStateFlow(0)
    val latestXXXIntervalDaysFlow: StateFlow<Int> = _latestXXXIntervalDaysFlow.asStateFlow()

    init {
        viewModelScope.launch {
            // 这里可以用 filterNotNull 筛除 null 值，使数据库为空时 UI 观察不到，不需要观察到。
            repository.getRecentTwoDaysCombinedEventsFlow().filterNotNull().collect { recentTwoDaysCombinedEvents ->
                _recentTwoDaysCombinedEventsFlow.value = recentTwoDaysCombinedEvents
            }
        }

        viewModelScope.launch {  // 必须分开进行，因为收集会挂起协程，如果放在一起，后边的代码不会执行
            repository.getLatestXXXIntervalDaysFlow().filterNotNull().collect { intervalDays -> // 筛除空值
                _latestXXXIntervalDaysFlow.value = intervalDays
            }

        }
    }
}