package com.huaguang.flowoftime.viewModel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.AlarmHelper
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.hourThreshold
import com.huaguang.flowoftime.hourThreshold2
import com.huaguang.flowoftime.minutesThreshold
import com.huaguang.flowoftime.names
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
    private val spHelper: SPHelper,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private val eventDao = repository.eventDao
    val events = eventDao.getAllEvents()
    val isTracking = MutableLiveData(false)
    private var currentEvent: Event? = null
    val buttonText = MutableLiveData(spHelper.getButtonText())
    private val newEventName = MutableLiveData("新事件")
    val scrollIndex = MutableLiveData<Int>()
    var eventCount = 0
    private val alarmHelper = AlarmHelper(application)
    val isAlarmSet = MutableLiveData(false)
    private val _remainingDuration = MutableStateFlow(spHelper.getRemainingDuration())
    val remainingDuration: StateFlow<Duration?> get() = _remainingDuration
    val rate: StateFlow<Float?> get() = _remainingDuration.map { remainingDuration ->
        remainingDuration?.let {
            it.toMillis().toFloat() / hourThreshold.toMillis()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)


    init {
        // 从SharedPreferences中恢复滚动索引
        val savedScrollIndex = spHelper.getScrollIndex()
        if (savedScrollIndex != -1) {
            scrollIndex.value = savedScrollIndex
            eventCount = savedScrollIndex + 1
        }
    }

    fun onClick() {
        when (buttonText.value) {
            "开始" -> {
                startNewEvent()
                buttonText.value = "结束"
            }
            "结束" -> {
                stopCurrentEvent()
                buttonText.value = "开始"
            }
        }
        spHelper.saveButtonText(buttonText.value!!)
    }

    fun onConfirm(textState: MutableState<String>) {
        if (textState.value.isEmpty()) {
            Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
            return
        }
        newEventName.value = textState.value

        // 以下两个函数调用内部都开了协程，所以会并行执行，且不会阻塞主线程。
        updateEventName()
        Log.i("打标签喽", "eventName = ${newEventName.value}")
        checkAndSetAlarm(newEventName.value!!)

        textState.value = ""
        newEventName.value = ""
        isTracking.value = false
    }

    private fun updateEventName() {
        currentEvent?.let {
            it.name = newEventName.value!!
            viewModelScope.launch {
                eventDao.updateEvent(it)
            }
        }
    }

    private fun startNewEvent() {
        val startTime = LocalDateTime.now()
        val newEvent = Event(
            name = newEventName.value!!,
            startTime = startTime,
            eventDate = repository.getEventDate(startTime)
        )
        Log.i("打标签喽", "startTime = $startTime")
        viewModelScope.launch {
            val eventId = eventDao.insertEvent(newEvent)
            currentEvent = newEvent.copy(id = eventId)
            isTracking.value = true
            // 更新事件数量
            eventCount++
            // 更新滚动索引
            scrollIndex.value = eventCount - 1
            // 保存滚动索引到SharedPreferences
            spHelper.saveScrollIndex(eventCount - 1)
        }
    }

    private fun stopCurrentEvent() {
        viewModelScope.launch {
            if (currentEvent == null) {
                currentEvent = eventDao.getLastEvent()
            }

            Log.i("打标签喽", "currentEvent 获取后 = $currentEvent")

            currentEvent?.let {
                Log.i("打标签喽", "let 块里边，执行！")
                it.endTime = LocalDateTime.now()
                it.duration = Duration.between(it.startTime, it.endTime)

                viewModelScope.launch {
                    eventDao.updateEvent(it)
                }

                // 只要包含，remainingDuration 就会得到设置，一定不为 null
                if (names.contains(it.name)) {
                    _remainingDuration.value = _remainingDuration.value?.minus(it.duration)
                    spHelper.saveRemainingDuration(_remainingDuration.value!!)

                    if (isAlarmSet.value == true && _remainingDuration.value!! > minutesThreshold) {
                        alarmHelper.cancelAlarm()
                        isAlarmSet.value = false
                    }
                }
            }
        }

        currentEvent = null
    }

    private fun checkAndSetAlarm(name: String) {
        Log.i("打标签喽", "checkAndSetAlarm 执行！！！")
        if (!names.contains(name)) return

        viewModelScope.launch {
            _remainingDuration.value = if (_remainingDuration.value == null) {
                // 数据库操作，查询并计算
                val totalDuration = repository.calculateTotalDuration()
                hourThreshold.minus(totalDuration)
            } else _remainingDuration.value

            if (_remainingDuration.value!! < hourThreshold2) {
                // 一般事务一次性持续时间都不超过 5 小时
                alarmHelper.setAlarm(_remainingDuration.value!!.toMillis())
                isAlarmSet.value = true
            }
        }
    }

}
