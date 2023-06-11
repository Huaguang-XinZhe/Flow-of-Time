package com.huaguang.flowoftime.ui.components.duration_slider

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.DataStoreHelper
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.sleepNames
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.utils.isSleepingTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DurationSliderViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    private val dataStoreHelper: DataStoreHelper,
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
    private var startCursor: LocalDateTime? = null // 记录正在进行的核心事务的开始时间
    private var subEventCount = 0
    var isCoreDurationReset = false


    /**
     * 这是个更新当下核心事务持续时间的关键方法，它综合了多个场景的应用，是为通用方法。
     * 1. Resume 时（包括启动时的场景）；
     * 2. 当下核心事务停止时；
     * @param mainEventId 这是正在进行核心事务的主事项的 id，也即是子事项的 parentId
     * @param currentSubEventST 这是当前子事项的开始时间，由于事件正在进行，所以没有存入数据库；
     * 传入该参数就意味着当前的子事项正在计时，有开始时间，如果当前只有主事项正在计时就不需要传入了；
     * 该参数的默认值为 now，这是因为只要当前子事项正在进行，subSum 的计算就需要加上 {now - currentSubEventST}；
     * 将其默认值设为 now 可以继续沿用此公式，加上 0，这也就是没有子事项正在进行时的 subSum 的计算方式。
     */
    fun updateCoreDuration(
        mainEventId: Long,
        currentSubEventST: LocalDateTime = LocalDateTime.now()
    ) {
        val now = LocalDateTime.now()

        viewModelScope.launch {
            // 订阅获取 DataStore 中 startCursor 的最新值（冷流获取）
            startCursor = dataStoreHelper.startCursorFlow.firstOrNull()

            // 核心事务正在进行（大前提）
            if (startCursor != null) {
                val isSubEventTracking = sharedState.eventStatus.value == EventStatus.fromInt(2)

                // 1. 如果当前事项正在进行且 start >= 当前子事项的开始时间，那么便不做更新；
                if (isSubEventTracking && startCursor!! >= currentSubEventST) return@launch

                subEventCount = dataStoreHelper.subEventCountFlow.first()

                // 2. 如果当前项是主事件且没有插入过子事件，那么 delta = now - start，subSum 为 0；
                val subSum = if (subEventCount == 0 &&
                    sharedState.eventStatus.value == EventStatus.fromInt(1)) {
                    Duration.ZERO
                } else { // 3. 除以上一二种可能的其他情况（子事项可能正在计时，也可能不在）
                    val subEventTimesList =
                        repository.eventDao.getSubEventTimesWithinRange(mainEventId, startCursor)
                    // 当有子事项正在计时的时候，还要加上这一部分，没有的话就不传入 currentSubEventST，结果为 0
                    val isSubTrackingAdditional = Duration.between(currentSubEventST, now)

                    calculateSubSum(subEventTimesList, startCursor!!) + isSubTrackingAdditional
                }

                val delta = Duration.between(startCursor!!, now) - subSum
                increaseDuration(delta)
            }
        }
    }

    private fun calculateSubSum(eventTimes: List<EventTimes>, start: LocalDateTime): Duration {
        // 提取出重复的代码为一个局部函数
        fun calculatePairDurations(pairs: List<LocalDateTime>): Duration {
            return pairs.zipWithNext()
                .sumOf { (first, second) ->
                    Duration.between(first, second).toMillis()
                }.let { Duration.ofMillis(it) }
        }

        // 将 EventTimes 列表转化为 LocalDateTime 列表，并进行排序
        val times = eventTimes
            .flatMap { listOfNotNull(it.startTime, it.endTime) }
            .sorted()

        return if (times.size % 2 == 0) {
            // 对于偶数个时间点，将它们两两配对，然后计算每一对的差值，最后将所有的差值相加
            calculatePairDurations(times)
        } else {
            // 对于奇数个时间点，先计算第一个时间点和 start 的差值，然后将剩余的时间点两两配对，计算每一对的差值，最后将所有的差值相加
            Duration.between(start, times[0]) + calculatePairDurations(times.drop(1))
        }
    }



    /**
     * 更新 startCursor 并立即存入 DataStore
     */
    private fun updateStartCursor(value: LocalDateTime?) {
        startCursor = value
        // 立即存入 DataStore
        viewModelScope.launch {
            dataStoreHelper.saveStartCursor(value)
        }
    }


//    fun increaseCDifCoreTracking() {
//        if (startCursor != null) { // 当下核心事务的计时正在进行
//            val now = LocalDateTime.now()
//            coreDuration.value += Duration.between(startCursor!!, now)
//            startCursor = now
//        }
//    }


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
        previousName: String,
        presentName: String, // 点击确认后的事项名称
        event: Event, // 点击确认后的事项
    ) {
        val isCoreNow = isCoreEvent(presentName)
        val isCorePrevious = isCoreEvent(previousName)
        val isTrackingOnClicked = event.duration == null

        // 点击确认不会改变事项是否正在进行的性质！！！
        val duration =
            if (isTrackingOnClicked) { // 事件正在进行中（包括已经入库但还在进行中的主事件）
                Duration.between(event.startTime, LocalDateTime.now()) // TODO:
            } else event.duration!! // 事件已经结束

        if (isCorePrevious && !isCoreNow) { // 之前的核心事务正在进行，现在改成别的事项了
            reduceDuration(duration)

            if (isTrackingOnClicked) { //正在进行
                viewModelScope.launch {
                    dataStoreHelper.saveStartCursor(null)
                }
            }
        }

        if (isCoreNow && !isCorePrevious) { // 现在是核心事务，但之前不是，应该加上
            increaseDuration(duration)

            if (isTrackingOnClicked) {
                updateStartCursor(event.startTime)
            }
        }
    }

    private fun increaseDuration(duration: Duration) {
        coreDuration.value += duration
    }

    fun reduceDuration(duration: Duration) {
        coreDuration.value -= duration
    }

    suspend fun updateCDonCurrentStop(currentEvent: Event) {
        if (isCoreEvent(currentEvent.name)) { // 结束的是当下核心事务
            updateCoreDuration(currentEvent.id)
            dataStoreHelper.saveStartCursor(null)
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
                updateStartCursor(currentEvent.startTime)
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