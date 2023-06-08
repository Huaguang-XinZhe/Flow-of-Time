package com.huaguang.flowoftime.ui.components.header

import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.TimeStreamApplication
import com.huaguang.flowoftime.data.EventRepository
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
    application: TimeStreamApplication
) : AndroidViewModel(application) {

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

            copyToClipboard(getApplication(), exportText)
            Toast.makeText(getApplication(), "导出数据已复制到剪贴板", Toast.LENGTH_SHORT).show()
        }
    }

    fun importEvents(text: String) {
        Toast.makeText(getApplication(), "导入成功", Toast.LENGTH_SHORT).show()
    }

}