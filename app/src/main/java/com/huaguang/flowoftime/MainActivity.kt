package com.huaguang.flowoftime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.pages.time_record.EventControlViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.state.ButtonsState
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sharedState: SharedState // Activity 和 Fragment 各自注入，也不会出问题！
    @Inject
    lateinit var spHelper: SPHelper
    @Inject
    lateinit var idState: IdState
    @Inject
    lateinit var buttonsState: ButtonsState
    @Inject
    lateinit var pauseState: PauseState
    @Inject
    lateinit var iconRepository: IconMappingRepository

    private val eventControlViewModel: EventControlViewModel by viewModels()
    private val buttonsViewModel: EventButtonsViewModel by viewModels()
    private val regulatorViewModel: TimeRegulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iconRepository.preloadData() // 从数据库预加载映射数据到内存中

        val appViewModels = AppViewModels(
            eventControlViewModel, buttonsViewModel, regulatorViewModel
        )
        
        setContent {
            MyApp(appViewModels)
        }

        sharedState.toastMessage.observe(this) { toastMessage ->
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStop() {
        super.onStop()
        RDALogger.info("回调 onStop()")

        spHelper.saveState(idState, buttonsState, pauseState, sharedState.cursorType)
    }
}

