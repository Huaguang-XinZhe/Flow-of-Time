package com.huaguang.flowoftime.ui.pages.time_record.time_regulator


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
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

    private var recordTime: LocalTime? = null
    // 用于取消之前的协程
    private var updateJob: Job? = null
    // 存储上次点击的时间戳
    private var lastClickTime: Long = 0

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }

    /**
     * 暂停/恢复按钮是否允许点击？
     * 只要当前有事件正在进行，且不为插入事件就可以了。
     */
    fun pauseButtonEnabled() = sharedState.cursorType.value.let { it != null && it != EventType.INSERT }

    fun calPauseInterval(checked: Boolean) { // checked 为 true 是继续（播放），表明当前事项正在计时……
        if (!checked) { // 暂停
            recordTime = LocalTime.now()
        } else { // 继续
            val interval = if (recordTime == null) 0 else {
                ChronoUnit.MINUTES.between(recordTime, LocalTime.now()).toInt()
            }
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
            if (newCustomTime.type == TimeType.START) sharedState.currentEvent?.startTime = newTime

            updateCurrentOrParentEvent(newCustomTime)

            selectedTime?.value = null // 取消选中状态
            customTimeState.value = null // 为防止取消选中后再调整的时候更新起作用
            sharedState.toastMessage.value = "调整已更新"
        }
    }

    private suspend fun updateCurrentOrParentEvent(newCustomTime: CustomTime) {
        val pair = calNewDurationPair(newCustomTime)

        repository.updateTimeAndDuration(newCustomTime, pair.first) // 更新数据库的开始、结束时间，同时更新 duration

        if (pair.second != null) { // 只更新当前项就可以了
            repository.updateDuration(newCustomTime.eventInfo.parentId!!, pair.second!!)
        }
    }

    /**
     * 根据变化量获取。start 反，end 正（变化量和最终 duration 的变化关系）
     * 不能根据结束时间和开始时间的简单相减来获取，这会把结束事件时的计算成果覆灭，结果还不准确。
     * @return 一般只取返回值的第一个元素（当前调整项的新 Duration），
     * 第二个元素是为已经结束的插入事件的父事件（也已经结束）准备的，如果为 null，表示不需要更新。
     */
    private suspend fun calNewDurationPair(newCustomTime: CustomTime): Pair<Duration?, Duration?> {
        with(newCustomTime) {
            if (type == TimeType.START && eventInfo.isTiming) return null to null

            val deltaDuration = ChronoUnit.MINUTES.between(initialTime, timeState.value).toDuration()

            with(eventInfo) {
                val originalDuration = repository.getDurationById(id)

                val newDuration = if (type == TimeType.START) {
                    originalDuration - deltaDuration
                } else {
                    originalDuration + deltaDuration
                }

                if (eventType == EventType.INSERT && !isTiming) {
                    val parentEvent = repository.getInsertParentById(parentId!!)
                    if (parentEvent.endTime != null) {
                        val newParentDuration = if (type == TimeType.START) {
                            parentEvent.duration + deltaDuration // start 同向变化（父）
                        } else {
                            parentEvent.duration - deltaDuration // end 反向变化（父）
                        }
                        return newDuration to newParentDuration
                    }
                }

                return newDuration to null
            }
        }
    }

    private fun Long.toDuration(): Duration {
        return if (this == 0L) Duration.ZERO else Duration.ofMinutes(this)
    }


}