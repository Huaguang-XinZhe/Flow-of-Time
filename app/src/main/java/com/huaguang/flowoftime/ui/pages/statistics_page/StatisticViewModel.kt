package com.huaguang.flowoftime.ui.pages.statistics_page

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.averageDateInterval
import com.huaguang.flowoftime.utils.copyToClipboard
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedDate
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

    private val yesterday: LocalDate = getAdjustedDate().minusDays(1)

    val category = mutableStateOf<String?>("-1")
    val size = mutableIntStateOf(0)
    val categoryDurationMap = mutableStateMapOf<String?, Duration>()

    private val _date = MutableStateFlow(yesterday)
    val date: StateFlow<LocalDate> = _date

    private val _sumDuration = MutableStateFlow(Duration.ZERO)
    val sumDuration: StateFlow<Duration> = _sumDuration


    private val _wakeUpTime = MutableStateFlow<LocalDateTime?>(null)
    val wakeUpTime: StateFlow<LocalDateTime?> = _wakeUpTime

    private val _sleepTime = MutableStateFlow<LocalDateTime?>(null)
    val sleepTime: StateFlow<LocalDateTime?> = _sleepTime

    private val _nextWakeUpTime = MutableStateFlow<LocalDateTime?>(null)
    val nextWakeUpTime: StateFlow<LocalDateTime?> = _nextWakeUpTime

    val displayXxxText = mutableStateOf("")
    val map = mutableStateMapOf<String?, List<Pair<String?, Float>>>()

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

                // 类属分组，还要用上现有的组件，就得构建出这样的数据结构：
                // Map<String, List<Pair<String?, Float>>>
                // 分组应该用 groupBy，而不是 associateBy，那样不会指向列表，而会指向元素
                val _map = categoryData
                    .filterNot { it.totalDuration == Duration.ZERO } // 时长为 0 的条目筛出，不展示
                    .map { it.category to it.totalDuration.toMinutes().toFloat() }
                    .groupBy { pair ->
                        sharedState.classify3(pair.first ?: "")
                    }
                map.putAll(_map)
                _sumDuration.value = categoryData
                    .map { it.totalDuration }
                    .fold(Duration.ZERO) { acc, duration -> acc + duration }
                categoryDurationMap.putAll(
                    categoryData.associate { it.category to it.totalDuration }
                )
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

    fun onDateSelected(selectedDate: LocalDate) { // 进入统计页时不会执行，只有选中日期才会
        _date.value = selectedDate
        category.value = "-1" // 重置 category，以防止进入新日期后点击同样的类属，不触发收集、状态更新
    }

    private fun resetBarData() {
        // 置空条形统计相关的重要状态值
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

    fun getCombinedEventsFlow(date: LocalDate, category: String?) =
        eventRepository.getCombinedEventsByDateCategoryFlow(date, category)



    fun onDateDurationItemClick() {
        viewModelScope.launch {

        }
    }

    // 以下是统计页的“XXX”功能
    fun getXXXData() {
        viewModelScope.launch {
            val xxxEvents = eventRepository.getXXXEvents()
            // 计算平均间隔
            val dates = xxxEvents.map { event ->
                event.eventDate!!.toString()
            }
            val averageInterval = averageDateInterval(dates)
            // 计算平均时长
            val averageDuration = xxxEvents.map { it.duration!!.toMillis() }.average()
            // 组装每条要显示的记录
            val xxxRecords = xxxEvents.map { event ->
                "${event.eventDate} ${event.name} ${formatDurationInText(event.duration!!)}"
            }
            // 组装最终要显示的文本
            displayXxxText.value = "平均间隔 $averageInterval 天\n" +
                    "平均每次耗时 ${formatDurationInText(Duration.ofMillis(averageDuration.toLong()))}\n" +
                    xxxRecords.joinToString("\n")

        }
    }

    fun copyXXXData(context: Context) {
        copyToClipboard(context, displayXxxText.value)
    }


}
