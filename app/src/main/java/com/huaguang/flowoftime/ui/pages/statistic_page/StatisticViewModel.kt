package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    val repository: DailyStatisticsRepository,
    private val eventRepository: EventRepository,
    val sharedState: SharedState,
) : ViewModel() {

    val yesterday: LocalDate = getAdjustedEventDate().minusDays(1)

    private val _combinedEvents = MutableStateFlow<List<CombinedEvent>>(listOf())
    val combinedEvents: StateFlow<List<CombinedEvent>> = _combinedEvents

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _date = MutableStateFlow(yesterday)
    val date: StateFlow<LocalDate> = _date

    private val _sumDuration = MutableStateFlow(Duration.ZERO)
    val sumDuration: StateFlow<Duration> = _sumDuration

    private val _data = MutableStateFlow(listOf<Pair<String, Float>>())
    val data: StateFlow<List<Pair<String, Float>>> = _data

    private val _referenceValue = MutableStateFlow(0f)
    val referenceValue: StateFlow<Float> = _referenceValue

    init {
        viewModelScope.launch {
            repository.deleteEntryByEmptyDuration()
            fetchDailyStatisticsByDate(yesterday)
        }
    }

    suspend fun fetchCombinedEventsByDateCategory(date: LocalDate, category: String) {
        val events = eventRepository.getCombinedEventsByDateCategory(date, category)
        _combinedEvents.value = events
        _category.value = category
    }

    suspend fun fetchDailyStatisticsByDate(date: LocalDate) {
        _date.value = date

        val dailyStatistics = repository.getDailyStatisticsByDate(date)
        if (dailyStatistics.isEmpty()) {
            sharedState.toastMessage.value = "当日无数据"
            // 置空条形统计相关的重要状态值
            _sumDuration.value = Duration.ZERO
            _data.value = listOf()
            _referenceValue.value = 0f
            return
        }

        _sumDuration.value = dailyStatistics
            .map { it.totalDuration }
            .fold(Duration.ZERO) { acc, duration -> acc + duration }
        _data.value = dailyStatistics.map {
            it.category to it.totalDuration.toMinutes().toFloat()
        }
        _referenceValue.value = data.value.first().second
    }


}
