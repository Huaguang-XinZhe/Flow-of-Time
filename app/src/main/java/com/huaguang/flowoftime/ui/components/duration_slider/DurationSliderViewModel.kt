package com.huaguang.flowoftime.ui.components.duration_slider

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.sleepNames
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.utils.isSleepingTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DurationSliderViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    // 共享属性
    private val newEventName
        get() = sharedState.newEventName.value

    // 专有
    val coreDuration = mutableStateOf(Duration.ZERO)
    val rate = derivedStateOf {
        coreDuration.value.toMillis().toFloat() / FOCUS_EVENT_DURATION_THRESHOLD.toMillis()
    }
    val isAlarmSet = mutableStateOf(false)
    var startTimeTracking: LocalDateTime? = null // 记录正在进行的核心事务的开始时间
    var isCoreDurationReset = false
    var isCoreEventTracking = false


    fun updateCoreDuration() {
        if (startTimeTracking != null) { // 当下核心事务的计时正在进行
            Log.i("打标签喽", "updateCoreDuration 执行！！！")
            val now = LocalDateTime.now()
            coreDuration.value += Duration.between(startTimeTracking!!, now)
            startTimeTracking = now
        }
    }

    fun updateCoreDurationOnDragStopped(
        updatedEvent: Event,
        originalDuration: Duration?,
        currentBeforeST: LocalDateTime,
    ) {
        if (!isCoreEvent(updatedEvent.name)) return // 更新当下核心事务的持续时间

        if (updatedEvent.duration != null) {
            updateCDonStoredItem(updatedEvent.duration!!, originalDuration!!)
        } else {
            updateCDonCurrentItem(currentBeforeST, updatedEvent.startTime)
        }
    }

    private fun updateCDonStoredItem(updatedDuration: Duration, originalDuration: Duration) {
        coreDuration.value += updatedDuration - originalDuration
    }

    private fun updateCDonCurrentItem(currentBeforeST: LocalDateTime, updatedST: LocalDateTime) {
        coreDuration.value -= Duration.between(currentBeforeST, updatedST)
    }

    fun updateCDonNameChangeConfirmed(
        name: String,
        event: Event,
        clickFlag: MutableState<Boolean>
    ) {
        if (isCoreEvent(name)) { // 文本是当下核心事务（之前不是）
            increaseStoredDuration(event.duration!!)
        } else { // 已修改，不是当下核心事务
            if (clickFlag.value) { // 点击修改之前是当下核心事务
                reduceStoredDuration(event.duration!!)
                clickFlag.value = false
            }
        }
    }

    private fun increaseStoredDuration(duration: Duration) {
        coreDuration.value += duration
    }

    fun reduceStoredDuration(duration: Duration) {
        coreDuration.value -= duration
    }

    fun increaseCDonCurrentStop(currentEvent: Event) {
        if (isCoreEvent(currentEvent.name)) { // 结束的是当下核心事务
            coreDuration.value += Duration.between(startTimeTracking!!, currentEvent.endTime)
            startTimeTracking = null
            isCoreEventTracking = false
        }
    }

    fun reduceCDonCurrentCancel(currentST: LocalDateTime, isCoreEvent: Boolean = false) {
        if (isCoreEvent) {
            val duration = Duration.between(currentST, LocalDateTime.now())
            coreDuration.value -= duration
        }
    }


    fun handleCoreOrSleepEvent(currentEvent: Event) {
        fun isSleepEvent(startTime: LocalDateTime): Boolean {
            return sleepNames.contains(newEventName) && isSleepingTime(startTime.toLocalTime())
        }

        currentEvent.let {
            if (isCoreEvent(newEventName)) { // 当下核心事务
                isCoreEventTracking = true
                startTimeTracking = currentEvent.startTime
            }

            if (isSleepEvent(currentEvent.startTime)) { // 晚睡
                isCoreDurationReset = false

                viewModelScope.launch(Dispatchers.IO) {
                    // 更新或存储当下核心事务的总值
                    repository.updateCoreDurationForDate(getAdjustedEventDate(), coreDuration.value)
                }
            }
        }
    }


//    private fun checkAndSetAlarm(name: String) {
//        if (!isCoreEvent(name)) return
//
//        if (coreDuration.value < ALARM_SETTING_THRESHOLD) {
//            // 一般事务一次性持续时间都不超过 5 小时
//            alarmHelper.setAlarm(coreDuration.value!!.toMillis())
//            isAlarmSet.value = true
//        }
//    }
//
//    private fun cancelAlarm() {
//        currentEvent.value?.let {
//            if (coreDuration.value != null && isCoreEvent(it.name)) {
//                coreDuration.value = coreDuration.value?.minus(it.duration)
//
//                if (isAlarmSet.value == true &&
//                    coreDuration.value!! > ALARM_CANCELLATION_THRESHOLD
//                ) {
//                    alarmHelper.cancelAlarm()
//                    isAlarmSet.value = false
//                }
//            }
//        }
//    }
//
//    private fun resetStateIfNewDay() {
//        viewModelScope.launch {
//            val events = eventsWithSubEvents.first()
//            if (events.isEmpty()) {
//                Log.i("打标签喽", "coreDuration 置空执行了。")
//                coreDuration.value = null
//            }
//        }
//    }
//
//    private suspend fun setCoreDuration() {
//        coreDuration.value = if (coreDuration.value == null) {
//            Log.i("打标签喽", "setCoreDuration 块内：currentEvent = $currentEvent")
//            // 数据库操作，查询并计算
//            val totalDuration = repository.calEventDateDuration(
//                currentEvent.value?.eventDate ?: LocalDate.now()
//            )
//            FOCUS_EVENT_DURATION_THRESHOLD.minus(totalDuration)
//        } else coreDuration.value
//    }


}