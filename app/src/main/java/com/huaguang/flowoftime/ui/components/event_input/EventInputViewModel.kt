package com.huaguang.flowoftime.ui.components.event_input

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.sleepNames
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import com.huaguang.flowoftime.utils.isCoreEvent
import com.huaguang.flowoftime.utils.isSleepingTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EventInputViewModel @Inject constructor(
    private val repository: EventRepository,
    application: TimeStreamApplication
) : AndroidViewModel(application) {


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

    private fun generalHandleFromNotClicked() {

        fun isSleepEvent(startTime: LocalDateTime): Boolean {
            return sleepNames.contains(newEventName.value) && isSleepingTime(startTime.toLocalTime())
        }

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



}