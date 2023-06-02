package com.huaguang.flowoftime.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class EventsViewModel(
    private val repository: EventRepository,
    private val spHelper: SPHelper,
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

    private val isTracking = mutableStateOf(false)
    val currentEventState: MutableState<Event?> =  mutableStateOf(null)
    private var incompleteMainEvent: Event? by mutableStateOf(null)
    private var beModifiedEvent: Event? by mutableStateOf(null)

    val isInputShowState = mutableStateOf(false)
    val newEventName = mutableStateOf("")

    // 底部按钮相关——————————————————————————————————👇
    val mainEventButtonText = mutableStateOf("开始")
    val subEventButtonText = mutableStateOf("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)
    private var subButtonClickCount = 0
    private var isLastStopFromSub = false
    // 底部按钮相关——————————————————————————————————👆

    val scrollIndex = mutableStateOf(0)
    var eventCount = 0

    private val alarmHelper = AlarmHelper(application)
    val isAlarmSet = MutableLiveData(false)

    @SuppressLint("MutableCollectionMutableState")
    val selectedEventIdsMap = mutableStateOf(mutableMapOf<Long, Boolean>())
    val isEventNameNotClicked = derivedStateOf {
        currentEventState.value?.let { selectedEventIdsMap.value[it.id] == null } ?: true
    }

//    val pager = Pager(
//        PagingConfig(pageSize = 25)
//    ) { eventDao.getAllEvents() }.flow

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
    val initialized = mutableStateOf(false)

    init {
        viewModelScope.launch {
            retrieveStateFromSP() // 恢复相关状态

            restoreButtonShow()

            initialized.value = true
        }

        // 目前主要是重置 remainingDuration
        resetStateIfNewDay()
    }



    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态
    }

    fun updateTimeAndState(updatedEvent: Event, lastDelta: Duration?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds
            withContext(Dispatchers.IO) {
                eventDao.updateEvent(updatedEvent)
            }
            Toast.makeText(getApplication(), "调整已更新到数据库", Toast.LENGTH_SHORT).show()

            isStartOrEndTimeClicked.value = false // 取消滑块阴影，禁止点击

            if (names.contains(updatedEvent.name)) {
                remainingDuration.value = remainingDuration.value?.minus(lastDelta)
//                remainingDuration.value?.let { spHelper.saveRemainingDuration(it) }
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



    fun onNameTextClicked(event: Event) {
        if (event.name == "起床") { // 起床项的名称禁止更改
            Toast.makeText(getApplication(), "起床项名称禁止修改！", Toast.LENGTH_SHORT).show()
            return
        }

        isInputShowState.value = true
        newEventName.value = event.name
        // 点击的事项条目的状态会被设为 true
        toggleSelectedId(event.id)
        beModifiedEvent = event
    }

    private fun toggleSelectedId(eventId: Long) {
        val map = selectedEventIdsMap.value.toMutableMap() // 调用这个方法能创建一个新实例！！！
        map[eventId] = !(map[eventId] ?: false)
        selectedEventIdsMap.value = map
    }

    fun undoTiming() {
        resetState()
    }

    private fun getUpHandle() {
        viewModelScope.launch {
            if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null
                Log.i("打标签喽", "起床处理，item 点击！！！")
                beModifiedEvent!!.name = "起床"

                withContext(Dispatchers.IO) {
                    eventDao.updateEvent(beModifiedEvent!!)
                }

                delayReset()
            } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
                Log.i("打标签喽", "起床处理，一般流程")
                currentEventState.value?.let { it.name = "起床" }

                withContext(Dispatchers.IO) {
                    currentEventState.value?.let { eventDao.insertEvent(it) }
                }

                // TODO: 这里似乎不需要 isEventNameClicked，是否可以优化呢？
                // 按钮文本直接还原为开始，不需要结束
                mainEventButtonText.value = "开始"
                // 比较特殊，插入按钮不需要显示
                subButtonShow.value = false
                currentEventState.value = null
            }

        }
    }

    private fun generalHandle() {
        if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null（事件可能在进行中）
            viewModelScope.launch {
                beModifiedEvent!!.name = newEventName.value

                withContext(Dispatchers.IO) {
                    eventDao.updateEvent(beModifiedEvent!!)
                }

                // 延迟一下，让边框再飞一会儿
                delayReset()
            }

        } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
            Log.i("打标签喽", "事件输入部分，点击确定，一般流程分支。")
            currentEventState.value?.let {
                currentEventState.value = it.copy(name = newEventName.value)
            }
//                checkAndSetAlarm(newEventName.value)
        }
    }

    private suspend fun delayReset() {
        Log.i("打标签喽", "延迟结束，子弹该停停了！")
        delay(500)
        beModifiedEvent = null
        selectedEventIdsMap.value = mutableMapOf()
    }

    private fun resetState() {
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
        viewModelScope.launch(Dispatchers.IO) {
            eventDao.deleteEvent(event.id)
            for (subEvent in subEvents) {
                eventDao.deleteEvent(subEvent.id)
            }
        }

        val isDeleteCurrentItem = currentEventState.value?.let { event.id == it.id } ?: false
        if (isTracking.value && isDeleteCurrentItem) resetState()
    }

    // 恢复和存储 UI 状态————————————————————————————————————————————————————————————————————————————👇

    private suspend fun retrieveStateFromSP() {
        val data = withContext(Dispatchers.IO) {
            spHelper.getAllData()
        }

        // 在主线程中使用取出的数据更新状态
        isOneDayButtonClicked.value = data.isOneDayButtonClicked
        isInputShowState.value = data.isInputShow
        mainEventButtonText.value = data.buttonText
        subEventButtonText.value = data.subButtonText
        remainingDuration.value = data.remainingDuration
        isTracking.value = data.isTracking
        currentEventState.value = data.currentEvent
        incompleteMainEvent = data.incompleteMainEvent
        subButtonClickCount = data.subButtonClickCount
        eventTypeState.value = if (data.isSubEventType) EventType.SUB else EventType.MAIN
        isLastStopFromSub = data.isLastStopFromSub

        if (data.scrollIndex != -1) {
            scrollIndex.value = data.scrollIndex
            eventCount = data.scrollIndex + 1
        }
    }

    fun saveState() {
        viewModelScope.launch(Dispatchers.IO) {
            spHelper.saveState(
                isOneDayButtonClicked.value,
                isInputShowState.value,
                mainEventButtonText.value,
                subEventButtonText.value,
                scrollIndex.value,
                isTracking.value,
                remainingDuration.value,
                currentEventState.value,
                incompleteMainEvent,
                subButtonClickCount,
                eventTypeState.value,
                isLastStopFromSub
            )
        }
    }

    // 恢复和存储 UI 状态————————————————————————————————————————————————————————————————————————————👆

    // 关键 UI 逻辑——————————————————————————————————————————————————————————————————————————————————👇

    private fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
        // 重要状态更新—————————————————————————————————————————————————————
        isTracking.value = true
        isInputShowState.value = true

        viewModelScope.launch {
            if (eventTypeState.value == EventType.SUB) {
                subButtonClickCount++

                if (subButtonClickCount == 1) { // 首次点击插入按钮
                    // 存储每个未完成的主事件，以备后边插入的子事件结束后获取
                    incompleteMainEvent = currentEventState.value

                    withContext(Dispatchers.IO) {
                        currentEventState.value?.let { eventDao.insertEvent(it) }
                    }
                }
            } else {
                subButtonClickCount = 0 // 一遇到主事件就清空
            }

            // 获取 parentId，并创建新的事件对象（主、子），更新 currentEvent——————————————————————
            val mainEventId = withContext(Dispatchers.IO) {
                if (eventTypeState.value == EventType.SUB) {
                    eventDao.getLastMainEventId()// 在插入子事件之前一定存在主事件，不会有问题
                } else null
            }

            currentEventState.value = Event(
                name = newEventName.value,
                startTime = startTime,
                eventDate = getEventDate(startTime),
                parentId = mainEventId
            )

            // 索引相关—————————————————————————————————————————————————————————
            // 更新事件数量
            eventCount++
            // 更新滚动索引
            scrollIndex.value = eventCount - 1
        }

    }

    fun onConfirm() {

        when (newEventName.value) {
            "" -> {
                Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
                return
            }
            "起床" -> {
                // 起床事件的特殊应对
                getUpHandle()
            }
            else -> {
                Log.i("打标签喽", "一般情况执行！！！")
                // 一般情况
                generalHandle()
            }
        }

        // 通用状态重置——————————————————————
        newEventName.value = ""
        isInputShowState.value = false

    }

    private fun stopCurrentEvent() {
        if (eventTypeState.value == EventType.MAIN) {
            isTracking.value = false
        }

        viewModelScope.launch {
            currentEventState.value?.let {
                // 如果是主事件，就计算从数据库中获取子事件列表，并计算其间隔总和
                val subEventsDuration = if (it.parentId == null) {
                    repository.calculateSubEventsDuration(it.id)
                } else Duration.ZERO

                // 这里就不赋给 currentEventState 的值了，减少不必要的重组
                it.endTime = LocalDateTime.now()
                it.duration = Duration.between(it.startTime, it.endTime).minus(subEventsDuration)

                withContext(Dispatchers.IO) {
                    if (isLastStopFromSub && eventTypeState.value == EventType.MAIN) {
                        Log.i("打标签喽", "结束：更新主事件到数据库！")
                        eventDao.updateEvent(it)
                    } else {
                        Log.i("打标签喽", "结束：插入到数据库执行！")
                        eventDao.insertEvent(it)
                    }
                }

                cancelAlarm()

                // 子事件结束后恢复到主事件（数据库插入会重组一次，因此这里无需赋值重组）
                if (eventTypeState.value == EventType.SUB) {
                    Log.i("打标签喽", "结束的是子事件")
                    it.id = it.parentId!!
                    it.startTime = incompleteMainEvent!!.startTime
                    it.name = incompleteMainEvent!!.name
                    it.endTime = LocalDateTime.MIN // 为优化显示，实际业务不需要
                    it.parentId = null

                    isLastStopFromSub = true
                    eventTypeState.value = EventType.MAIN // 必须放在 stop 逻辑中
                } else {
                    Log.i("打标签喽", "结束主事件，改变名称！不显示！")
                    // 结束后的特殊设置，为减少重组和优化显示
                    it.name = "&currentEvent不显示&"

                    isLastStopFromSub = false
                }

                Log.i("打标签喽", "currentEventState.value = $it")
            }
        }

    }

    // 关键 UI 逻辑——————————————————————————————————————————————————————————————————————————————————👆

    // RemainingDuration 和闹钟相关—————————————————————————————————————————————————————————————————👇

    private fun resetStateIfNewDay() {
       viewModelScope.launch {
           val events = eventsWithSubEvents.first()
           if (events.isEmpty()) {
               Log.i("打标签喽", "remainingDuration 置空执行了。")
               remainingDuration.value = null
           }
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

    private fun cancelAlarm() {
        currentEventState.value?.let {
            if (remainingDuration.value != null && names.contains(it.name)) {
                remainingDuration.value = remainingDuration.value?.minus(it.duration)

                if (isAlarmSet.value == true &&
                    remainingDuration.value!! > ALARM_CANCELLATION_THRESHOLD) {
                    alarmHelper.cancelAlarm()
                    isAlarmSet.value = false
                }
            }
        }
    }

    // RemainingDuration 和闹钟相关—————————————————————————————————————————————————————————————————👆


    // 底部按钮相关————————————————————————————————————————————————————————————————————————————👇

    fun onMainButtonLongClick() {
        if (mainEventButtonText.value == "结束") return

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val lastEvent = withContext(Dispatchers.IO) {
                eventDao.getLastEvent() // 这个数据库操作是必需的
            }
            val startTime = lastEvent.endTime?.plus(DEFAULT_EVENT_INTERVAL)

            if (startTime != null) {
                startNewEvent(startTime = startTime)
                toggleMainButtonState("开始")
            }
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    private fun restoreButtonShow() {
        if (mainEventButtonText.value == "结束") {
            if (subEventButtonText.value == "插入结束") {
                Log.i("打标签喽", "插入结束部分恢复！")
                subButtonShow.value = true
                mainButtonShow.value = false
            } else {
                subButtonShow.value = true
            }
        }

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
                toggleSubButtonState("插入") // 这个必须放在前边，否则 start 逻辑会出问题
                startNewEvent()
            }
            "插入结束" -> {
                stopCurrentEvent()
                toggleSubButtonState("插入结束")
            }
        }
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
    }

    private fun toggleSubButtonState(buttonText: String) {
        when (buttonText) {
            "插入" -> {
                eventTypeState.value = EventType.SUB
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                // 不能放在这里，stop 里边的协程会挂起，这一段会先执行，必须放入 stop 里边
//                eventTypeState.value = EventType.MAIN
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
            }
        }
    }

    // 底部按钮相关————————————————————————————————————————————————————————————————————————————👆

}
