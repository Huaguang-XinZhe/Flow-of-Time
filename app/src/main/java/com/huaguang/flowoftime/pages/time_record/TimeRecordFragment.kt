package com.huaguang.flowoftime.pages.time_record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.data.sources.SPHelper
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulatorViewModel
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
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
    lateinit var dndManager: DNDManager

    // 注入各大组件的 ViewModel
    private val eventButtonsViewModel: EventButtonsViewModel by viewModels()
    private val timeRegulatorViewModel: TimeRegulatorViewModel by viewModels()
    private val eventInputViewModel: EventInputViewModel by viewModels()

    private lateinit var pageViewModel: TimeRecordPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dndManager = DNDManager(requireContext())
        pageViewModel = TimeRecordPageViewModel(
            eventButtonsViewModel,
            timeRegulatorViewModel,
            eventInputViewModel,
            eventRepository,
            spHelper,
            sharedState, dndManager
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TimeRecordPage(pageViewModel = pageViewModel)
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventButtonsViewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            sharedState.currentEvent = event // 一但撤销，就赋新值
        }

        sharedState.toastMessage.observe(viewLifecycleOwner) { toastMessage ->
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
        }

    }

}