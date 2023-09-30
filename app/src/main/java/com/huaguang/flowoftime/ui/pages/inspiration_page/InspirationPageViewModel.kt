package com.huaguang.flowoftime.ui.pages.inspiration_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.tables.Inspiration
import com.huaguang.flowoftime.data.repositories.InspirationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspirationPageViewModel @Inject constructor(
    val repository: InspirationRepository,
): ViewModel() {
    private val _allInspirations = MutableStateFlow(listOf<Inspiration>())
    val allInspirations: StateFlow<List<Inspiration>> = _allInspirations

    init {
        viewModelScope.launch {
            repository.getAllInspirationsFlow().collect { inspirations ->
                _allInspirations.value = inspirations
            }
        }
    }

    fun onDeleteButtonClick(id: Long) {
        viewModelScope.launch {
            repository.deleteInspirationById(id)
        }
    }

}