package com.huaguang.flowoftime.other

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.models.SharedState
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.other.current_item.CurrentItemViewModel
import com.huaguang.flowoftime.other.duration_slider.DurationSliderViewModel
import com.huaguang.flowoftime.other.event_name.EventNameViewModel
import com.huaguang.flowoftime.other.event_tracker.EventTrackerScreen
import com.huaguang.flowoftime.other.header.HeaderViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.utils.DNDManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Injected dependencies
    @Inject
    lateinit var repository: EventRepository
    @Inject
    lateinit var sharedState: SharedState
    @Inject
    lateinit var iconRepository: IconMappingRepository
    lateinit var dndManager: DNDManager

    // Injected ViewModels
    private val headerViewModel: HeaderViewModel by viewModels()
    private val durationSliderViewModel: DurationSliderViewModel by viewModels()
    private val eventNameViewModel: EventNameViewModel by viewModels()
    private val eventButtonsViewModel: EventButtonsViewModel by viewModels()
    private val currentItemViewModel: CurrentItemViewModel by viewModels()

    private lateinit var mediator: EventTrackerMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RDALogger.info("onCreate 执行！")
        dndManager = DNDManager(this)
        mediator = EventTrackerMediator(
            headerViewModel,
            durationSliderViewModel,
            eventButtonsViewModel,
            currentItemViewModel,
            eventNameViewModel,
            repository,
            SPHelper.getInstance(this),
            dndManager,
            sharedState,
        )

        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

        setContent {

            EventTrackerScreen(mediator = mediator)

        }

    }

    override fun onResume() {
        super.onResume()
        RDALogger.info("MainActivity 的 onResume() 执行了！")

        // 再次检查 “通知策略访问” 权限
//        if (dndManager.hasNotificationPolicyAccess() && sharedState.newEventName.value == "睡") {
//            dndManager.openDND()
//            // 必须重新置空。如果已授予权限，且当前名称缓存为 “睡”，那只要一打开应用就会进入勿扰模式，这会影响到忘却进行新的记录的情况。
//            sharedState.newEventName.value = ""
//        }

        mediator.increaseCDonResume()
    }

    override fun onStop() {
        super.onStop()
        mediator.saveState()
    }

}

