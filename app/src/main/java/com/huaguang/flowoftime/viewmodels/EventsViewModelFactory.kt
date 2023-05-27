package com.huaguang.flowoftime.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper

class EventsViewModelFactory(
    private val repository: EventRepository,
    private val spHelper: SPHelper,
    private val application: TimeStreamApplication
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventsViewModel(repository, spHelper, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
