package com.huaguang.flowoftime.ui.pages.time_record.time_regulator


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.repositories.DailyStatisticsRepository
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.InputState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class TimeRegulatorViewModel @Inject constructor(
    private val repository: EventRepository,
    private val statRepository: DailyStatisticsRepository,
    private val sharedState: SharedState,
    private val pauseState: PauseState,
    private val idState: IdState,
    val inputState: InputState, // 必须加，为了防止软键盘弹出时组件被顶到屏幕上面去
) : ViewModel() {

    var selectedTime: MutableState<LocalDateTime?>? = null
    var customTimeState:  MutableState<CustomTime?>? = null

    // 用于取消之前的协程
    private var updateJob: Job? = null
    // 存储上次点击的时间戳
    private var lastClickTime: Long = 0


    private var initialized = false
    val checkedLiveData = MutableLiveData(true) // 我需要的就是外界更改其值，所以就不保护了！
    private val checkedLiveDataObserver = Observer<Boolean> { newValue ->
        handleCheckedLiveDataChange(newValue)
    }

    init {
        checkedLiveData.observeForever(checkedLiveDataObserver)
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
        checkedLiveData.removeObserver(checkedLiveDataObserver)
    }

    fun toggleChecked(checked: Boolean) {
        checkedLiveData.value = checked
        RDALogger.info("toggle 执行，checkedLiveData.value = ${checkedLiveData.value}")
    }

    /**
     * 暂停/恢复按钮是否允许点击？
     * 只要当前有事件正在进行，且不为插入事件就可以了。
     */
    fun pauseButtonEnabled() = sharedState.cursorType.value.let { it != null && !it.isInsert() }

    fun onClick(value: Long, ) {
        if (customTimeState?.value == null) { // 非选中态
            debouncedOnClick {
                sharedState.toastMessage.value = "请选中时间标签后再调整"
            }
            return
        }

        adjustTimeAndHandleChange(value) //选中态直接调整

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

    private fun adjustTimeAndHandleChange(minutes: Long) = handleTimeChange {
        val newCustomTime = customTimeState?.value!!.copy().apply {
            timeState.value = timeState.value?.plusMinutes(minutes)
        }
        customTimeState?.value = newCustomTime // 触发状态更改
        if (newCustomTime.timeState.value != newCustomTime.initialTime!!) customTimeState!!
        else mutableStateOf(null)
    }

    private fun handleTimeChange(
        action: () -> MutableState<CustomTime?>
    ) {
        val customTimeState = action()
        val newCustomTime = customTimeState.value ?: return // 执行时间调整

        updateJob?.cancel() // 取消之前的协程
        updateJob = viewModelScope.launch {
            delay(1200) // 延迟1.2秒

            updateDB(newCustomTime)

            selectedTime?.value = null // 取消选中状态
            customTimeState.value = null // 为防止取消选中后再调整的时候更新起作用
            sharedState.toastMessage.value = "调整已更新"
        }
    }

    private suspend fun updateDB(newCustomTime: CustomTime) {
        val (newDuration, durationMap) = calNewDurationPair(newCustomTime) { signedDelta ->
            // 如有必要，更新类属时长
            val (eventDate, category) = repository.getDateAndCategoryById(idState.subject.value)
            category?.let {
                statRepository.updateCategoryDuration(eventDate, it, signedDelta)
            }
        }

        repository.updateTimeAndDuration(newCustomTime, newDuration) // 更新数据库的开始、结束时间，同时更新 duration

        durationMap?.let { map ->
            for ((id, duration) in map) {
                repository.updateDuration(id, duration) // 更新父事件和可能的主题事件的 duration
            }
        }
    }

    /**
     * 根据变化量获取。start 反，end 正（变化量和最终 duration 的变化关系）
     * 不能根据结束时间和开始时间的简单相减来获取，这会把结束事件时的计算成果覆灭，结果还不准确。
     * @return 一般只取返回值的第一个元素（当前调整项的新 Duration），
     * 第二个元素是为已经结束的插入事件的父事件（也已经结束）准备的，如果为 null，表示不需要更新。
     */
    private suspend fun calNewDurationPair(
        newCustomTime: CustomTime,
        onCategoryUpdate: suspend (signedDelta: Duration) -> Unit
    ): Pair<Duration?, Map<Long, Duration>?> {
        with(newCustomTime) {
            if (eventInfo.isTiming) return null to null

            /*------------------------------- 调整项已经结束 ------------------------------------------*/
            val deltaDuration = ChronoUnit.MINUTES.between(initialTime, timeState.value).toDuration()
            val originalDuration = repository.getDurationById(eventInfo.id)
            val newDuration = calNewDurationWithCurrent(type, originalDuration, deltaDuration)

            if (eventInfo.eventType == EventType.SUBJECT) {
                // 调整已经结束的主题事件
                onCategoryUpdate(signedDelta(type, deltaDuration, true))
            }

            if (!eventInfo.eventType.isInsert()) return newDuration to null

            /*------------------------------------ 调整项是插入事件 ---------------------------------------*/
            val insertParent = repository.getInsertParentById(eventInfo.parentId!!)
            if (insertParent.endTime == null) return newDuration to null

            /*---------------------------------- 插入事项的父事项也已经结束 -----------------------------------*/
            val newParentDuration = calNewDurationWithSuper(type, insertParent.duration!!, deltaDuration)
            val durationMap = mutableMapOf(eventInfo.parentId to newParentDuration) // 这个 parent 有可能是主题事件，也有可能是步骤事件，此时的 map 只有一个元素

            if (eventInfo.eventType == EventType.SUBJECT_INSERT) {
                // 调整已经结束的主题事件下的插入事件
                onCategoryUpdate(signedDelta(type, deltaDuration))
            } else if (sharedState.cursorType.value == null) {
                // 调整已经结束的步骤事件下的插入事件（此时主题事件也结束了，只有这样，才会影响到主题事件，才需要进一步更新）
                onCategoryUpdate(signedDelta(type, deltaDuration))

                val subjectId = idState.subject.value
//                RDALogger.info("进一步计算，获取主事件的 id = $subjectId")
                val subjectDuration = repository.getDurationById(subjectId)
                val newSubjectDuration = calNewDurationWithSuper(type, subjectDuration, deltaDuration)
//                RDALogger.info("newSubjectDuration = $newSubjectDuration")
                durationMap[subjectId] = newSubjectDuration // 此时的 map 有两个元素，第二个是主题事件，到这一步，那就说明上个 parent 一定是步骤事件了
            }

            return newDuration to durationMap
        }
    }

    /**
     * 赋予变化量以符号（方向）
     */
    private fun signedDelta(
        timeType: TimeType,
        deltaDuration: Duration,
        isStartReverse: Boolean = false, // 是否是 start 反，end 正？这是赋予变化量符号的模式，根据情况而定。
    ) = if (isStartReverse) {
        if (timeType == TimeType.START) Duration.ZERO.minus(deltaDuration) else deltaDuration
    } else {
        if (timeType == TimeType.START) deltaDuration else Duration.ZERO.minus(deltaDuration)
    }

    /**
     * 计算当前调整项的新时长
     * start 反，end 同
     */
    private fun calNewDurationWithCurrent(
        type: TimeType,
        originalDuration: Duration,
        deltaDuration: Duration
    ) = if (type == TimeType.START) {
        originalDuration - deltaDuration
    } else {
        originalDuration + deltaDuration
    }

    /**
     * 计算上级的新时长，可能是步骤，也可能是主题
     */
    private fun calNewDurationWithSuper(
        type: TimeType,
        parentDuration: Duration,
        deltaDuration: Duration,
    ) = if (type == TimeType.START) {
        parentDuration + deltaDuration // start 同向变化
    } else {
        parentDuration - deltaDuration // end 反向变化
    }

    private fun Long.toDuration(): Duration {
        return if (this == 0L) Duration.ZERO else Duration.ofMinutes(this)
    }

    private fun handleCheckedLiveDataChange(newValue: Boolean) {
        // 防止初始化的时候执行
        if (initialized) {
            calPauseInterval(newValue) // 希望比副作用要快
            RDALogger.info("监听 acc = ${pauseState.acc.value}")

            if (newValue) { // 只有恢复原先状态的时候才会执行。
                resetPauseState()
            }
        }

        initialized = true // 必须放在 if 块外，如果直接 return，那就相当于放在块内了。
    }

    private fun calPauseInterval(checked: Boolean?) { // checked 为 true 是继续（播放），表明当前事项正在计时……
        pauseState.apply {
            if (checked == false) { // 暂停
                start.value = LocalDateTime.now()
                RDALogger.info("暂停之后：start = ${start.value}")
            } else { // 继续
                RDALogger.info("恢复/继续！acc = ${acc.value}, start = ${start.value}")
                val interval = if (start.value == null) 0 else {
                    ChronoUnit.MINUTES.between(start.value, LocalDateTime.now()).toInt()
                }
                acc.value += interval
            }
        }
    }

    private fun resetPauseState() {
        pauseState.apply {
            when(sharedState.cursorType.value) {
                EventType.SUBJECT -> subjectAcc.value = acc.value
                EventType.STEP -> stepAcc.value = acc.value
                else -> currentAcc.value = acc.value // else 包括 null，但没事，null 下根本不会执行到这里
            }

            RDALogger.info("重置 pauseState 的状态")
            start.value = null
            acc.value = 0
        }
    }


}