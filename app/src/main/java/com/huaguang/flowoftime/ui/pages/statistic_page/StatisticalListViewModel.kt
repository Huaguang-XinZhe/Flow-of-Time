package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.IdState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class StatisticalListViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    val repository: DailyStatisticsRepository,
    val idState: IdState,
) : ViewModel() {
    private val _yesterdaysDailyStatisticsFlow = MutableStateFlow<List<DailyStatistics>>(listOf())
    val yesterdaysDailyStatisticsFlow: StateFlow<List<DailyStatistics>> = _yesterdaysDailyStatisticsFlow.asStateFlow()
    val sumDuration = mutableStateOf(Duration.ZERO)

    init {
        viewModelScope.launch {
            repository.getYesterdaysDailyStatisticsFlow()
                .onStart {
                    idState.apply {
                        RDALogger.info("startId = $startId, endId = $endId")
                        if (startId == endId) return@onStart

                        // 在开始收集流之前，执行类属时长的计算并填充统计表
                        val events = eventRepository.getEventsByIdRange(startId, endId)
                        if (events.isEmpty()) return@onStart

                        repository.upsertDailyStatistics(events)
                        startId = endId // 重置 startId，已经计算过了
                    }
                }
                .collect { yesterdaysDailyStatistics ->
                    sumDuration.value = yesterdaysDailyStatistics // 计算昨天的时长总计
                        .map { it.totalDuration }
                        .fold(Duration.ZERO) { acc, duration -> acc + duration }

                    _yesterdaysDailyStatisticsFlow.value = yesterdaysDailyStatistics
                }
        }
    }
}