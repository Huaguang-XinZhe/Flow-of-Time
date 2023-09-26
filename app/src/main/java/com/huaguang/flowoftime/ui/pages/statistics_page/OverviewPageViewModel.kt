package com.huaguang.flowoftime.ui.pages.statistics_page

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class OverviewPageViewModel @Inject constructor(
    private val dailyRepository: DailyStatisticsRepository
): ViewModel() {
    val statisticsByCategory = mutableStateMapOf<String?, List<DailyStatistics>>()
    val averageDurationByCategory = mutableStateMapOf<String?, Duration>()
    val loadButtonShow = mutableStateOf(true)

    fun loadDateDurationData() {
        viewModelScope.launch {
            val groupedMap = dailyRepository.getAllDailyStatistics()
                .filterNot { it.date.isEqual(getAdjustedEventDate()) } // 筛除今天的数据
                .groupBy { it.category }
            val transformedMap = groupedMap.mapValues { entry ->
                val averageMillis = entry.value.map { it.totalDuration.toMillis() }.average()
                Duration.ofMillis(averageMillis.toLong())
            }
            statisticsByCategory.putAll(groupedMap)
            averageDurationByCategory.putAll(transformedMap)
            loadButtonShow.value = false // 数据加载完后隐藏按钮
        }
    }
}