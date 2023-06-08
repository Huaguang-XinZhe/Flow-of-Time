package com.huaguang.flowoftime.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.components.current_item.CurrentItemViewModel
import com.huaguang.flowoftime.ui.components.duration_slider.DurationSliderViewModel
import com.huaguang.flowoftime.ui.components.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.components.event_name.EventNameViewModel
import com.huaguang.flowoftime.ui.components.event_time.EventTimeViewModel
import com.huaguang.flowoftime.ui.components.header.HeaderViewModel
import com.huaguang.flowoftime.utils.isCoreEvent
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


class EventTrackerMediator @Inject constructor(
    val headerViewModel: HeaderViewModel,
    val durationSliderViewModel: DurationSliderViewModel,
    val eventButtonsViewModel: EventButtonsViewModel,
    val eventTimeViewModel: EventTimeViewModel,
    val currentItemViewModel: CurrentItemViewModel,
    val eventNameViewModel: EventNameViewModel,
    private val repository: EventRepository,
    private val spHelper: SPHelper,
    val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    // 依赖子 ViewModel 的状态
    private val beModifiedEvent = eventNameViewModel.beModifiedEvent
    private var currentEvent = currentItemViewModel.currentEvent.value

    // 辅助构成函数逻辑
    private var updateJob: Job? = null
    val initialized = mutableStateOf(false)
    private val dismissedItems = mutableSetOf<Long>() // 为了防止删除逻辑多次执行

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

    fun deleteItem(event: Event, subEvents: List<Event> = listOf()) {
        if (dismissedItems.contains(event.id)) return

        val isCoreEvent = isCoreEvent(event.name)

        if (event.id != 0L) { // 删除项已经存入数据库中了，排除已经插入了子事件的主事件（有点复杂，不处理这样的场景）
            Log.i("打标签喽", "删除已经入库的条目")
            viewModelScope.launch {
                repository.deleteEventWithSubEvents(event, subEvents)
            }

            durationSliderViewModel.reduceStoredDuration(event.duration!!)
        } else { // 删除的是当前项（正在计时）
            resetState(isCoreEvent, true)
        }

        // 在删除完成后，将该 id 添加到已删除项目的记录器中
        if (event.id != 0L) { // 当前项的 id 始终是 0，就不要加进来限制执行次数了。
            dismissedItems.add(event.id)
        }
    }

    fun onConfirmed() {
        sharedState.apply {
            when(newEventName.value) {
                "" -> {
                    Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
                    return
                }
                "起床" -> { // 起床事件的特殊应对
                    getUpHandle()
                }
                else -> {
                    Log.i("打标签喽", "一般情况执行！！！")
                    generalHandle()
                }
            }

            isInputShow.value = false
        }
    }



    /**
     * 这里边是两个分支，分为点击和没点击
     */
    private fun getUpHandle() {
        if (beModifiedEvent == null) { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
            Log.i("打标签喽", "起床处理，一般流程")
            viewModelScope.launch {
                currentEvent?.let {
                    it.name = "起床"

                    repository.insertEvent(it)
                }

                eventButtonsViewModel.updateStateOnGetUpConfirmed()

                currentEvent = null
            }
        }

        eventNameViewModel.onGetUpTextClickThenConfirmed()
    }



    private fun generalHandle() { // 确认时文本不为空也不是 ”起床“
        if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null（事件可能在进行中）
            generalHandleFromNameClicked()
        } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
            currentEvent?.let {
                durationSliderViewModel.generalHandleFromNotClicked(it)
                currentEvent = it.copy(name = sharedState.newEventName.value)
            }
        }
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

    private fun generalHandleFromNameClicked() {
        viewModelScope.launch {
            eventNameViewModel.apply {
                updateNameChangedToDB()

                durationSliderViewModel.updateCDonNameChangeConfirmed(
                    name = sharedState.newEventName.value,
                    event = beModifiedEvent!!,
                    clickFlag = coreNameClickFlag
                )

                // 延迟一下，让边框再飞一会儿
                delayReset()
            }

        }
    }

    private fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
        viewModelScope.launch {
            currentItemViewModel.apply {
                saveInCompleteMainEvent()

                currentEvent.value = createCurrentEvent(startTime)
            }
        }

        sharedState.updateStateOnStart()
    }


    private fun stopCurrentEvent() {
        viewModelScope.launch {
            currentItemViewModel.apply {
                updateCurrentEventOnStop()

                saveCurrentEvent()

//                cancelAlarm()

                resetCurrentEvent()

                durationSliderViewModel.increaseCDonCurrentStop(currentEvent.value!!)
            }

        }

    }

    fun resetState(isCoreEvent: Boolean = false, fromDelete: Boolean = false) {
        // 按钮状态
        eventButtonsViewModel.toggleButtonStateStopped()

        // CurrentItem 状态
        currentItemViewModel.apply {
            resetCurrentEvent(fromDelete)

            // 更新 CoreDuration
            durationSliderViewModel.reduceCDonCurrentCancel(
                currentEvent.value!!.startTime,
                isCoreEvent
            )
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
            currentItemViewModel.apply {
                saveCurrentEvent()

                resetCurrentItem()
            }

            // 切换按钮状态——————————————————————————————————————————————————
            eventButtonsViewModel.apply {
                toggleSubButtonState("插入结束")
                toggleMainButtonState("结束")
            }

            // 结束主事件——————————————————————————————————————————————————————————

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
        sharedState.apply { // 共享状态
            isInputShow.value = data.isInputShow
            eventType.value = if (data.isSubEventType) EventType.SUB else EventType.MAIN

            if (data.scrollIndex != -1) {
                scrollIndex.value = data.scrollIndex
                eventCount = data.scrollIndex + 1
            }
        }

        headerViewModel.isOneDayButtonClicked.value = data.isOneDayButtonClicked

        durationSliderViewModel.apply {
            coreDuration.value = data.coreDuration
            startTimeTracking = data.startTimeTracking
            isCoreDurationReset = data.isCoreDurationReset
        }

        currentItemViewModel.apply {
            currentEvent.value = data.currentEvent
            incompleteMainEvent = data.incompleteMainEvent
            subButtonClickCount = data.subButtonClickCount
            isLastStopFromSub = data.isLastStopFromSub
        }

        eventButtonsViewModel.apply {
            mainEventButtonText.value = data.buttonText
            subEventButtonText.value = data.subButtonText
        }

    }

    fun saveState() {
        viewModelScope.launch(Dispatchers.IO) {
            spHelper.saveState(
                headerViewModel.isOneDayButtonClicked.value,
                sharedState.isInputShow.value,
                eventButtonsViewModel.mainEventButtonText.value,
                eventButtonsViewModel.subEventButtonText.value,
                sharedState.scrollIndex.value,
                sharedState.isTracking.value,
                durationSliderViewModel.isCoreEventTracking,
                durationSliderViewModel.coreDuration.value,
                durationSliderViewModel.startTimeTracking,
                currentItemViewModel.currentEvent.value,
                currentItemViewModel.incompleteMainEvent,
                currentItemViewModel.subButtonClickCount,
                sharedState.eventType.value,
                currentItemViewModel.isLastStopFromSub,
                durationSliderViewModel.isCoreDurationReset
            )
        }
    }

}
