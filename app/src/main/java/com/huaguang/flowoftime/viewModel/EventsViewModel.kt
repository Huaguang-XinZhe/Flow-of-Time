package com.huaguang.flowoftime.viewModel

import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventDao
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class EventsViewModel(private val eventDao: EventDao) : ViewModel() {

    val events = eventDao.getAllEvents()
    val isTracking = MutableLiveData(false)
    var currentEvent: Event? = null
    val buttonText = MutableLiveData("开始")
    private val newEventName = MutableLiveData("新事件")

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

    fun onConfirm(text: String) {
        newEventName.value = text
        updateEventName()
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
