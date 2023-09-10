package com.huaguang.flowoftime.ui.pages.time_record.time_regulator


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class TimeRegulatorViewModel @Inject constructor(
    private val spHelper: SPHelper,
    private val repository: EventRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    var selectedTime: MutableState<LocalDateTime?>? = null

    private lateinit var recordTime: LocalTime
    // 用于取消之前的协程
    private var updateJob: Job? = null
    // 存储上次点击的时间戳
    private var lastClickTime: Long = 0

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }

    fun calPauseInterval(checked: Boolean) { // checked 为 true 是继续（播放），表明当前事项正在计时……
        if (!checked) { // 暂停
            recordTime = LocalTime.now()
        } else { // 继续
            val interval = Duration.between(recordTime, LocalTime.now()).toMinutes().toInt()
            spHelper.savePauseInterval(interval)
        }
    }

    fun onClick(
        value: Long,
        customTimeState: MutableState<CustomTime?>,
    ) {
        if (customTimeState.value == null) { // 非选中态
            debouncedOnClick {
                sharedState.toastMessage.value = "请选中时间标签后再调整"
            }
            return
        }

        adjustTimeAndHandleChange(value, customTimeState) //选中态直接调整

    }

    /**
     * 计算含有子事项的父事项的真正时长，并更新结束时间和 duration 到数据库
     */
    suspend fun calParentEventDurationAndUpdateDB(
        eventId: Long,
        endTime: LocalDateTime = LocalDateTime.now(),
    ) {
        val duration = calEventDuration(eventId, endTime)
        repository.updateDB(eventId, endTime, duration)
    }

    /**
     * 每个含有下级的事项，都要减去本事项的暂停间隔，然后还要减去插入事项的总时长。
     * @param eventId 它就是那个含有下级事项的父事项 id
     */
    private suspend fun calEventDuration(eventId: Long, endTime: LocalDateTime): Duration {
        val stopRequired = repository.getStopRequired(eventId)
        val pauseIntervalDuration = Duration.ofMinutes(stopRequired.pauseInterval.toLong())
        RDALogger.info("pauseIntervalDuration = $pauseIntervalDuration")
        val totalDurationOfSubInsert = repository.calTotalSubInsertDuration(eventId)
        RDALogger.info("totalDurationOfSubInsert = $totalDurationOfSubInsert")
        val standardDuration = Duration.between(stopRequired.startTime, endTime)
        RDALogger.info("standardDuration = $standardDuration")

        return standardDuration.minus(totalDurationOfSubInsert).minus(pauseIntervalDuration)
    }

    private fun debouncedOnClick(onClick: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        // 如果两次点击的时间差小于1000毫秒，则返回
        if (currentTime - lastClickTime < 1000) {
            return
        }
        // 更新上次点击的时间戳
        lastClickTime = currentTime
        onClick() // 执行点击逻辑
    }

    private fun adjustTimeAndHandleChange(
        minutes: Long,
        customTimeState: MutableState<CustomTime?>,
    ) = handleTimeChange {
        val newCustomTime = customTimeState.value!!.copy().apply {
            timeState.value = timeState.value?.plusMinutes(minutes)
        }
        customTimeState.value = newCustomTime // 触发状态更改
        if (newCustomTime.timeState.value != newCustomTime.initialTime!!) customTimeState
        else mutableStateOf(null)
    }

    private fun handleTimeChange(
        action: () -> MutableState<CustomTime?>
    ) {
        val customTimeState = action()
        val newCustomTime = customTimeState.value ?: return // 执行时间调整

        updateJob?.cancel() // 取消之前的协程
        updateJob = viewModelScope.launch {
            delay(1500) // 延迟1.5秒

            val newTime = newCustomTime.timeState.value!!
            val type = newCustomTime.type
            if (type == TimeType.START) sharedState.currentEvent?.startTime = newTime

            newCustomTime.eventInfo.apply {
                if (type == TimeType.END && withContent && !isTiming) { // 如果调整的是有下级且已经结束过的事项的结束标签，那么就重新计算（结束时计算）
                    calParentEventDurationAndUpdateDB(id, newTime)
                } else { // 一般调整的计算方式
                    val newDuration = calNewDuration(newCustomTime)
                    repository.updateDatabase(newCustomTime, newDuration) // 更新数据库的开始、结束时间，同时更新 duration
                }
            }

            selectedTime?.value = null // 取消选中状态
            customTimeState.value = null // 为防止取消选中后再调整的时候更新起作用
            sharedState.toastMessage.value = "调整已更新"
        }
    }

    /**
     * 根据变化量获取。start 反，end 正（变化量和最终 duration 的变化关系）
     * 不能根据结束时间和开始时间的简单相减来获取，这会把结束事件时的计算成果覆灭，结果还不准确。
     */
    private suspend fun calNewDuration(newCustomTime: CustomTime): Duration? {
        newCustomTime.apply {
            if (type == TimeType.START && eventInfo.isTiming) return null

            val deltaDuration = ChronoUnit.MINUTES.between(initialTime, timeState.value).let {
                if (it == 0L) Duration.ZERO else Duration.ofMinutes(it)
            }
            val originalDuration = repository.getDurationById(eventInfo.id)

            return if (type == TimeType.START) {
                originalDuration - deltaDuration
            } else {
                originalDuration + deltaDuration
            }
        }
    }
}