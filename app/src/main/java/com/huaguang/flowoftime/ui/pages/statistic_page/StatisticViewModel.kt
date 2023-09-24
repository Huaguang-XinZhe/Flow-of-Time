package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    val repository: DailyStatisticsRepository,
    private val eventRepository: EventRepository,
    val sharedState: SharedState,
) : ViewModel() {

    suspend fun getCombinedEventsByDateCategory(date: LocalDate, category: String) =
        eventRepository.getCombinedEventsByDateCategory(date, category)

    suspend fun getDailyStatisticsByDate(date: LocalDate) =
        repository.getDailyStatisticsByDate(date)

    suspend fun deleteEntryByEmptyDuration() {
        repository.deleteEntryByEmptyDuration()
    }

}