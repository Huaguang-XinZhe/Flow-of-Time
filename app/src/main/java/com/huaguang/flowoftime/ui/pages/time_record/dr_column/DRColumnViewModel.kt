package com.huaguang.flowoftime.ui.pages.time_record.dr_column

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.InputState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DRColumnViewModel @Inject constructor(
    val repository: EventRepository,
    val inputState: InputState,

) : ViewModel() {
    private val _currentCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val currentCombinedEventFlow: StateFlow<CombinedEvent?> = _currentCombinedEventFlow.asStateFlow()
    private val _secondLatestCombinedEventFlow = MutableStateFlow<CombinedEvent?>(null)
    val secondLatestCombinedEventFlow: StateFlow<CombinedEvent?> = _secondLatestCombinedEventFlow.asStateFlow()


    init {
        viewModelScope.launch {
            // 去掉 filterNotNull，在数据库为空时将发射 null，否则不会发射值，也就不会通知 UI 变化
            repository.getCurrentCombinedEventFlow().collect { combinedEvent ->
//                RDALogger.info("currentCombinedEvent = $combinedEvent")
                _currentCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

        viewModelScope.launch {
            repository.getSecondLatestCombinedEventFlow().filterNotNull().collect { combinedEvent ->
                _secondLatestCombinedEventFlow.value = combinedEvent // 传给 UI
            }
        }

    }
}