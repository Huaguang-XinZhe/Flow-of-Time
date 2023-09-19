package com.huaguang.flowoftime.ui.pages.time_record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.state.IdState
import com.huaguang.flowoftime.ui.state.PauseState
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.DNDManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimeRecordFragment : Fragment() {

    @Inject
    lateinit var eventRepository: EventRepository
    @Inject
    lateinit var iconRepository: IconMappingRepository
    @Inject
    lateinit var spHelper: SPHelper
    @Inject
    lateinit var sharedState: SharedState
    @Inject
    lateinit var idState: IdState
    @Inject
    lateinit var pauseState: PauseState

    lateinit var dndManager: DNDManager

    // 注入各大组件的 ViewModel
    private val eventButtonsViewModel: EventButtonsViewModel by viewModels()
    private val timeRegulatorViewModel: TimeRegulatorViewModel by viewModels()
    private val eventInputViewModel: EventInputViewModel by viewModels()

    private lateinit var pageViewModel: TimeRecordPageViewModel
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dndManager = DNDManager(requireContext())
        pageViewModel = TimeRecordPageViewModel(
            eventButtonsViewModel,
            timeRegulatorViewModel,
            eventInputViewModel,

//            dndManager,

        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TimeRecordPage(viewModel = pageViewModel)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeRegulatorViewModel.apply {
            checkedLiveData.observe(viewLifecycleOwner) { newValue ->
//                RDALogger.info("观察到变化 newValue = $newValue")
                // 防止初始化的时候执行
                if (initialized) {
                    calPauseInterval(newValue) // 希望比副作用要快
                    RDALogger.info("监听 acc = ${pauseState.acc.value}")

                    if (newValue) { // 只有恢复原先状态的时候才会执行。
                        pauseState.apply {
                            when(sharedState.cursorType.value) {
                                EventType.SUBJECT -> subjectAcc.value = acc.value
                                EventType.STEP -> stepAcc.value = acc.value
                                else -> currentAcc.value = acc.value // else 包括 null，但没事，null 下根本不会执行到这里
                            }

                            RDALogger.info("重置 pauseState 的状态")
                            start.value = null
                            acc.value = 0
                        }

                    }
                }

                initialized = true // 必须放在 if 块外，如果直接 return，那就相当于放在块内了。
            }
        }

    }

    override fun onStop() {
        super.onStop()
        RDALogger.info("回调 onStop()")
        eventButtonsViewModel.apply {
            spHelper.saveState(idState, buttonsState, pauseState, sharedState.cursorType)
        }
    }

}