package com.huaguang.flowoftime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.huaguang.flowoftime.viewModel.EventsViewModel
import com.huaguang.flowoftime.views.EventTrackerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val myApplication = application as TimeStreamApplication
            val database = myApplication.database
            val eventDao = database.eventDao()
            val viewModel = EventsViewModel(eventDao)
            EventTrackerScreen(viewModel = viewModel)
            
        }
    }
}

