package com.huaguang.flowoftime.other.event_name

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.SharedState
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.other.SelectionTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventNameViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    var beModifiedEvent: Event? by mutableStateOf(null)
    val previousName = mutableStateOf("")
    val clickedTracker = SelectionTracker()

    // 这是修改项的点击状态（唯一的，不适用于全部 name）
    val beModifiedItemNameClicked = derivedStateOf {
        // 状态没存入 Map 和 beModifiedEvent 为空都为 false
        beModifiedEvent?.let { clickedTracker.isSelected(it.id) } ?: false
    }

    fun onNameTextClicked(event: Event) {
        if (event.name == "起床") { // 起床项的名称禁止更改
            sharedState.toastMessage.value = "起床项名称禁止修改！"
            return
        }

        previousName.value = event.name
        sharedState.newEventName.value = event.name
        beModifiedEvent = event
        // 点击的事项条目的状态会被设为 true
        clickedTracker.toggleSelection(event.id)

        sharedState.isInputShow.value = true
    }


    suspend fun updateNameChangedToDB() {
        beModifiedEvent!!.let {
            it.name = sharedState.newEventName.value

            repository.updateEvent(it)
        }
    }

    suspend fun delayReset() {
        Log.i("打标签喽", "延迟结束，子弹该停停了！")
        delay(500)
        beModifiedEvent = null
        clickedTracker.clearSelection()
    }

    fun onGetUpTextClickThenConfirmed() {
        if (beModifiedEvent != null) { // 来自 item 名称的点击，一定不为 null
            Log.i("打标签喽", "起床处理，item 点击！！！")
            viewModelScope.launch {
                beModifiedEvent!!.name = "起床"

                repository.updateEvent(beModifiedEvent!!)

                delayReset()
            }
        }
    }

}