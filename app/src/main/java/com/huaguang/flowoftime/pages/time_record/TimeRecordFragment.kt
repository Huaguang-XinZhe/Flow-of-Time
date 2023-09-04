package com.huaguang.flowoftime.pages.time_record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.pages.time_record.recording_event_item.RecordingEventItemViewModel
import com.huaguang.flowoftime.pages.time_record.time_regulator.TimeRegulatorViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TimeRecordFragment : Fragment() {

    @Inject
    lateinit var eventRepository: EventRepository
    @Inject
    lateinit var iconRepository: IconMappingRepository

    // 注入各大组件的 ViewModel
    private val eventButtonsViewModel: EventButtonsViewModel by viewModels()
    private val timeRegulatorViewModel: TimeRegulatorViewModel by viewModels()
    private val recordingEventItemViewModel: RecordingEventItemViewModel by viewModels()

    private lateinit var pageViewModel: TimeRecordPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pageViewModel = TimeRecordPageViewModel(
            eventButtonsViewModel,
            timeRegulatorViewModel,
            recordingEventItemViewModel,
            eventRepository,
            iconRepository,
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
            pageViewModel.currentEvent = event // 一但撤销，就赋新值
        }

    }

}