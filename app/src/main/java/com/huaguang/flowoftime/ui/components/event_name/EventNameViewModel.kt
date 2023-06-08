package com.huaguang.flowoftime.ui.components.event_name

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.other.NameClickedTracker
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.isCoreEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class EventNameViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    application: TimeStreamApplication
) : AndroidViewModel(application) {

    var beModifiedEvent: Event? by mutableStateOf(null)
    val clickedTracker = NameClickedTracker()
    var coreNameClickFlag = mutableStateOf(false)
    val isNameClicked = derivedStateOf {
        Log.i("打标签喽", "beModifiedEvent = $beModifiedEvent")
        val isClicked = beModifiedEvent?.let { clickedTracker.isSelected(it.id) } ?: false
        Log.i("打标签喽", "isClicked = $isClicked")
        isClicked
    }

    fun onNameTextClicked(event: Event) {
        if (event.name == "起床") { // 起床项的名称禁止更改
            Toast.makeText(getApplication(), "起床项名称禁止修改！", Toast.LENGTH_SHORT).show()
            return
        }

        if (isCoreEvent(event.name)) {
            coreNameClickFlag.value = true
        }

        sharedState.newEventName.value = event.name
        // 点击的事项条目的状态会被设为 true
        clickedTracker.toggleSelection(event.id)
        beModifiedEvent = event

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

}