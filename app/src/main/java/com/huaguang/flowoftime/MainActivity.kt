package com.huaguang.flowoftime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.WindowCompat
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.MyApp
import com.huaguang.flowoftime.ui.components.EventTrackerMediator
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.ui.components.current_item.CurrentItemViewModel
import com.huaguang.flowoftime.ui.components.duration_slider.DurationSliderViewModel
import com.huaguang.flowoftime.ui.components.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.components.event_name.EventNameViewModel
import com.huaguang.flowoftime.ui.components.header.HeaderViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Injected dependencies
    @Inject
    lateinit var repository: EventRepository
    @Inject
    lateinit var spHelper: SPHelper
    @Inject
    lateinit var sharedState: SharedState


    // Injected ViewModels
    private val headerViewModel: HeaderViewModel by viewModels()
    private val durationSliderViewModel: DurationSliderViewModel by viewModels()
    private val eventNameViewModel: EventNameViewModel by viewModels()
    private val eventButtonsViewModel: EventButtonsViewModel by viewModels()
    private val currentItemViewModel: CurrentItemViewModel by viewModels()

    private lateinit var mediator: EventTrackerMediator



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediator = EventTrackerMediator(
            headerViewModel,
            durationSliderViewModel,
            eventButtonsViewModel,
            currentItemViewModel,
            eventNameViewModel,
            repository,
            spHelper,
            sharedState,
        )

        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {


            MyApp(mediator)

        }

    }

    override fun onResume() {
        super.onResume()

        mediator.increaseCDonResume()
    }

    override fun onStop() {
        super.onStop()
        mediator.saveState()
    }

}

