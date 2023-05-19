package com.huaguang.flowoftime.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventDao
import com.huaguang.flowoftime.utils.SPHelper
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class EventsViewModel(
    private val eventDao: EventDao,
    private val spHelper: SPHelper) : ViewModel() {

    val events = eventDao.getAllEvents()
    val isTracking = MutableLiveData(false)
    private var currentEvent: Event? = null
    val buttonText = MutableLiveData("开始")
    private val newEventName = MutableLiveData("新事件")
    val scrollIndex = MutableLiveData<Int>()
    private var eventCount = 0

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
    }

    fun onConfirm(textState: MutableState<String>, context: Context) {
        if (textState.value.isEmpty()) {
            Toast.makeText(context, "你还没有输入呢？", Toast.LENGTH_SHORT).show()
            return
        }
        newEventName.value = textState.value
        updateEventName()
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
        val newEvent = Event(name = newEventName.value!!, startTime = startTime)
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
        currentEvent?.let {
            it.endTime = LocalDateTime.now()
            it.duration = Duration.between(it.startTime, it.endTime)
            viewModelScope.launch {
                eventDao.updateEvent(it)
            }
        }
        currentEvent = null
    }
}
