package com.huaguang.flowoftime.pages.time_record.time_regulator


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TimeRegulatorViewModel @Inject constructor(
    private val spHelper: SPHelper,
    private val eventRepository: EventRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    private val currentEvent get() = sharedState.currentEvent

    private lateinit var recordTime: LocalTime
    // 用于取消之前的协程
    private var updateJob: Job? = null

    fun calPauseInterval(checked: Boolean) { // checked 为 true 是继续（播放），表明当前事项正在计时……
        if (!checked) { // 暂停
            recordTime = LocalTime.now()
        } else { // 继续
            val interval = LocalTime.now().minute - recordTime.minute
            spHelper.savePauseInterval(interval)
        }
    }

    fun adjustTimeAndHandleChange(
        minutes: Long,
        customTimeState: MutableState<CustomTime?>,
        selectedTime: MutableState<LocalDateTime?>?
    ) {
        if (customTimeState.value == null) {
            sharedState.toastMessage.value = "请选中时间标签后再调整"
            return
        }

        handleTimeChange(selectedTime) {
            val newCustomTime = customTimeState.value!!.copy().apply {
                timeState.value = timeState.value?.plusMinutes(minutes)
            }
            customTimeState.value = newCustomTime // 触发状态更改
            if (newCustomTime.timeState.value != newCustomTime.initialTime!!) customTimeState
            else mutableStateOf(null)
        }
    }

    private fun handleTimeChange(
        selectedTime: MutableState<LocalDateTime?>?,
        action: () -> MutableState<CustomTime?>
    ) {
        val customTimeState = action()
        val newCustomTime = customTimeState.value ?: return // 执行时间调整

        updateJob?.cancel() // 取消之前的协程
        updateJob = viewModelScope.launch {
            delay(1000) // 延迟1秒
            syncUpdateCurrentAndDB(newCustomTime) // 同步更新当前项和数据库
            selectedTime?.value = null // 取消选中状态
            customTimeState.value = null // 为防止取消选中后再调整的时候更新起作用
            sharedState.toastMessage.value = "调整已更新"
        }
    }

    private suspend fun syncUpdateCurrentAndDB(newCustomTime: CustomTime) {
        updateCurrentTime(newCustomTime)
        val newDuration = calNewDuration()
        updateCurrentDuration(newDuration)
        eventRepository.updateDatabase(newCustomTime, newDuration) // 更新数据库的开始、结束时间，同时更新 duration
    }

    /**
     * 更新当前项的开始时间和结束时间，以保持内存当前项和数据库当前项同步
     */
    private fun updateCurrentTime(newCustomTime: CustomTime) {
        val newTime = newCustomTime.timeState.value!!

        if (newCustomTime.type == TimeType.START) {
            currentEvent?.startTime = newTime
        } else {
            currentEvent?.endTime = newTime
        }
    }

    /**
     * 根据开始时间和结束时间获取
     * start 反，end 正（变化量和最终 duration 的变化关系）
     */
    private fun calNewDuration(): Duration? {
        if (currentEvent == null) return null

        val startTime = currentEvent!!.startTime
        val endTime = currentEvent!!.endTime

        return endTime?.let { Duration.between(startTime, it) }

    }

    private fun updateCurrentDuration(newDuration: Duration?) {
        currentEvent?.duration = newDuration
    }

}