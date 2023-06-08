package com.huaguang.flowoftime

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.data.SPHelper
import com.huaguang.flowoftime.ui.screens.event_tracker.EventTrackerScreen
import com.huaguang.flowoftime.ui.screens.event_tracker.EventTrackerScreenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: EventTrackerScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val myApplication = application as TimeStreamApplication
            val database = myApplication.database
            val eventDao = database.eventDao()
            val dateDurationDao = database.dateDurationDao()
            val repository = EventRepository(eventDao, dateDurationDao)
            val sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
            val spHelper = SPHelper(sharedPreferences)

            EventTrackerScreen(viewModel = viewModel)
            
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateCoreDuration()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveState()
    }

}

