package com.huaguang.flowoftime.ui.screens.event_tracker

import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.ui.components.current_item.CurrentItemViewModel
import com.huaguang.flowoftime.ui.components.duration_slider.DurationSliderViewModel
import com.huaguang.flowoftime.ui.components.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.components.event_list.EventListViewModel
import com.huaguang.flowoftime.ui.components.event_name.EventNameViewModel
import com.huaguang.flowoftime.ui.components.event_time.EventTimeViewModel
import com.huaguang.flowoftime.ui.components.header.HeaderViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventTrackerScreenViewModel @Inject constructor(
    val headerViewModel: HeaderViewModel,
    val durationSliderViewModel: DurationSliderViewModel,
    val eventListViewModel: EventListViewModel,
    val eventInputViewModel: EventInputViewModel,
    val eventButtonsViewModel: EventButtonsViewModel,
    val eventTimeViewModel: EventTimeViewModel,
    val currentItemViewModel: CurrentItemViewModel,
    val eventNameViewModel: EventNameViewModel,
    val repository: EventRepository,
    val spHelper: SPHelper,
    val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {



    var updateJob: Job? = null
    val initialized = mutableStateOf(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithSubEvents = headerViewModel.isOneDayButtonClicked.flatMapLatest { clicked ->
        if (clicked) {
            repository.getCustomTodayEvents()
        } else {
            repository.getRecentTwoDaysEvents()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    init {
        viewModelScope.launch {
            retrieveStateFromSP() // 恢复相关状态

            eventButtonsViewModel.restoreButtonShow()

            initialized.value = true
        }

        if (!durationSliderViewModel.isCoreDurationReset) {
            durationSliderViewModel.coreDuration.value = Duration.ZERO
            durationSliderViewModel.isCoreDurationReset = true
        }

        durationSliderViewModel.updateCoreDuration()
    }


    fun updateOnDragStopped(updatedEvent: Event, originalDuration: Duration?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds

            eventTimeViewModel.apply {
                repository.updateEvent(updatedEvent)
                unBorder(updatedEvent.id)
            }

            durationSliderViewModel.updateCoreDurationOnDragStopped(
                updatedEvent,
                originalDuration,
                currentItemViewModel.currentEvent.value!!.startTime
            )

            currentItemViewModel.updateSTonDragStopped(updatedEvent)

            Toast.makeText(getApplication(), "调整已更新！", Toast.LENGTH_SHORT).show()
        }
    }

    fun generalHandleFromNameClicked() {
        viewModelScope.launch {
            eventNameViewModel.apply {
                updateNameChangedToDB()

                durationSliderViewModel.updateCDonNameChangeConfirmed(
                    name = newEventName.value,
                    event = beModifiedEvent!!,
                    clickFlag = coreNameClickFlag
                )

                // 延迟一下，让边框再飞一会儿
                delayReset()
            }

        }
    }

    fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            currentItemViewModel.apply {
                saveInCompleteMainEvent()

                currentEvent.value = createCurrentEvent(startTime)
            }
        }

        sharedState.updateStateOnStart()
    }


    fun stopCurrentEvent() {
        viewModelScope.launch {
            updateOrInsertCurrentEventToDB()

            updateCoreDurationOnStop()

            //                cancelAlarm()

            resetCurrentEventAndState()

        }

    }

    fun resetState(isCoreEvent: Boolean = false, fromDelete: Boolean = false) {
        // 按钮和currentEvent状态++++++++++++++++++++++++++++++++++++++++
        if (eventType == EventType.SUB) {
            toggleSubButtonState("插入结束")
            restoreOnMainEvent(fromDelete)
        } else {
            if (isCoreEvent) {
                val duration = Duration.between(currentEvent.value!!.startTime, LocalDateTime.now())
                coreDuration.value -= duration
            }

            toggleMainButtonState("结束")

            resetCurrentOnMainBranch(fromDelete)
        }

        // 输入状态
        sharedState.resetInputState()
    }

    fun onMainButtonLongClick() {
        eventButtonsViewModel.apply {
            if (mainEventButtonText.value == "结束") return

            // ButtonText 的值除了结束就是开始了，不可能为 null
            viewModelScope.launch {
                val startTime = repository.getOffsetStartTime()
                startNewEvent(startTime = startTime)

                toggleMainButtonState("开始")
            }
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    fun onSubButtonLongClick() {
        viewModelScope.launch {
            // 结束子事件————————————————————————————————————————————————————
            updateOrInsertCurrentEventToDB()

            restoreOnMainEvent()

            toggleSubButtonState("插入结束")

            // 结束主事件——————————————————————————————————————————————————————————
            toggleMainButtonState("结束")

            stopCurrentEvent()

            Toast.makeText(getApplication(), "全部结束！", Toast.LENGTH_SHORT).show()

        }
    }



    fun toggleMainEvent() {
        eventButtonsViewModel.apply {
            when (mainEventButtonText.value) {
                "开始" -> {
                    toggleMainButtonState("开始")
                    startNewEvent()
                }
                "结束" -> {
                    toggleMainButtonState("结束")
                    stopCurrentEvent()
                }
            }
        }
    }

    fun toggleSubEvent() {
        eventButtonsViewModel.apply {
            when (subEventButtonText.value) {
                "插入" -> {
                    toggleSubButtonState("插入") // 这个必须放在前边，否则 start 逻辑会出问题
                    startNewEvent()
                }
                "插入结束" -> {
                    stopCurrentEvent()
                    toggleSubButtonState("插入结束")
                }
            }
        }
    }


    private suspend fun retrieveStateFromSP() {
        val data = withContext(Dispatchers.IO) {
            spHelper.getAllData()
        }

        // 在主线程中使用取出的数据更新状态
        headerViewModel.isOneDayButtonClicked.value = data.isOneDayButtonClicked
        eventInputViewModel.isInputShow.value = data.isInputShow
        eventButtonsViewModel.mainEventButtonText.value = data.buttonText
        eventButtonsViewModel.subEventButtonText.value = data.subButtonText
        durationSliderViewModel.coreDuration.value = data.coreDuration
        durationSliderViewModel.startTimeTracking = data.startTimeTracking
        eventListViewModel.currentEvent.value = data.currentEvent
        eventListViewModel.incompleteMainEvent = data.incompleteMainEvent
        eventButtonsViewModel.subButtonClickCount = data.subButtonClickCount
        eventListViewModel.eventType.value = if (data.isSubEventType) EventType.SUB else EventType.MAIN
        eventButtonsViewModel.isLastStopFromSub = data.isLastStopFromSub
        durationSliderViewModel.isCoreDurationReset = data.isCoreDurationReset

        if (data.scrollIndex != -1) {
            eventListViewModel.scrollIndex.value = data.scrollIndex
            eventListViewModel.eventCount = data.scrollIndex + 1
        }
    }

    fun saveState() {
        viewModelScope.launch(Dispatchers.IO) {
            spHelper.saveState(
                headerViewModel.isOneDayButtonClicked.value,
                eventInputViewModel.isInputShow.value,
                eventButtonsViewModel.mainEventButtonText.value,
                eventButtonsViewModel.subEventButtonText.value,
                eventListViewModel.scrollIndex.value,
                eventListViewModel.isTracking.value,
                eventListViewModel.isCoreEventTracking,
                durationSliderViewModel.coreDuration.value,
                durationSliderViewModel.startTimeTracking,
                eventListViewModel.currentEvent.value,
                eventListViewModel.incompleteMainEvent,
                eventButtonsViewModel.subButtonClickCount,
                eventListViewModel.eventType.value,
                eventButtonsViewModel.isLastStopFromSub,
                durationSliderViewModel.isCoreDurationReset
            )
        }
    }

}
