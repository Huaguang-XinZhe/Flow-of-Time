package com.huaguang.flowoftime.other

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.other.current_item.CurrentItemViewModel
import com.huaguang.flowoftime.other.duration_slider.DurationSliderViewModel
import com.huaguang.flowoftime.other.event_name.EventNameViewModel
import com.huaguang.flowoftime.other.header.HeaderViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.DNDManager
import com.huaguang.flowoftime.utils.isCoreEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration

class EventTrackerMediator(
    val headerViewModel: HeaderViewModel,
    val durationSliderViewModel: DurationSliderViewModel,
    private val eventButtonsViewModel: EventButtonsViewModel,
    val currentItemViewModel: CurrentItemViewModel,
    val eventNameViewModel: EventNameViewModel,
    private val repository: EventRepository,
    private val spHelper: SPHelper,
    private val dndManager: DNDManager,
    val sharedState: SharedState,
) : ViewModel() {

    // 依赖的共享状态
    private val currentStatus
        get() = sharedState.eventStatus.value
    private val newEventName
        get() = sharedState.newEventName.value

    // 依赖子 ViewModel 的状态
    private val beModifiedEvent
        get() = eventNameViewModel.beModifiedEvent

    private var currentEvent // 访问其他类的属性一定要用 getter 方法，否则，获取的始终是初始获取值。
        get() = currentItemViewModel.currentEvent.value
        set(value) {
            currentItemViewModel.currentEvent.value = value
        }

    // 辅助构成函数逻辑
    private var updateJob: Job? = null
    val initialized = mutableStateOf(false)
    val dragTracker = SelectionTracker()
    private val dismissedItems = mutableSetOf<Long>() // 为了防止删除逻辑多次执行

    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsWithSubEvents = headerViewModel.isOneDayButtonClicked.flatMapLatest { clicked ->
        if (clicked) {
            RDALogger.info("oneDay")
            repository.getCustomTodayEvents()
        } else {
            RDALogger.info("recentTwoDays")
            repository.getRecentTwoDaysEvents()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), listOf())

    init {
        viewModelScope.launch {
            initialized.value = true

            eventButtonsViewModel.restoreButtonShow()
            durationSliderViewModel.resetCoreDuration()
//            repository.generateDurationStr()
        }

    }

//    fun increaseCDonResume() {
//        viewModelScope.launch {
//            durationSliderViewModel.apply {
//                if (currentStatus == EventStatus.fromInt(1)) {
//                    // 仅有主事务正在进行
//                    RDALogger.info("resume: 仅有主事务正在进行！")
//                    updateCoreDuration(currentEvent!!.id)
//                } else if (currentStatus == EventStatus.fromInt(2)) {
//                    // 同时有子事务正在计时
//                    RDALogger.info("resume: 同时有子事务正在进行！")
//                    val mainEventId = currentEvent!!.parentEventId!!
//                    val currentST = currentEvent!!.startTime
//
//                    updateCoreDuration(mainEventId, currentSubEventST = currentST)
//                }
//            }
//        }
//    }

    fun deleteItem(event: Event, subEvents: List<Event> = listOf()) {
        if (dismissedItems.contains(event.id)) return

        if (event.duration != null) { // 删除 stored 且已经终结的事项
            Log.i("打标签喽", "删除已经入库的条目")
            viewModelScope.launch {
                repository.deleteEventWithSubEvents(event, subEvents)
            }

            if (isCoreEvent(event.name)) {
                durationSliderViewModel.reduceDuration(event.duration!!)
            }
        }

        // 在删除完成后，将该 id 添加到已删除项目的记录器中
        dismissedItems.add(event.id)
    }

    fun onConfirmed() {
        sharedState.apply {
            when(newEventName.value) {
                "" -> {
                    sharedState.toastMessage.value = "你还没有输入呢？"
                    return
                }
                "起床" -> { // 起床事件的特殊应对
                    getUpHandle()
                }
                // TODO: 不固定，可配置
                "睡" -> {
                    viewModelScope.launch {
                        RDALogger.info("睡代码块执行！")
                        if (dndManager.hasNotificationPolicyAccess()) {
                            dndManager.openDND() // 开启免打扰
                        } else {
                            toastMessage.value = "需要勿扰控制权限才能\n为您自动开启、关闭勿扰模式"
                            delay(200)
                            dndManager.jumpAuth() // 跳到设置页授权，接下来的事儿就交给 MainActivity 的 onResume 回调了
                        }

                        currentEvent?.let {
                            it.name = "睡"
                        }
                    }
                }
                else -> {
                    Log.i("打标签喽", "一般情况执行！！！")
                    generalHandle()
                }
            }
            isInputShow.value = false
        }
    }

    /**
     * 这里边是两个分支，分为点击和没点击
     */
    private fun getUpHandle() {
        if (beModifiedEvent == null) { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
            Log.i("打标签喽", "起床处理，一般流程")
            viewModelScope.launch {
                currentEvent?.let {
                    it.name = "起床"

                    repository.insertEvent(it)
                }

                eventButtonsViewModel.updateStateOnGetUpConfirmed()

                currentEvent = null
            }
        }

        eventNameViewModel.onGetUpTextClickThenConfirmed()
    }

    private fun generalHandle() { // 确认时文本不为空也不是 ”起床“
        viewModelScope.launch {
            if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null（事件可能在进行中）
                generalHandleFromNameClicked()
            } else { // 来自一般流程，事件名称没有得到点击（此时事项一定正在进行中）
                currentEvent?.let {
//                    subProhibitCore() // 禁止子事项输入核心事务

//                    durationSliderViewModel.otherHandle(it) {newCurrent ->
//                        stopEventOnConfirmed(newCurrent)
//                    } ?: run {
//                        // newCurrent 为 null，即末尾没有两位分钟数时才会执行这一段代码
//                        currentEvent = it.copy(name = newEventName)
//                    }
                }
            }
        }
    }

    private suspend fun stopEventOnConfirmed(newCurrent: Event) {
        currentEvent = newCurrent

        currentItemViewModel.apply {
            saveCurrentEvent()
            hideCurrentItem()
        }
        eventButtonsViewModel.toggleStateOnMainStop()
    }

    /**
     * 插入的子事项输入名称后点击确认，会执行此方法进行验证，以确保子事项不进行当下核心事务（包括名称修改）
     */
//    private suspend fun subProhibitCore() {
//        if (isCoreEvent(newEventName) &&
//            currentStatus == EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS) {
//            sharedState.toastMessage.value = "禁止在子事务中执行核心事务！"
//
//            eventButtonsViewModel.toggleStateOnSubStop()
//
//            currentItemViewModel.restoreOnMainEvent()
//        }
//    }

    fun updateOnDragStopped(updatedEvent: Event, originalDuration: Duration?) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(2000) // Wait for 2 seconds

            // 1. 更新 startTime 的 UI——————————————————————————————————————
            // 更新 stored 型事项
            repository.updateEvent(updatedEvent)
            // 如果操作项是当前项，那就更新它的 startTime（没入库，所以要这么做！）
//            currentItemViewModel.updateCurrentST(updatedEvent)
            dragTracker.cancelSelection(updatedEvent.id) // 取消滑块阴影，禁止点击

            // 2. 更新核心事务持续时间的 UI—————————————————————————————————————
            durationSliderViewModel.updateCoreDurationOnDragStopped(
                updatedEvent = updatedEvent,
                originalDuration = originalDuration
            )

            sharedState.toastMessage.value = "调整已更新！"
        }
    }

    private suspend fun generalHandleFromNameClicked() {
        eventNameViewModel.apply {
            updateNameChangedToDB()

            durationSliderViewModel.updateCDonNameChangeConfirmed(
                previousName = previousName.value,
                presentName = newEventName,
                event = beModifiedEvent!!,
            )

            // 延迟一下，让边框再飞一会儿
            delayReset()
        }
    }

//    private fun startNewEvent(startTime: LocalDateTime = LocalDateTime.now()) {
//        viewModelScope.launch {
//            currentItemViewModel.apply {
//                // 通用状态更新
//                sharedState.updateStateOnStart()
//
//                if (currentStatus == EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS) {
//                    // 开始主事项
//                    clearSubEventCount()
//                } else if (currentStatus == EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS) {
//                    // 开始子事项
//                    increaseSubEventCount() // 放在这里好些，虽然不是在点击的源头
//                    saveInCompleteMainEvent() // 首次插入时保存
//                }
//
//                // 通用创建，但必须放在后边，尤其是对于子事项（需要先保存当前主事件，然后再创建新事件）
////                currentEvent.value = createCurrentEvent(startTime)
//            }
//        }
//
//    }

//    private suspend fun stopCurrentEvent() {
//        currentItemViewModel.apply {
//            // 通用逻辑
//            updateCurrentEventOnStop()
//            saveCurrentEvent() // 插入或更新到数据库
//
//            if (currentStatus == EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS) {
//                // 停止主事项——————————————————————————————————————————————————
//                // 先把 currentEvent 记录下来，隐藏后再更新 CoreDuration，要不然可能出现异常！！！
//                val currentRecord = currentEvent.value?.copy()
//                hideCurrentItem()
//                // 使用 let 块更安全，这里有必要，为避免各种意外的崩溃情况！！！
//                currentRecord?.let { durationSliderViewModel.updateCDonCurrentStop(it) }
//                dndManager.closeDND() // 如果之前开启了免打扰的话，现在关闭
//            } else if (currentStatus == EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS) {
//                // 停止子事项————————————————————————————————————————————————
//                restoreOnMainEvent()
//            }
//        }
//
//    }

    /**
     * 撤销或删除时执行的方法，回到它处理后该有的状态。
     */
//    fun resetState(isCoreEvent: Boolean = false) {
//        // 输入状态
////        sharedState.resetInputState()
//
//        if (currentStatus == EventStatus.ONLY_MAIN_EVENT_IN_PROGRESS) {
//            // 撤销或删除主事项
//            eventButtonsViewModel.toggleStateOnMainStop() // 按钮状态
//            currentItemViewModel.hideCurrentItem() // CurrentItem 状态
//
//            // 更新 CoreDuration
//            durationSliderViewModel.reduceCDonCurrentCancel(
//                currentEvent!!.startTime,
//                isCoreEvent
//            )
//        } else if (currentStatus == EventStatus.MAIN_AND_SUB_EVENT_IN_PROGRESS) {
//            // 撤销或删除子事项
//            eventButtonsViewModel.toggleStateOnSubStop()
//            viewModelScope.launch {
//                currentItemViewModel.restoreOnMainEvent()
//            }
//        }
//
//    }


}
