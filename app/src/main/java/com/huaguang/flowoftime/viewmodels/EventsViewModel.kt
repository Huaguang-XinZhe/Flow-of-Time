package com.huaguang.flowoftime.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.huaguang.flowoftime.ALARM_CANCELLATION_THRESHOLD
import com.huaguang.flowoftime.ALARM_SETTING_THRESHOLD
import com.huaguang.flowoftime.DEFAULT_EVENT_INTERVAL
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.names
import com.huaguang.flowoftime.utils.AlarmHelper
import com.huaguang.flowoftime.utils.copyToClipboard
import com.huaguang.flowoftime.utils.getEventDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class EventsViewModel(
    private val repository: EventRepository,
    val spHelper: SPHelper,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private val eventDao = repository.eventDao

    val isOneDayButtonClicked = MutableStateFlow(false)
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithSubEvents = isOneDayButtonClicked.flatMapLatest { clicked ->
        if (clicked) {
            repository.getCustomTodayEvents()
        } else {
            repository.getRecentTwoDaysEvents()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    val isTracking = mutableStateOf(false)
    var currentEventState: MutableState<Event?> =  mutableStateOf(null)

    val isInputShowState = mutableStateOf(false)
    val newEventName = MutableLiveData("")

    // 底部按钮相关状态——————————————————————————————————👇
    val mainEventButtonText = MutableLiveData("开始")
    val subEventButtonText = MutableLiveData("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)
    // 底部按钮相关状态——————————————————————————————————👆

    val scrollIndex = MutableLiveData<Int>()
    var eventCount = 0

    private val alarmHelper = AlarmHelper(application)
    val isAlarmSet = MutableLiveData(false)

    @SuppressLint("MutableCollectionMutableState")
    val selectedEventIdsMap = mutableStateOf(mutableMapOf<Long, Boolean>())
    val isEventNameNotClicked = derivedStateOf {
        currentEventState.value?.let { selectedEventIdsMap.value[it.id] == null } ?: true
    }

    val pager = Pager(
        PagingConfig(pageSize = 25)
    ) { eventDao.getAllEvents() }.flow

    val remainingDuration = MutableStateFlow(Duration.ZERO)
    val rate: StateFlow<Float?> get() = remainingDuration.map { remainingDuration ->
        remainingDuration?.let {
            val remainingRate = it.toMillis().toFloat() / FOCUS_EVENT_DURATION_THRESHOLD.toMillis()
            1 - remainingRate
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isImportExportEnabled = MutableLiveData(true)
    private var updateJob: Job? = null
    val isStartOrEndTimeClicked = mutableStateOf(false)
    private val eventTypeState = mutableStateOf(EventType.MAIN)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            isOneDayButtonClicked.value = spHelper.getIsOneDayButtonClicked()
            isInputShowState.value = spHelper.getIsInputShow()
            mainEventButtonText.value = spHelper.getButtonText()
            remainingDuration.value = spHelper.getRemainingDuration()

            isTracking.value = spHelper.getIsTracking()
            if (isTracking.value) {
                currentEventState.value = spHelper.getCurrentEvent()
            }

            // 从SharedPreferences中恢复滚动索引
            val savedScrollIndex = spHelper.getScrollIndex()
            if (savedScrollIndex != -1) {
                scrollIndex.value = savedScrollIndex // TODO: 这里似乎存在着优化空间 
                eventCount = savedScrollIndex + 1
            }
        }

        // 目前主要是重置 remainingDuration
        resetStateIfNewDay()

//        if (subEventButtonText.value == "插入结束") {
//            subButtonShow.value = true
//            mainButtonShow.value = false
//        } else if (mainEventButtonText.value == "结束") {
//            subButtonShow.value = true
//        }
    }

    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态
    }

    fun updateTimeAndState(updatedEvent: Event, lastDelta: Duration?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds
            eventDao.updateEvent(updatedEvent)
            Toast.makeText(getApplication(), "调整已更新到数据库", Toast.LENGTH_SHORT).show()

            isStartOrEndTimeClicked.value = false // 取消滑块阴影，禁止点击

            if (names.contains(updatedEvent.name)) {
                remainingDuration.value = remainingDuration.value?.minus(lastDelta)
                remainingDuration.value?.let { spHelper.saveRemainingDuration(it) }
            }
        }
    }

    fun exportEvents() {
        if (isImportExportEnabled.value == true) {
            viewModelScope.launch {
                val exportText = repository.exportEvents()
                copyToClipboard(getApplication(), exportText)
                Toast.makeText(getApplication(), "导出数据已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importEvents(text: String) {
        Toast.makeText(getApplication(), "导入成功！", Toast.LENGTH_SHORT).show()
    }



    fun onConfirm() {
        if (newEventName.value == "") {
            Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
            return
        }

        updateEventName()

        if (newEventName.value == "起床" && isEventNameNotClicked.value) {
            // 按钮文本直接还原为开始，不需要结束
            mainEventButtonText.value = "开始"
            // 不需要显示结束时间和间隔
            updateEventEndTimeAndDuration()
        }

        viewModelScope.launch {
            handleConfirmProcess()
        }

        viewModelScope.launch {
            // 等一会儿再置空，让 updateEventName 中的数据库操作先执行完！
            delay(200)
            newEventName.value = ""
        }

        isInputShowState.value = false
        Log.i("打标签喽", "onConfirm：eventTypeState.value = ${eventTypeState.value}")
    }

    private suspend fun handleConfirmProcess() {
        setRemainingDuration()

        // 当前事项条目的名称部分没被点击，没有对应的状态（为 null），反之，点过了的话，对应的状态就为 true
        if (isEventNameNotClicked.value) {
            Log.i("打标签喽", "事件输入部分，点击确定，一般流程分支。")
            checkAndSetAlarm(newEventName.value!!)
        } else {
            // 点击修改事项名称进行的分支
            // 延迟一下，让边框再飞一会儿
            delay(800)
            Log.i("打标签喽", "延迟结束，子弹该停停了！")
            selectedEventIdsMap.value = mutableMapOf()
            currentEventState.value = null
        }
    }


    private fun updateEventName() {
        viewModelScope.launch {
            currentEventState.value = if (currentEventState.value == null) {
                eventDao.getLastEvent()
            } else currentEventState.value

            currentEventState.value!!.let {
                it.name = newEventName.value!!
                Log.i("打标签喽", "updateEventName 块内：newEventName.value = ${newEventName.value}")
                eventDao.updateEvent(it)
            }
        }
    }

    private fun updateEventEndTimeAndDuration() {
        currentEventState.value?.let {
            it.endTime = it.startTime
            it.duration = Duration.ZERO
            viewModelScope.launch {
                eventDao.updateEvent(it)
            }
        }
    }

    private fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
        // 1. 获取 parentId，并创建新的事件对象（主、子），更新 currentEvent——————————————————————
        viewModelScope.launch {
            val mainEventId = if (eventTypeState.value == EventType.SUB) {
                eventDao.getLastMainEventId()
            } else null

            currentEventState.value = Event(
                name = newEventName.value!!,
                startTime = startTime,
                eventDate = getEventDate(startTime),
                parentId = mainEventId
            )
        }

        // 2. 重要状态更新—————————————————————————————————————————————————————
        isTracking.value = true
        isInputShowState.value = true

        // 3. 索引相关—————————————————————————————————————————————————————————
        // 更新事件数量
        eventCount++
        // 更新滚动索引
        scrollIndex.value = eventCount - 1

    }

    private fun stopCurrentEvent() {
        if (eventTypeState.value == EventType.MAIN) {
            isTracking.value = false
        }

        viewModelScope.launch {
            if (currentEventState.value == null) {
                Log.i("打标签喽", "停止事件记录，currentEvent 为 null，从数据库获取最新的事件。")
                currentEventState.value = eventDao.getLastIncompleteEvent()
            }

            currentEventState.value?.let {
                // 如果是主事件，就计算从数据库中获取子事件列表，并计算其间隔总和
                val subEventsDuration = if (it.parentId == null) {
                    repository.calculateSubEventsDuration(it.id)
                } else Duration.ZERO

                it.endTime = LocalDateTime.now()
                it.duration = Duration.between(it.startTime, it.endTime).minus(subEventsDuration)

                viewModelScope.launch {
                    Log.i("打标签喽", "viewModelScope 块，更新到数据库执行了！！！")
                    eventDao.updateEvent(it)
                }

                if (remainingDuration.value != null && names.contains(it.name)) {
                    remainingDuration.value = remainingDuration.value?.minus(it.duration)

                    if (isAlarmSet.value == true &&
                        remainingDuration.value!! > ALARM_CANCELLATION_THRESHOLD) {
                        alarmHelper.cancelAlarm()
                        isAlarmSet.value = false
                    }
                }
            }

            currentEventState.value = if (eventTypeState.value == EventType.SUB) {
                currentEventState.value!!.parentId?.let { eventDao.getEvent(it) }
            } else null
        }

    }

    private suspend fun setRemainingDuration() {
        remainingDuration.value = if (remainingDuration.value == null) {
            Log.i("打标签喽", "setRemainingDuration 块内：currentEvent = $currentEventState")
            // 数据库操作，查询并计算
            val totalDuration = repository.calEventDateDuration(
                currentEventState.value?.eventDate ?: LocalDate.now()
            )
            FOCUS_EVENT_DURATION_THRESHOLD.minus(totalDuration)
        } else remainingDuration.value
    }

    private fun checkAndSetAlarm(name: String) {
        if (!names.contains(name)) return

        if (remainingDuration.value!! < ALARM_SETTING_THRESHOLD) {
            // 一般事务一次性持续时间都不超过 5 小时
            alarmHelper.setAlarm(remainingDuration.value!!.toMillis())
            isAlarmSet.value = true
        }
    }

    fun onNameTextClicked(event: Event) {
        isInputShowState.value = true
        newEventName.value = event.name
        currentEventState.value = event
        // 点击的事项条目的状态会被设为 true
        toggleSelectedId(event.id)
    }

    private fun toggleSelectedId(eventId: Long) {
        val map = selectedEventIdsMap.value.toMutableMap() // 调用这个方法能创建一个新实例！！！
        map[eventId] = !(map[eventId] ?: false)
        selectedEventIdsMap.value = map
    }



    private fun resetStateIfNewDay() {
       viewModelScope.launch {
           val events = eventsWithSubEvents.first()
           if (events.isEmpty()) {
               Log.i("打标签喽", "remainingDuration 置空执行了。")
               remainingDuration.value = null
           }
       }
    }

    fun undoTiming() {
        viewModelScope.launch {
            currentEventState.value?.let { eventDao.deleteEvent(it.id) }
            reset()
        }
    }

    private fun reset() {
        // 按钮状态++++++++++++++++++++++++++++++++++++++++
        if (eventTypeState.value == EventType.SUB) {
            toggleSubButtonState("插入结束")
        } else {
            toggleMainButtonState("结束")
        }

        // 输入状态+++++++++++++++++++++++++++++++++++++++++
        if (isInputShowState.value) {
            isInputShowState.value = false
            newEventName.value = ""
        }

        // 事件跟踪+++++++++++++++++++++++++++++++++++++++++
        isTracking.value = false
        currentEventState.value = null // 方便快捷的方法，让停止事件之前总是从数据库获取当前未完成的事件，以避免 id 问题。

    }

    fun deleteItem(event: Event, subEvents: List<Event>) {
        viewModelScope.launch {
            eventDao.deleteEvent(event.id)
            for (subEvent in subEvents) {
                eventDao.deleteEvent(subEvent.id)
            }
        }

        val isDeleteCurrentItem = currentEventState.value?.let { event.id == it.id } ?: false
        if (isTracking.value && isDeleteCurrentItem) reset()
    }


    // 底部按钮相关————————————————————————————————————————————————————————————👇

    fun onMainButtonLongClick() {
        if (mainEventButtonText.value == "结束") return

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val lastEvent = eventDao.getLastEvent() // 这个数据库操作是必需的
            val startTime = lastEvent.endTime?.plus(DEFAULT_EVENT_INTERVAL)

            if (startTime != null) {
                startNewEvent(startTime = startTime)
                toggleMainButtonState("开始")
            }
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    fun toggleMainEvent() {
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

    fun toggleSubEvent() {
        when (subEventButtonText.value) {
            "插入" -> {
                toggleSubButtonState("插入")
                startNewEvent()
            }
            "插入结束" -> {
                toggleSubButtonState("插入结束")
                stopCurrentEvent()
            }
        }
    }

    private fun toggleMainButtonState(buttonText: String) {
        when (buttonText) {
            "开始" -> {
                mainEventButtonText.value = "结束"
                subButtonShow.value = true
//                subEventButtonText.value = "插入" // TODO: 不知道这个设置是不是多此一举
                isImportExportEnabled.value = false
            }
            "结束" -> {
                mainEventButtonText.value = "开始"
                subButtonShow.value = false
//                subEventButtonText.value = "插入结束" // TODO: 不知道这个设置是不是多此一举
                isImportExportEnabled.value = true
            }
        }
    }

    private fun toggleSubButtonState(buttonText: String) {
        when (buttonText) {
            "插入" -> {
                eventTypeState.value = EventType.SUB
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                eventTypeState.value = EventType.MAIN
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
            }
        }
    }

    // 底部按钮相关————————————————————————————————————————————————————————————👆

}
