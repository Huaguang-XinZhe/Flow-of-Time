package com.huaguang.flowoftime.other.current_item

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class CurrentItemViewModel @Inject constructor(
    private val repository: EventRepository,
    private val dataStoreHelper: DataStoreHelper,
) : ViewModel() {

    // 共享依赖

    val currentEvent: MutableState<Event?> =  mutableStateOf(null)
    private var subEventCount = 0


    init {
        viewModelScope.launch {
            subEventCount = dataStoreHelper.subEventCountFlow.first()
            RDALogger.info("subEventCount = $subEventCount")
        }
    }


    suspend fun restoreOnMainEvent(fromDelete: Boolean = false) {
        Log.i("打标签喽", "结束的是子事件")
        val incompleteMainEvent = repository.getLastMainEvent()!!

        if (!fromDelete) {
            currentEvent.value?.let { // 这么写是为了不引起重组
                it.id = incompleteMainEvent.id
                it.startTime = incompleteMainEvent.startTime
                it.name = incompleteMainEvent.name
                it.endTime = LocalDateTime.MIN // 为优化显示，实际业务不需要（为不显示当前条目特别设置）
                it.parentEventId = null

            }
        } else {
            currentEvent.value = incompleteMainEvent.copy(
                endTime = LocalDateTime.MIN,

            )
        }

        RDALogger.info("已经执行了恢复到主事件的逻辑：currentEvent.value = ${currentEvent.value}")

    }

    fun hideCurrentItem(fromDelete: Boolean = false) {
        if (fromDelete) {
            currentEvent.value = null
        } else {
            currentEvent.value?.name = "￥为减少重组，优化频闪，不显示的特别设定￥"
        }

    }

    suspend fun updateCurrentEventOnStop() {
        currentEvent.value?.let {
            // 如果是主事件，就计算从数据库中获取子事件列表，并计算其间隔总和
            val subEventsDuration = if (it.parentEventId == null) {
                repository.calculateSubEventsDuration(it.id)
            } else Duration.ZERO

            // 这里就不赋给 currentEventState 的值了，减少不必要的重组
            it.endTime = LocalDateTime.now()
            it.duration = Duration.between(it.startTime, it.endTime).minus(subEventsDuration)
        }
    }

    /**
     * 将更新后的当前项插入或更新到数据库
     */
    suspend fun saveCurrentEvent() {
        currentEvent.value?.let {
            repository.insertEvent(it) // 如果有冲突，则自动替换
        }
    }

//    fun updateCurrentST(updatedEvent: Event) {
//        if (updatedEvent.isCurrent) { // 处理 currentItem
//            currentEvent.value?.startTime = updatedEvent.startTime
//        }
//    }

    /**
     * 这个方法将未完成的主事件存入了数据库
     */
    suspend fun saveInCompleteMainEvent() {
        if (subEventCount == 1) { // 首次点击插入按钮
            RDALogger.info("首次点击插入按钮，存储主事件")
            repository.insertEvent(currentEvent.value!!)
        }
    }

    suspend fun increaseSubEventCount() {
        dataStoreHelper.saveSubEventCount(++subEventCount) // 注意这里要放在前边，加一再返回！！！
    }

    suspend fun clearSubEventCount() {
        if (subEventCount == 0) return

        subEventCount = 0
        dataStoreHelper.saveSubEventCount(0)
    }






}