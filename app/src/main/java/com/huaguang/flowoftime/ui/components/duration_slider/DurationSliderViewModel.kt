package com.huaguang.flowoftime.ui.components.duration_slider

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventStatus
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventTimes
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import com.huaguang.flowoftime.sleepNames
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.utils.isGetUpTime
import com.huaguang.flowoftime.utils.isSleepingTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class DurationSliderViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    private val dataStoreHelper: DataStoreHelper,
) : ViewModel() {

    // 外界依赖（由 sharedState 共享）
    private val newEventName
        get() = sharedState.newEventName.value
    private val isSubEventTracking
        get() = sharedState.eventStatus.value == EventStatus.fromInt(2)

    // 专有
    val coreDuration = mutableStateOf(Duration.ZERO)
    val rate = derivedStateOf {
        coreDuration.value.toMillis().toFloat() / FOCUS_EVENT_DURATION_THRESHOLD.toMillis()
    }
    val isAlarmSet = mutableStateOf(false)
    private var startCursor: LocalDateTime? = null // 记录正在进行的核心事务的开始时间
    private var saveCoreDurationFlag = false

//    // 这是为了能在多个方法中使用这一变量（紧跟着，如果把那两个方法合并到一处，就不需要这个变量了），
//    // 本质上是观察 DataStore 中的变化
//    private var subEventCount = 0

    init {
        viewModelScope.launch {
            // 订阅获取 DataStore 中 startCursor 的最新值（冷流获取）
            startCursor = dataStoreHelper.startCursorFlow.firstOrNull()
            saveCoreDurationFlag = dataStoreHelper.saveCoreDurationFlagFlow.first()
        }
    }

    // 核心事务持续时间更新的关键算法——————————————————————————————————————————————————————————————————

    /**
     * 这是个更新当下核心事务持续时间的关键方法，它综合了多个场景的应用，是为通用方法。
     * 1. Resume 时（包括启动时的场景）；
     * 2. 当下核心事务停止时；
     * @param mainEventId 这是正在进行核心事务的主事项的 id，也即是子事项的 parentId。
     * @param currentSubEventST 这是当前子事项的开始时间，由于事件正在进行，所以没有存入数据库；
     * 1. 传入该参数就意味着当前的子事项正在计时，有开始时间，如果当前只有主事项正在计时就不需要传入了；
     * 2. 该参数的默认值为 now，这是因为只要当前子事项正在进行，subSum 的计算就需要加上 {now - currentSubEventST}；
     * 将其默认值设为 now 可以继续沿用此公式，加上 0，这也就是没有子事项正在进行时的 subSum 的计算方式。
     * @param start 这是传入的变更后的主事项的开始时间，默认为 null；
     * 通过传入就免于从 DataStore 中获取了，一个小小地性能优化。
     */
    suspend fun updateCoreDuration(
        mainEventId: Long,
        currentSubEventST: LocalDateTime = LocalDateTime.now(),
        start: LocalDateTime? = null,
    ) {
        RDALogger.info("更新 CoreDuration！！！")
        val now = LocalDateTime.now()

        if (start != null) {
            startCursor = start
        }

        RDALogger.info("startCursor = $startCursor")
        if (startCursor == null) return
        RDALogger.info("核心事务正在进行……")

        // 如果当前子事项正在进行且 start >= 当前子事项的开始时间，那么便不做更新
        if (isSubEventTracking && startCursor!! >= currentSubEventST) return

        // 获取当前事件的状态
        val currentStatus = sharedState.eventStatus.value
        val isMainEventTracking = currentStatus == EventStatus.fromInt(1)

        // 获取子事件的数量
        val subEventCount = dataStoreHelper.subEventCountFlow.first()

        // 计算 subSum
        val subSum =
            calculateSubSumIfNecessary(
                mainEventId,
                currentSubEventST,
                isMainEventTracking,
                now,
                subEventCount
            )

        // 计算 delta，并更新总持续时间
        val delta = Duration.between(startCursor!!, now) - subSum
        RDALogger.info("delta = ${formatDurationInText(delta)}")
        increaseDuration(delta)

        // 收集并更新 delta
        updateDeltaSum(delta)

        // 更新 startCursor
        updateStartCursor(now)
    }


    /**
     * 如果必要的话，计算 subSum
     * @param currentSubEventST 这是当前子事项的开始时间，由于事件正在进行，所以没有存入数据库；
     * 传入该参数就意味着当前的子事项正在计时，有开始时间，如果当前只有主事项正在计时就不需要传入了；
     * 该参数的默认值为 now，这是因为只要当前子事项正在进行，subSum 的计算就需要加上 {now - currentSubEventST}；
     * 将其默认值设为 now 可以继续沿用此公式，加上 0，这也就是没有子事项正在进行时的 subSum 的计算方式。
     */
    private suspend fun calculateSubSumIfNecessary(
        mainEventId: Long,
        currentSubEventST: LocalDateTime,
        isMainEventTracking: Boolean,
        now: LocalDateTime,
        subEventCount: Int
    ): Duration {
        // 如果当前项是主事件且没有插入过子事件，那么 subSum 为 0
        return if (subEventCount == 0 && isMainEventTracking) {
            RDALogger.info("当前项是主事件，且没有插入过子事件")
            Duration.ZERO
        } else {
            // 获取在 startCursor 和现在之间的所有子事件的时间
            val subEventTimesList =
                repository.getSubEventTimesWithinRange(mainEventId, startCursor)
            // 如果有子事项正在计时，那么还需要加上子事项开始时间到现在的这段时间
            val isSubTrackingAdditional = Duration.between(currentSubEventST, now)

            // 计算子事件的总时间
            calculateSubSum(subEventTimesList, startCursor!!) + isSubTrackingAdditional
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

    // ————————————————————————————————————————————————————————————————————————————————————————————

    /**
     * 更新 startCursor 并立即存入 DataStore
     */
    suspend fun updateStartCursor(value: LocalDateTime?) {
        startCursor = value
        RDALogger.info("更新 startCursor 的值：$startCursor")
        // 立即存入 DataStore
        dataStoreHelper.saveStartCursor(value)
    }




//    fun increaseCDifCoreTracking() {
//        if (startCursor != null) { // 当下核心事务的计时正在进行
//            val now = LocalDateTime.now()
//            coreDuration.value += Duration.between(startCursor!!, now)
//            startCursor = now
//        }
//    }

    private suspend fun updateDeltaSum(delta: Duration?) {
        dataStoreHelper.saveDeltaSum(delta)
    }

    suspend fun updateCoreDurationOnDragStopped(
        updatedEvent: Event, // 滑动操作项
        originalDuration: Duration?,
    ) {
        if (isCoreEvent(updatedEvent.name)) {
            if (updatedEvent.duration != null) {
                // 1. 滑动的是 stored 的已经终结的核心事务；
                updateCDonStoredItem(updatedEvent.duration!!, originalDuration!!)
            } else {
                // 2. 滑动的是 stored 或正在进行的主核心事务
                updateCoreDuration(updatedEvent.id, start = updatedEvent.startTime)
            }
        }

        if (updatedEvent.parentEventId != null) {
            // 3. 滑动的是正在进行的子事项的开始时间
            updateCoreDuration(updatedEvent.parentEventId!!, currentSubEventST = updatedEvent.startTime)
        }

    }

    private fun updateCDonStoredItem(updatedDuration: Duration, originalDuration: Duration) {
        coreDuration.value += updatedDuration - originalDuration
    }

    suspend fun updateCDonNameChangeConfirmed(
        previousName: String,
        presentName: String, // 点击确认后的事项名称
        event: Event, // 点击确认后的事项
    ) {
        val isCoreNow = isCoreEvent(presentName)
        val isCorePrevious = isCoreEvent(previousName)
        // 之前的核心事务正在进行，现在改成别的事项了
        val coreToOther = isCorePrevious && !isCoreNow
        // 现在是核心事务，但之前不是，应该加上
        val otherToCore = isCoreNow && !isCorePrevious

        if (event.duration != null) {
            // 修改的是 stored 的已经终结的事项名称
            if (coreToOther) reduceDuration(event.duration!!)
            if (otherToCore) increaseDuration(event.duration!!)
        } else {
            // 修改的是还在进行中的事件的名称（包括已经入库但还在进行中的主事件）
            if (coreToOther) {
                // 获取当前正在进行的主事项从开始到现在一共收集到的变化量的总和，也就是它加了多少，就减去多少
                val deltaSum = dataStoreHelper.deltaSumFlow.first()

                reduceDuration(deltaSum)
                updateStartCursor(null)
            }

            if (otherToCore) {
                updateCoreDuration(event.id, start = event.startTime)
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
            updateStartCursor(null)
            updateDeltaSum(null)
        }
    }

    fun reduceCDonCurrentCancel(currentST: LocalDateTime, isCoreEvent: Boolean = false) {
        if (isCoreEvent) {
            val duration = Duration.between(currentST, LocalDateTime.now())
            coreDuration.value -= duration
        }
    }


    /**
     * 这个函数是 onConfirmed 的一般分支处理的子分支，没有经过点击的一些其他可能的情况。
     */
    private suspend fun otherHandle(currentEvent: Event): Event? {

        fun isSleepEvent(startTime: LocalDateTime): Boolean {
            return sleepNames.contains(newEventName) && isSleepingTime(startTime.toLocalTime())
        }

        currentEvent.let {
            if (isCoreEvent(newEventName)) { // 当下核心事务
                updateStartCursor(currentEvent.startTime)
            }

            RDALogger.info("isSleepEvent = ${isSleepEvent(currentEvent.startTime)}")
            if (isSleepEvent(currentEvent.startTime)) { // 晚睡
                // 更新或存储当下核心事务的总值
                repository.saveCoreDurationForDate(getAdjustedEventDate(), coreDuration.value)
                updateSaveCDFlag(true)
            }

            return updateCEonMinutesTail(it) // 末尾两位数字
        }
    }

    suspend fun otherHandle(
        currentEvent: Event,
        continueHandle: suspend (newCurrent: Event) -> Unit
    ): Unit? {
        val newCurrent = otherHandle(currentEvent)

        return if (newCurrent != null) {
            continueHandle(newCurrent)
        } else null
    }


    /**
     * @return 返回的是 currentEvent。
     * 1. 如果输入名称的末尾没有两位分钟数，直接返回 null（以做区分），后期不做处理；
     * 2. 如果有的话，就返回更新了 endTime 和 duration 值的 currentEvent，后面要停止事件！
     */
    private fun updateCEonMinutesTail(currentEvent: Event): Event? {

        fun extractMinutes(text: String): Long? {
            val regex = "\\d{2}$".toRegex()
            val matchResult = regex.find(text)
            return matchResult?.value?.toLong()
        }

        val minutes = extractMinutes(newEventName)

        if (minutes == null || isSubEventTracking) return null // 末尾没有两位数字或是子事件，直接返回

        currentEvent.let {
            val endTime = it.startTime.plusMinutes(minutes)

            it.name = newEventName.replace("$minutes", "")
            it.endTime = endTime
            it.duration = Duration.ofMinutes(minutes)


            RDALogger.info("返回的 currentEvent = $it")
            return it
        }
    }

    suspend fun resetCoreDuration() {
        // 只有在自定义的起床时间和保存了 CD 之后才能进行清零
        if (isGetUpTime(LocalTime.now()) && saveCoreDurationFlag) {
            coreDuration.value = Duration.ZERO
            updateSaveCDFlag(false)
        }
    }

    fun clearCD(displayText: String) {
        if (displayText.isEmpty()) return

        coreDuration.value = Duration.ZERO
        sharedState.toastMessage.value = "已清空重置"
    }

    private suspend fun updateSaveCDFlag(value: Boolean) {
        saveCoreDurationFlag = value
        dataStoreHelper.saveCoreDurationFlag(value)
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