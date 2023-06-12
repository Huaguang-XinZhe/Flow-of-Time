package com.huaguang.flowoftime.ui.components.header

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.EventRepository
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.copyToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HeaderViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
) : ViewModel() {

    // 专有
    val isOneDayButtonClicked = MutableStateFlow(false)

    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态
    }

    fun exportEvents() {
        viewModelScope.launch {
            val exportText = withContext(Dispatchers.IO) {
                repository.exportEvents()
            }

            copyToClipboard(sharedState.application, exportText)

            sharedState.toastMessage.value = "导出数据已复制到剪贴板"
        }
    }

    fun importEvents(text: String) {
        sharedState.toastMessage.value = "导入成功"
    }

}