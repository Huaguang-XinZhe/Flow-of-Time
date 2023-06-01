package com.huaguang.flowoftime

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.viewmodels.EventsViewModel
import com.huaguang.flowoftime.viewmodels.EventsViewModelFactory
import com.huaguang.flowoftime.views.EventTrackerScreen

class MainActivity : ComponentActivity() {

    private var viewModel: EventsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val myApplication = application as TimeStreamApplication
            val database = myApplication.database
            val eventDao = database.eventDao()
            val repository = EventRepository(eventDao)
            val sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
            val spHelper = SPHelper(sharedPreferences)

            val eVM by viewModels<EventsViewModel> {
                EventsViewModelFactory(repository, spHelper, myApplication)
            }
            viewModel = eVM

            Log.i("打标签喽", "eventCount = ${viewModel!!.eventCount}")

            EventTrackerScreen(viewModel = viewModel!!)
            
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel?.saveState()
        Log.i("打标签喽", "onStop：${viewModel?.currentEventState?.value}")
    }

}

