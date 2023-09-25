package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticViewModel @Inject constructor(
    val repository: DailyStatisticsRepository,
    private val eventRepository: EventRepository,
    val sharedState: SharedState,
) : ViewModel() {

    private val yesterday: LocalDate = getAdjustedEventDate().minusDays(1)

    private val _combinedEvents = MutableStateFlow<List<CombinedEvent>>(listOf())
    val combinedEvents: StateFlow<List<CombinedEvent>> = _combinedEvents

    val category = mutableStateOf("")

    private val _date = MutableStateFlow(yesterday)
    val date: StateFlow<LocalDate> = _date

    private val _sumDuration = MutableStateFlow(Duration.ZERO)
    val sumDuration: StateFlow<Duration> = _sumDuration

    private val _data = MutableStateFlow(listOf<Pair<String?, Float>>())
    val data: StateFlow<List<Pair<String?, Float>>> = _data

    private val _referenceValue = MutableStateFlow(0f)
    val referenceValue: StateFlow<Float> = _referenceValue

    private val _wakeUpTime = MutableStateFlow<LocalDateTime?>(null)
    val wakeUpTime: StateFlow<LocalDateTime?> = _wakeUpTime

    private val _sleepTime = MutableStateFlow<LocalDateTime?>(null)
    val sleepTime: StateFlow<LocalDateTime?> = _sleepTime

    private val _nextWakeUpTime = MutableStateFlow<LocalDateTime?>(null)
    val nextWakeUpTime: StateFlow<LocalDateTime?> = _nextWakeUpTime

    init {
        viewModelScope.launch {
            repository.deleteEntryByEmptyDuration() // 删除时长为空的条目
        }

        viewModelScope.launch {
            _date.flatMapLatest { selectedDate ->
                repository.getDailyStatisticsFlowByDate(selectedDate)
            }.collect { categoryData ->
                // 这里的代码每次 categoryData 改变时都会执行
                if (categoryData.isEmpty()) {
                    resetBarData()
                    return@collect
                }

                _data.value = categoryData
                    .filterNot { it.totalDuration == Duration.ZERO } // 时长为 0 的条目筛出，不展示
                    .map { it.category to it.totalDuration.toMinutes().toFloat() }
                _sumDuration.value = categoryData
                    .map { it.totalDuration }
                    .fold(Duration.ZERO) { acc, duration -> acc + duration }
                _referenceValue.value = data.value.first().second
            }
        }

        viewModelScope.launch {
            _date.flatMapLatest { selectedDate ->
                eventRepository.getKeyTimePointsByDate(selectedDate)
            }.collect { keyTimePoints ->
                if (keyTimePoints.wakeUpTime == null) { // 起床时间不可能为 null，如果为 null，那一定没有数据
//                    resetBarData() // 这里就不重置了，以免重置两次
                    return@collect
                }
                _wakeUpTime.value = keyTimePoints.wakeUpTime
                _sleepTime.value = keyTimePoints.sleepTime
                _nextWakeUpTime.value = keyTimePoints.nextWakeUpTime
            }
        }

    }

    suspend fun fetchCombinedEventsByDateCategory(date: LocalDate, category: String?) {
        val events = eventRepository.getCombinedEventsByDateCategory(date, category)
        RDALogger.info("events = $events")
        _combinedEvents.value = events
        this.category.value = category ?: "❓"
    }

    fun onDateSelected(selectedDate: LocalDate) { // 进入统计页时不会执行，只有选中日期才会
        _date.value = selectedDate
    }

    private fun resetBarData() {
        // 置空条形统计相关的重要状态值
        _sumDuration.value = Duration.ZERO
        _data.value = listOf()
        _referenceValue.value = 0f

        _wakeUpTime.value = null
        _sleepTime.value = null
        _nextWakeUpTime.value = null

        sharedState.toastMessage.value = "当日无数据"
    }

    fun deleteAllOfTheDay() {
        viewModelScope.launch {
            repository.deleteAllByDate(_date.value)
            eventRepository.deleteEventsByDate(_date.value)
        }
    }


}
