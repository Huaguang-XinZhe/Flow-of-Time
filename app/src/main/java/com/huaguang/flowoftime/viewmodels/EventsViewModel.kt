package com.huaguang.flowoftime.viewmodels

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
    val isOneDayButtonClicked = MutableStateFlow(spHelper.getIsOneDayButtonClicked())

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithSubEvents = isOneDayButtonClicked.flatMapLatest { clicked ->
        if (clicked) {
            repository.getCustomTodayEvents()
        } else {
            repository.getRecentTwoDaysEvents()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    val isTracking = MutableLiveData(spHelper.getIsTracking())
    private var currentEvent: Event? = null
    val mainEventButtonText = MutableLiveData(spHelper.getButtonText())
    val subEventButtonText = MutableLiveData(spHelper.getSubButtonText())
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)
    val newEventName = MutableLiveData("")
    val scrollIndex = MutableLiveData<Int>()
    var eventCount = 0
    private val alarmHelper = AlarmHelper(application)
    val isAlarmSet = MutableLiveData(false)
    val remainingDuration = MutableStateFlow(spHelper.getRemainingDuration())
    val isImportExportEnabled = MutableLiveData(true)
    private var updateJob: Job? = null
    val selectedEventIdsMap = MutableLiveData<MutableMap<Long, Boolean>>(mutableMapOf())
    val isStartOrEndTimeClicked = mutableStateOf(false)


    private val isCurrentItemNotClicked: Boolean
        get() = currentEvent?.let { selectedEventIdsMap.value!![it.id] == null } ?: true

    val rate: StateFlow<Float?> get() = remainingDuration.map { remainingDuration ->
        remainingDuration?.let {
            val remainingRate = it.toMillis().toFloat() / FOCUS_EVENT_DURATION_THRESHOLD.toMillis()
            1 - remainingRate
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {

        // 从SharedPreferences中恢复滚动索引
        val savedScrollIndex = spHelper.getScrollIndex()
        if (savedScrollIndex != -1) {
            scrollIndex.value = savedScrollIndex
            eventCount = savedScrollIndex + 1
        }
        // 目前主要是重置 remainingDuration
        resetStateIfNewDay()

        if (subEventButtonText.value == "插入结束") {
            subButtonShow.value = true
            mainButtonShow.value = false
        } else if (mainEventButtonText.value == "结束") {
            subButtonShow.value = true
        }
    }

    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态
        spHelper.saveIsOneDayButtonClicked(isOneDayButtonClicked.value)
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

    fun toggleMainEvent() {
        when (mainEventButtonText.value) {
            "开始" -> {
                startNewEvent()
                toggleMainButtonState("开始")
            }
            "结束" -> {
                stopCurrentEvent()
                toggleMainButtonState("结束")
            }
        }
    }

    private fun toggleSubButtonState(buttonText: String) {
        when (buttonText) {
            "插入" -> {
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
            }
        }
        spHelper.saveButtonText(subEventButtonText.value!!)
    }

    fun toggleSubEvent() {
        when (subEventButtonText.value) {
            "插入" -> {
                startNewEvent(EventType.SUB)
                toggleSubButtonState("插入")
            }
            "插入结束" -> {
                stopCurrentEvent(EventType.SUB)
                toggleSubButtonState("插入结束")
            }
        }
    }

    fun onConfirm() {
        if (newEventName.value == "") {
            Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
            return
        }

        updateEventName()

        Log.i("打标签喽", "isCurrentItemNotClicked = $isCurrentItemNotClicked")
        if (newEventName.value == "起床" && isCurrentItemNotClicked) {
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

        isTracking.value = false
    }

    private suspend fun handleConfirmProcess() {
        setRemainingDuration()

        // 当前事项条目的名称部分没被点击，没有对应的状态（为 null），反之，点过了的话，对应的状态就为 true
        if (isCurrentItemNotClicked) {
            Log.i("打标签喽", "事件输入部分，点击确定，一般流程分支。")
            checkAndSetAlarm(newEventName.value!!)
        } else {
            // 点击修改事项名称进行的分支
            // 延迟一下，让边框再飞一会儿
            delay(800)
            Log.i("打标签喽", "延迟结束，子弹该停停了！")
            selectedEventIdsMap.value = mutableMapOf()
            currentEvent = null
        }
    }


    private fun updateEventName() {
        viewModelScope.launch {
            currentEvent = if (currentEvent == null) {
                eventDao.getLastEvent()
            } else currentEvent

            currentEvent!!.let {
                it.name = newEventName.value!!
                Log.i("打标签喽", "updateEventName 块内：newEventName.value = ${newEventName.value}")
                eventDao.updateEvent(it)
            }
        }
    }

    private fun updateEventEndTimeAndDuration() {
        currentEvent?.let {
            it.endTime = it.startTime
            it.duration = Duration.ZERO
            viewModelScope.launch {
                eventDao.updateEvent(it)
            }
        }
    }

    private fun startNewEvent(
        type: EventType = EventType.MAIN,
        startTime: LocalDateTime = LocalDateTime.now()
    ) {
        val newEvent = Event(
            name = newEventName.value!!,
            startTime = startTime,
            eventDate = getEventDate(startTime)
        )

        viewModelScope.launch {
            val mainEventId = if (type == EventType.SUB) {
                eventDao.getLastMainEventId()
            } else null
            // 必须放在后边，要不然获取 mainEventId 会出错
            val eventId = eventDao.insertEvent(newEvent)

            currentEvent = newEvent.copy(id = eventId, parentId = mainEventId)
            isTracking.value = true
            // 更新事件数量
            eventCount++
            // 更新滚动索引
            scrollIndex.value = eventCount - 1
            // 保存滚动索引到SharedPreferences
            spHelper.saveScrollIndex(eventCount - 1)
        }
    }

    private fun stopCurrentEvent(type: EventType = EventType.MAIN) {
        viewModelScope.launch {
            if (currentEvent == null) {
                Log.i("打标签喽", "停止事件记录，currentEvent 为 null，从数据库获取最新的事件。")
                currentEvent = eventDao.getLastIncompleteEvent()
            }

            currentEvent?.let {
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
                    spHelper.saveRemainingDuration(remainingDuration.value!!)

                    if (isAlarmSet.value == true &&
                        remainingDuration.value!! > ALARM_CANCELLATION_THRESHOLD) {
                        alarmHelper.cancelAlarm()
                        isAlarmSet.value = false
                    }
                }
            }

            Log.i("打标签喽", "currentEvent = $currentEvent")

            currentEvent = if (type == EventType.SUB) {
                currentEvent!!.parentId?.let { eventDao.getEvent(it) }
            } else null
        }

    }

    private suspend fun setRemainingDuration() {
        remainingDuration.value = if (remainingDuration.value == null) {
            Log.i("打标签喽", "setRemainingDuration 块内：currentEvent = $currentEvent")
            // 数据库操作，查询并计算
            val totalDuration = repository.calEventDateDuration(
                currentEvent?.eventDate ?: LocalDate.now()
            )
            FOCUS_EVENT_DURATION_THRESHOLD.minus(totalDuration)
        } else remainingDuration.value
    }

    private fun checkAndSetAlarm(name: String) {
        Log.i("打标签喽", "checkAndSetAlarm 执行！！！")
        if (!names.contains(name)) return

        if (remainingDuration.value!! < ALARM_SETTING_THRESHOLD) {
            // 一般事务一次性持续时间都不超过 5 小时
            alarmHelper.setAlarm(remainingDuration.value!!.toMillis())
            isAlarmSet.value = true
        }
    }

    fun onNameTextClicked(event: Event) {
        isTracking.value = true
        newEventName.value = event.name
        currentEvent = event
        // 点击的事项条目的状态会被设为 true
        toggleSelectedId(event.id)
    }

    private fun toggleSelectedId(eventId: Long) {
        val map = selectedEventIdsMap.value!!
        map[eventId] = !(map[eventId] ?: false)
        selectedEventIdsMap.value = map
    }

    fun onMainButtonLongClick() {
        if (mainEventButtonText.value == "结束") return

        Log.i("打标签喽", "长按执行了！")
        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val lastEvent = eventDao.getLastEvent()
            val startTime = lastEvent.endTime?.plus(DEFAULT_EVENT_INTERVAL)

            if (startTime != null) {
                startNewEvent(startTime = startTime)
                toggleMainButtonState("开始")
            }
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    private fun toggleMainButtonState(buttonText: String) {
        when (buttonText) {
            "开始" -> {
                mainEventButtonText.value = "结束"
                subButtonShow.value = true
                isImportExportEnabled.value = false
            }
            "结束" -> {
                mainEventButtonText.value = "开始"
                subButtonShow.value = false
                isImportExportEnabled.value = true
            }
        }
        spHelper.saveButtonText(mainEventButtonText.value!!)
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
        isTracking.value = false
        newEventName.value = ""
        // 切换到开始状态
        if (subEventButtonText.value == "插入结束") {
            // 撤销的是子事件
            toggleSubButtonState("插入结束")
        } else {
            // 撤销的是主事件
            toggleMainButtonState("结束")
        }
        viewModelScope.launch {
            currentEvent?.let { eventDao.deleteEvent(it.id) }
            currentEvent = null // 方便快捷的方法，让停止事件之前总是从数据库获取当前未完成的事件，以避免 id 问题。
        }
    }

    fun deleteItem(event: Event, subEvents: List<Event>) {
        viewModelScope.launch {
            eventDao.deleteEvent(event.id)
            for (subEvent in subEvents) {
                eventDao.deleteEvent(subEvent.id)
            }
        }
    }


}
