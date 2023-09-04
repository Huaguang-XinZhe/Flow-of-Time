package com.huaguang.flowoftime.pages.time_record.recording_event_item

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.huaguang.flowoftime.ui.components.EventDisplay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RecordingEventItemViewModel @Inject constructor(): ViewModel() {

    private val expandStates = mutableStateMapOf<EventDisplay, MutableState<Boolean>>()

    fun getExpandStateFor(event: EventDisplay): MutableState<Boolean> {
        return expandStates.getOrPut(event) { mutableStateOf(false) }
    }

}