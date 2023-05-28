package com.huaguang.flowoftime.viewmodels

import android.util.Log
import android.widget.Toast
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
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.getEventDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class EventsViewModel(
    private val repository: EventRepository,
    val spHelper: SPHelper,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private val eventDao = repository.eventDao
    val eventsWithSubEvents = eventDao.getEventsWithSubEvents(getAdjustedEventDate())
    val isTracking = MutableLiveData(spHelper.getIsTracking())
    private var currentEvent: Event? = null
    val mainEventButtonText = MutableLiveData(spHelper.getButtonText())
    val subEventButtonText = MutableLiveData("插入")
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

        if (mainEventButtonText.value == "结束") {
            subButtonShow.value = true
        }
    }

    fun updateTimeToDB(event: Event) {
        Log.i("打标签喽", "拖拽后结束后更新数据到数据库。")
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds
            eventDao.updateEvent(event)
            Toast.makeText(getApplication(), "调整已更新到数据库", Toast.LENGTH_SHORT).show()
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
                mainEventButtonText.value = "结束"
                subButtonShow.value = true
                isImportExportEnabled.value = false
            }
            "结束" -> {
                stopCurrentEvent()
                mainEventButtonText.value = "开始"
                subButtonShow.value = false
                isImportExportEnabled.value = true
            }
        }
        spHelper.saveButtonText(mainEventButtonText.value!!)
    }



    fun toggleSubEvent() {
        when (subEventButtonText.value) {
            "插入" -> {
                startNewEvent(EventType.SUB)
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                stopCurrentEvent(EventType.SUB)
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
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

            Log.i("打标签喽", "updateEventName 块内：currentEvent = $currentEvent")

            currentEvent!!.let {
                it.name = newEventName.value!!
                Log.i("打标签喽", "updateEventName 块内：newEventName.value = ${newEventName.value}")
                eventDao.updateEvent(it)
            }
        }
    }

    private fun updateEventEndTimeAndDuration() {
        currentEvent?.let {
            it.endTime = LocalDateTime.MIN
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
                Log.i("打标签喽", "停止事件记录，currentEvent 为 0，从数据库获取最新的事件。")
                currentEvent = eventDao.getLastEvent()
            }

            Log.i("打标签喽", "currentEvent 获取后 = $currentEvent")

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

                // 只要包含，remainingDuration 就会得到设置，一定不为 null
                if (names.contains(it.name)) {
                    remainingDuration.value = remainingDuration.value?.minus(it.duration)
                    spHelper.saveRemainingDuration(remainingDuration.value!!)

                    if (isAlarmSet.value == true &&
                        remainingDuration.value!! > ALARM_CANCELLATION_THRESHOLD) {
                        alarmHelper.cancelAlarm()
                        isAlarmSet.value = false
                    }
                }
            }

            currentEvent = if (type == EventType.SUB) {
                eventDao.getEvent(currentEvent!!.parentId!!)
            } else null
        }

    }

    private suspend fun setRemainingDuration() {
        remainingDuration.value = if (remainingDuration.value == null) {
            // 数据库操作，查询并计算
            val totalDuration = repository.calculateTotalDuration()
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
                startEventWithStateUpdateAndStorage(startTime)
            }
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    private fun startEventWithStateUpdateAndStorage(startTime: LocalDateTime) {
        startNewEvent(startTime = startTime)
        mainEventButtonText.value = "结束"
        subButtonShow.value = true
        isImportExportEnabled.value = false
        spHelper.saveButtonText(mainEventButtonText.value!!)
    }


}
