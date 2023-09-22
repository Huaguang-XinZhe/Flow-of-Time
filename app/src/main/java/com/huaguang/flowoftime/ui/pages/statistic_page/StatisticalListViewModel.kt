package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.ui.state.IdState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class StatisticalListViewModel @Inject constructor(
    val repository: DailyStatisticsRepository,
    val idState: IdState,
) : ViewModel() {
    private val _yesterdaysDailyStatisticsFlow = MutableStateFlow<List<DailyStatistics>>(listOf())
    val yesterdaysDailyStatisticsFlow: StateFlow<List<DailyStatistics>> = _yesterdaysDailyStatisticsFlow.asStateFlow()
    val sumDuration = mutableStateOf(Duration.ZERO)

    init {
        viewModelScope.launch {
            repository.getYesterdaysDailyStatisticsFlow()
                .collect { yesterdaysDailyStatistics ->
                    sumDuration.value = yesterdaysDailyStatistics // 计算昨天的时长总计
                        .map { it.totalDuration }
                        .fold(Duration.ZERO) { acc, duration -> acc + duration }

                    _yesterdaysDailyStatisticsFlow.value = yesterdaysDailyStatistics
                }
        }
    }
}