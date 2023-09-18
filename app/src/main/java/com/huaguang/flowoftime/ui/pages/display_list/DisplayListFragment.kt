package com.huaguang.flowoftime.ui.pages.display_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DisplayListFragment : Fragment() {

    @Inject
    lateinit var repository: EventRepository

    private val inputViewModel: EventInputViewModel by viewModels()

    lateinit var viewModel: DisplayListPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = DisplayListPageViewModel(
            inputViewModel,
            repository
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DisplayListPage(viewModel = viewModel)
            }
        }

    }

}