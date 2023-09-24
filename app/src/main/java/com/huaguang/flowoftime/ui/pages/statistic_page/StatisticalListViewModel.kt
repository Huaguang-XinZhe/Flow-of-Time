package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.ui.state.IdState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class StatisticalListViewModel @Inject constructor(
    val repository: DailyStatisticsRepository,
    val idState: IdState,
) : ViewModel() {
//    private val _yesterdaysDailyStatisticsFlow = MutableStateFlow<List<DailyStatistics>>(listOf())
//    val yesterdaysDailyStatisticsFlow: StateFlow<List<DailyStatistics>> = _yesterdaysDailyStatisticsFlow.asStateFlow()
    var sumDuration = mutableStateOf(Duration.ZERO)
    var data = listOf<Pair<String, Float>>()
    var referenceValue = 0f

    init {
        viewModelScope.launch {
            repository.getYesterdaysDailyStatisticsFlow()
                .onStart {
                    repository.deleteEntryByEmptyDuration() // 清理掉统计表中时长为 0 的条目
                }
                .collect { yesterdaysDailyStatistics ->
                    sumDuration.value = yesterdaysDailyStatistics // 计算昨天的时长总计
                        .map { it.totalDuration }
                        .fold(Duration.ZERO) { acc, duration -> acc + duration }
                    data = yesterdaysDailyStatistics.map { // 获取横向条形图的数据
                        it.category to it.totalDuration.toMinutes().toFloat()
                    }
                    referenceValue = data.first().second

//                    _yesterdaysDailyStatisticsFlow.value = yesterdaysDailyStatistics
                }
        }
    }
}