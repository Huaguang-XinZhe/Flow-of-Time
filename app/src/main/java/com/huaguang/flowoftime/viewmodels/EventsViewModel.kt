package com.huaguang.flowoftime.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.ALARM_CANCELLATION_THRESHOLD
import com.huaguang.flowoftime.ALARM_SETTING_THRESHOLD
import com.huaguang.flowoftime.DEFAULT_EVENT_INTERVAL
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.FOCUS_EVENT_DURATION_THRESHOLD
import com.huaguang.flowoftime.ItemSelectionTracker
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.Event
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.sleepNames
import com.huaguang.flowoftime.utils.AlarmHelper
import com.huaguang.flowoftime.utils.copyToClipboard
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.getEventDate
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.utils.isSleepingTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class EventsViewModel(
    private val repository: EventRepository,
    private val spHelper: SPHelper,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    private val eventDao = repository.eventDao

    val isOneDayButtonClicked = MutableStateFlow(false)
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithSubEvents = isOneDayButtonClicked.flatMapLatest { clicked ->
        if (clicked) {
            repository.getCustomTodayEvents()
        } else {
            repository.getRecentTwoDaysEvents()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    private val isTracking = mutableStateOf(false)
    val currentEvent: MutableState<Event?> =  mutableStateOf(null)
    private var incompleteMainEvent: Event? by mutableStateOf(null)
    private var beModifiedEvent: Event? by mutableStateOf(null)

    val isInputShow = mutableStateOf(false)
    val newEventName = mutableStateOf("")

    // 底部按钮相关——————————————————————————————————👇
    val mainEventButtonText = mutableStateOf("开始")
    val subEventButtonText = mutableStateOf("插入")
    val mainButtonShow = MutableLiveData(true)
    val subButtonShow = MutableLiveData(false)
    private var subButtonClickCount = 0
    private var isLastStopFromSub = false
    // 底部按钮相关——————————————————————————————————👆

    val scrollIndex = mutableStateOf(0)
    var eventCount = 0

    private val alarmHelper = AlarmHelper(application)
    val isAlarmSet = MutableLiveData(false)

    @SuppressLint("MutableCollectionMutableState")
    val selectedEventIdsMap = mutableStateOf(mutableMapOf<Long, Boolean>())
    val isEventNameNotClicked = derivedStateOf {
        beModifiedEvent?.let { selectedEventIdsMap.value[it.id] == null } ?: true
    }

//    val pager = Pager(
//        PagingConfig(pageSize = 25)
//    ) { eventDao.getAllEvents() }.flow

    val coreDuration = mutableStateOf(Duration.ZERO)
    val rate = derivedStateOf {
        coreDuration.value.toMillis().toFloat() / FOCUS_EVENT_DURATION_THRESHOLD.toMillis()
    }
    private var startTimeTracking: LocalDateTime? = null // 记录正在进行的核心事务的开始时间

    val isImportExportEnabled = MutableLiveData(true)
    private var updateJob: Job? = null
    private val eventType = mutableStateOf(EventType.MAIN)
    val initialized = mutableStateOf(false)
    private var isCoreEventTracking = false
    private var isCoreDurationReset = false
    private var coreNameClickedFlag = false
    private val dismissedItems = mutableSetOf<Long>() // 为了防止删除逻辑多次执行
    val selectionTracker = ItemSelectionTracker()

    init {
        viewModelScope.launch {
            retrieveStateFromSP() // 恢复相关状态

            restoreButtonShow()

            initialized.value = true
        }

        if (!isCoreDurationReset) {
            coreDuration.value = Duration.ZERO
            isCoreDurationReset = true
        }

        updateCoreDuration()

//        // 目前主要是重置 coreDuration
//        resetStateIfNewDay()
    }

    fun updateCoreDuration() {
        if (startTimeTracking != null) { // 当下核心事务的计时正在进行
            Log.i("打标签喽", "updateCoreDuration 执行！！！")
            val now = LocalDateTime.now()
            coreDuration.value += Duration.between(startTimeTracking!!, now)
            startTimeTracking = now
        }
    }

    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态
    }

    fun updateTimeAndState(updatedEvent: Event, originalDuration: Duration?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds

            withContext(Dispatchers.IO) {// 处理非 currentItem
                eventDao.updateEvent(updatedEvent)
            }

            Toast.makeText(getApplication(), "调整已更新！", Toast.LENGTH_SHORT).show()

            selectionTracker.cancelSelection(updatedEvent.id) // 取消滑块阴影，禁止点击

            if (isCoreEvent(updatedEvent.name)) { // 更新当下核心事务的持续时间
                if (updatedEvent.duration != null) {
                    coreDuration.value += updatedEvent.duration!! - originalDuration!!
                } else {
                    Log.i("打标签喽", "正在计时！！！")
                    coreDuration.value -=
                        Duration.between(currentEvent.value!!.startTime, updatedEvent.startTime)
                }

            }

            if (updatedEvent.isCurrent) { // 处理 currentItem
                currentEvent.value?.startTime = updatedEvent.startTime
            }

        }
    }

    fun exportEvents() {
        if (isImportExportEnabled.value == true) {
            viewModelScope.launch {
                val exportText = repository.exportEvents()
                copyToClipboard(getApplication(), exportText)
                Toast.makeText(getApplication(), "导出数据已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importEvents(text: String) {
        Toast.makeText(getApplication(), "导入成功！", Toast.LENGTH_SHORT).show()
    }



    fun onNameTextClicked(event: Event) {
        if (event.name == "起床") { // 起床项的名称禁止更改
            Toast.makeText(getApplication(), "起床项名称禁止修改！", Toast.LENGTH_SHORT).show()
            return
        }

        isInputShow.value = true
        newEventName.value = event.name
        // 点击的事项条目的状态会被设为 true
        toggleSelectedId(event.id)
        beModifiedEvent = event

        if (isCoreEvent(event.name)) {
            coreNameClickedFlag = true
        }
    }

    private fun toggleSelectedId(eventId: Long) {
        val map = selectedEventIdsMap.value.toMutableMap() // 调用这个方法能创建一个新实例！！！
        map[eventId] = !(map[eventId] ?: false)
        selectedEventIdsMap.value = map
    }

    private fun isSleepEvent(startTime: LocalDateTime): Boolean {
        return sleepNames.contains(newEventName.value) && isSleepingTime(startTime.toLocalTime())
    }

    private suspend fun delayReset() {
        Log.i("打标签喽", "延迟结束，子弹该停停了！")
        delay(500)
        beModifiedEvent = null
        selectedEventIdsMap.value = mutableMapOf()
    }

    fun resetState(isCoreEvent: Boolean = false, fromDelete: Boolean = false) {
        // 按钮和currentEvent状态++++++++++++++++++++++++++++++++++++++++
        if (eventType.value == EventType.SUB) {
            toggleSubButtonState("插入结束")
            restoreOnMainEvent(fromDelete)
        } else {
            if (isCoreEvent) {
                val duration = Duration.between(currentEvent.value!!.startTime, LocalDateTime.now())
                coreDuration.value -= duration
            }

            toggleMainButtonState("结束")

            resetCurrentOnMainBranch(fromDelete)
        }

        // 输入状态+++++++++++++++++++++++++++++++++++++++++
        if (isInputShow.value) {
            isInputShow.value = false
            newEventName.value = ""
        }
    }

    fun deleteItem(event: Event, subEvents: List<Event> = listOf()) {
        if (dismissedItems.contains(event.id)) return

        val isCoreEvent = isCoreEvent(event.name)

        if (event.id != 0L) { // 删除项已经存入数据库中了，排除已经插入了子事件的主事件（有点复杂，不处理这样的场景）
            Log.i("打标签喽", "删除已经入库的条目")
            viewModelScope.launch(Dispatchers.IO) {
                eventDao.deleteEvent(event.id)
                for (subEvent in subEvents) {
                    eventDao.deleteEvent(subEvent.id)
                }
            }

            if (isCoreEvent) { // 删除的是当下核心事务
                coreDuration.value -= event.duration
            }
        } else { // 删除的是当前项（正在计时）
            resetState(isCoreEvent, true)
        }

        // 在删除完成后，将该 id 添加到已删除项目的记录器中
        if (event.id != 0L) { // 当前项的 id 始终是 0，就不要加进来限制执行次数了。
            dismissedItems.add(event.id)
        }
    }

    // 恢复和存储 UI 状态————————————————————————————————————————————————————————————————————————————👇

    private suspend fun retrieveStateFromSP() {
        val data = withContext(Dispatchers.IO) {
            spHelper.getAllData()
        }

        // 在主线程中使用取出的数据更新状态
        isOneDayButtonClicked.value = data.isOneDayButtonClicked
        isInputShow.value = data.isInputShow
        mainEventButtonText.value = data.buttonText
        subEventButtonText.value = data.subButtonText
        coreDuration.value = data.coreDuration
        startTimeTracking = data.startTimeTracking
        currentEvent.value = data.currentEvent
        incompleteMainEvent = data.incompleteMainEvent
        subButtonClickCount = data.subButtonClickCount
        eventType.value = if (data.isSubEventType) EventType.SUB else EventType.MAIN
        isLastStopFromSub = data.isLastStopFromSub
        isCoreDurationReset = data.isCoreDurationReset

        if (data.scrollIndex != -1) {
            scrollIndex.value = data.scrollIndex
            eventCount = data.scrollIndex + 1
        }
    }

    fun saveState() {
        viewModelScope.launch(Dispatchers.IO) {
            spHelper.saveState(
                isOneDayButtonClicked.value,
                isInputShow.value,
                mainEventButtonText.value,
                subEventButtonText.value,
                scrollIndex.value,
                isTracking.value,
                isCoreEventTracking,
                coreDuration.value,
                startTimeTracking,
                currentEvent.value,
                incompleteMainEvent,
                subButtonClickCount,
                eventType.value,
                isLastStopFromSub,
                isCoreDurationReset
            )
        }
    }

    // 恢复和存储 UI 状态————————————————————————————————————————————————————————————————————————————👆

    // 关键 UI 逻辑——————————————————————————————————————————————————————————————————————————————————👇

    private fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
        // 重要状态更新—————————————————————————————————————————————————————
        isTracking.value = true
        isInputShow.value = true
        newEventName.value = ""

        viewModelScope.launch {
            if (eventType.value == EventType.SUB) {
                subButtonClickCount++

                if (subButtonClickCount == 1) { // 首次点击插入按钮
                    val id = withContext(Dispatchers.IO) {
                        currentEvent.value?.let { eventDao.insertEvent(it) }
                    }

                    // 存储每个未完成的主事件，以备后边插入的子事件结束后获取
                    incompleteMainEvent = currentEvent.value?.copy(id = id!!)
                }
            } else {
                subButtonClickCount = 0 // 一遇到主事件就清空
            }

            // 获取 parentId，并创建新的事件对象（主、子），更新 currentEvent——————————————————————
            val mainEventId = withContext(Dispatchers.IO) {
                if (eventType.value == EventType.SUB) {
                    eventDao.getLastMainEventId()// 在插入子事件之前一定存在主事件，不会有问题
                } else null
            }

            currentEvent.value = Event(
                startTime = startTime,
                eventDate = getEventDate(startTime),
                parentId = mainEventId,
                isCurrent = true
            )

            // 索引相关—————————————————————————————————————————————————————————
            // 更新事件数量
            eventCount++
            // 更新滚动索引
            scrollIndex.value = eventCount - 1
        }

    }

    fun onConfirm() {
        when (newEventName.value) {
            "" -> {
                Toast.makeText(getApplication(), "你还没有输入呢？", Toast.LENGTH_SHORT).show()
                return
            }
            "起床" -> {
                // 起床事件的特殊应对
                getUpHandle()
            }
            else -> {
                Log.i("打标签喽", "一般情况执行！！！")
                // 一般情况
                generalHandle()
            }
        }

        isInputShow.value = false
    }

    private fun stopCurrentEvent() {
        viewModelScope.launch {
            updateOrInsertCurrentEventToDB()

            updateCoreDurationOnStop()

            //                cancelAlarm()

            resetCurrentEventAndState()

        }

    }

    private fun getUpHandle() {
        viewModelScope.launch {
            if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null
                Log.i("打标签喽", "起床处理，item 点击！！！")
                beModifiedEvent!!.name = "起床"

                withContext(Dispatchers.IO) {
                    eventDao.updateEvent(beModifiedEvent!!)
                }

                delayReset()
            } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
                Log.i("打标签喽", "起床处理，一般流程")
                currentEvent.value?.let { it.name = "起床" }

                withContext(Dispatchers.IO) {
                    currentEvent.value?.let { eventDao.insertEvent(it) }
                }

                // TODO: 这里似乎不需要 isEventNameClicked，是否可以优化呢？
                // 按钮文本直接还原为开始，不需要结束
                mainEventButtonText.value = "开始"
                // 比较特殊，插入按钮不需要显示
                subButtonShow.value = false
                currentEvent.value = null
            }

        }
    }

    private fun generalHandleFromNameClicked() {
        viewModelScope.launch {
            beModifiedEvent!!.let {
                it.name = newEventName.value

                withContext(Dispatchers.IO) {
                    eventDao.updateEvent(it)
                }

                if (isCoreEvent(newEventName.value)) { // 文本是当下核心事务
                    coreDuration.value += it.duration!!
                } else { // 已修改，不是当下核心事务
                    if (coreNameClickedFlag) { // 点击修改之前是当下核心事务
                        coreDuration.value -= it.duration!!
                        coreNameClickedFlag = false
                    }
                }
            }

            // 延迟一下，让边框再飞一会儿
            delayReset()
        }
    }

    private fun generalHandleFromNotClicked() {
        Log.i("打标签喽", "事件输入部分，点击确定，一般流程分支。")
        currentEvent.value?.let {
            if (isCoreEvent(newEventName.value)) { // 文本是当下核心事务
                isCoreEventTracking = true
                startTimeTracking = it.startTime
            }

            if (isSleepEvent(it.startTime)) { // 当前事项是晚睡
                isCoreDurationReset = false

                viewModelScope.launch(Dispatchers.IO) {
                    // 更新或存储当下核心事务的总值
                    repository.updateCoreDurationForDate(getAdjustedEventDate(), coreDuration.value)
                }
            }

            currentEvent.value = it.copy(name = newEventName.value)
        }
    }

    private fun generalHandle() { // 确认时文本不为空也不是 ”起床“
        if (eventType.value == EventType.SUB && isCoreEvent(newEventName.value)) {
            Toast.makeText(getApplication(), "不可在子事务中进行核心事务！", Toast.LENGTH_SHORT).show()
            resetState()
            return
        }

        if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null（事件可能在进行中）
            generalHandleFromNameClicked()
        } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
            generalHandleFromNotClicked()
        }
    }

    private suspend fun updateOrInsertCurrentEventToDB() {
        currentEvent.value?.let {
            // 如果是主事件，就计算从数据库中获取子事件列表，并计算其间隔总和
            val subEventsDuration = if (it.parentId == null) {
                withContext(Dispatchers.IO) {
                    repository.calculateSubEventsDuration(it.id)
                }
            } else Duration.ZERO

            // 这里就不赋给 currentEventState 的值了，减少不必要的重组
            it.endTime = LocalDateTime.now()
            it.duration = Duration.between(it.startTime, it.endTime).minus(subEventsDuration)
            it.isCurrent = false

            withContext(Dispatchers.IO) {
                if (isLastStopFromSub && eventType.value == EventType.MAIN) {
                    Log.i("打标签喽", "结束：更新主事件到数据库！")
                    eventDao.updateEvent(it)
                } else {
                    Log.i("打标签喽", "结束：插入到数据库执行！")
                    eventDao.insertEvent(it)
                }
            }

        }
    }

    private fun updateCoreDurationOnStop() {
        currentEvent.value?.let {
            if (isCoreEvent(it.name)) { // 结束的是当下核心事务
                coreDuration.value += Duration.between(startTimeTracking!!, it.endTime)
                startTimeTracking = null
                isCoreEventTracking = false
            }
        }
    }

    private fun resetCurrentEventAndState() {
        // 子事件结束后恢复到主事件（数据库插入会重组一次，因此这里无需赋值重组）
        if (eventType.value == EventType.SUB) {
            restoreOnMainEvent()
        } else {
            resetCurrentOnMainBranch()
        }

        Log.i("打标签喽", "currentEvent.value = ${currentEvent.value}")
    }

    private fun restoreOnMainEvent(fromDelete: Boolean = false) {
        Log.i("打标签喽", "结束的是子事件")
        if (!fromDelete) {
            currentEvent.value?.let {
                it.id = it.parentId!!
                it.startTime = incompleteMainEvent!!.startTime
                it.name = incompleteMainEvent!!.name
                it.endTime = LocalDateTime.MIN // 为优化显示，实际业务不需要
                it.parentId = null
                it.isCurrent = true
            }
        } else {
            currentEvent.value = incompleteMainEvent!!.copy(
                endTime = LocalDateTime.MIN,
                isCurrent = true
            )
        }

        isLastStopFromSub = true
        eventType.value = EventType.MAIN // 必须放在 stop 逻辑中
    }

    private fun resetCurrentOnMainBranch(fromDelete: Boolean = false) {
        Log.i("打标签喽", "结束的是主事件")
        if (fromDelete) {
            currentEvent.value = null
        } else { // 本来应该为 null，这里是为了优化显示
            currentEvent.value?.name = "￥为减少重组，优化频闪，不显示的特别设定￥"
        }

        isLastStopFromSub = false
        isTracking.value = false
    }

    // 关键 UI 逻辑——————————————————————————————————————————————————————————————————————————————————👆

    // coreDuration 和闹钟相关—————————————————————————————————————————————————————————————————👇

    private fun resetStateIfNewDay() {
       viewModelScope.launch {
           val events = eventsWithSubEvents.first()
           if (events.isEmpty()) {
               Log.i("打标签喽", "coreDuration 置空执行了。")
               coreDuration.value = null
           }
       }
    }

    private suspend fun setCoreDuration() {
        coreDuration.value = if (coreDuration.value == null) {
            Log.i("打标签喽", "setCoreDuration 块内：currentEvent = $currentEvent")
            // 数据库操作，查询并计算
            val totalDuration = repository.calEventDateDuration(
                currentEvent.value?.eventDate ?: LocalDate.now()
            )
            FOCUS_EVENT_DURATION_THRESHOLD.minus(totalDuration)
        } else coreDuration.value
    }

    private fun checkAndSetAlarm(name: String) {
        if (!isCoreEvent(name)) return

        if (coreDuration.value < ALARM_SETTING_THRESHOLD) {
            // 一般事务一次性持续时间都不超过 5 小时
            alarmHelper.setAlarm(coreDuration.value!!.toMillis())
            isAlarmSet.value = true
        }
    }

    private fun cancelAlarm() {
        currentEvent.value?.let {
            if (coreDuration.value != null && isCoreEvent(it.name)) {
                coreDuration.value = coreDuration.value?.minus(it.duration)

                if (isAlarmSet.value == true &&
                    coreDuration.value!! > ALARM_CANCELLATION_THRESHOLD) {
                    alarmHelper.cancelAlarm()
                    isAlarmSet.value = false
                }
            }
        }
    }

    // coreDuration 和闹钟相关—————————————————————————————————————————————————————————————————👆


    // 底部按钮相关————————————————————————————————————————————————————————————————————————————👇

    fun onMainButtonLongClick() {
        if (mainEventButtonText.value == "结束") return

        // ButtonText 的值除了结束就是开始了，不可能为 null
        viewModelScope.launch {
            val lastEvent = withContext(Dispatchers.IO) {
                eventDao.getLastMainEvent() // 这个数据库操作是必需的
            }
            val startTime = lastEvent.endTime?.plus(DEFAULT_EVENT_INTERVAL)
                ?: lastEvent.startTime.plus(DEFAULT_EVENT_INTERVAL)

            startNewEvent(startTime = startTime)
            toggleMainButtonState("开始")
        }

        Toast.makeText(getApplication(), "开始补计……", Toast.LENGTH_SHORT).show()
    }

    fun onSubButtonLongClick() {
        viewModelScope.launch {
            // 结束子事件————————————————————————————————————————————————————
            updateOrInsertCurrentEventToDB()

            restoreOnMainEvent()

            toggleSubButtonState("插入结束")

            // 结束主事件——————————————————————————————————————————————————————————
            toggleMainButtonState("结束")

            stopCurrentEvent()

            Toast.makeText(getApplication(), "全部结束！", Toast.LENGTH_SHORT).show()

        }
    }

    private fun restoreButtonShow() {
        if (mainEventButtonText.value == "结束") {
            if (subEventButtonText.value == "插入结束") {
                Log.i("打标签喽", "插入结束部分恢复！")
                subButtonShow.value = true
                mainButtonShow.value = false
            } else {
                subButtonShow.value = true
            }
        }

    }

    fun toggleMainEvent() {
        when (mainEventButtonText.value) {
            "开始" -> {
                toggleMainButtonState("开始")
                startNewEvent()
            }
            "结束" -> {
                toggleMainButtonState("结束")
                stopCurrentEvent()
            }
        }
    }

    fun toggleSubEvent() {
        when (subEventButtonText.value) {
            "插入" -> {
                toggleSubButtonState("插入") // 这个必须放在前边，否则 start 逻辑会出问题
                startNewEvent()
            }
            "插入结束" -> {
                stopCurrentEvent()
                toggleSubButtonState("插入结束")
            }
        }
    }

    private fun toggleMainButtonState(buttonText: String) {
        when (buttonText) {
            "开始" -> {
                mainEventButtonText.value = "结束"
                subButtonShow.value = true
                isImportExportEnabled.value = false
            }
            "结束" -> {
                mainEventButtonText.value = "开始"
                subButtonShow.value = false
                isImportExportEnabled.value = true
            }
        }
    }

    private fun toggleSubButtonState(buttonText: String) {
        when (buttonText) {
            "插入" -> {
                eventType.value = EventType.SUB
                subEventButtonText.value = "插入结束"
                mainButtonShow.value = false
            }
            "插入结束" -> {
                // 不能放在这里，stop 里边的协程会挂起，这一段会先执行，必须放入 stop 里边
//                eventTypeState.value = EventType.MAIN
                subEventButtonText.value = "插入"
                mainButtonShow.value = true
            }
        }
    }

    // 底部按钮相关————————————————————————————————————————————————————————————————————————————👆

}
