package com.huaguang.flowoftime.ui.components.header

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.copyToClipboard
import com.huaguang.flowoftime.utils.isGetUpTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HeaderViewModel @Inject constructor(
    private val repository: EventRepository,
    private val sharedState: SharedState,
    private val dateStoreHelper: DataStoreHelper,
) : ViewModel() {

    // 专有
    val isOneDayButtonClicked = MutableStateFlow(false)
    // 默认允许重置列表显示的模式，也就是默认显示最近两天的情况
    var resetListDisplayFlag = true

    init {
        viewModelScope.launch {
            resetListDisplayFlag = dateStoreHelper.resetListDisplayFlagFlow.first()
        }
    }

    fun toggleListDisplayState() {
        isOneDayButtonClicked.value = !isOneDayButtonClicked.value //切换状态

        if (isGetUpTime(LocalTime.now())) {
            RDALogger.info("现在是起床时段，下次起床时段启动，就不允许重置了，要保持我切换后的状态")
            // 在起床时段内，只重置一次列表展示（即默认展示两天的内容）；
            // 在起床时段内，点了一次切换之后，下次就不重置 isOneDayButtonClicked 的状态了。
            viewModelScope.launch {
                updateResetFlag(false)
            }
        }
    }

    private suspend fun updateResetFlag(value: Boolean) {
        resetListDisplayFlag = value
        dateStoreHelper.saveResetListAgainFlag(value)
    }

    fun exportAndDeleteEvents(tag: String) {
        viewModelScope.launch {
            val exportText = withContext(Dispatchers.IO) {
                repository.exportEvents(tag)
            }

            copyToClipboard(sharedState.application, exportText)

            repository.deleteEventsExceptToday()
        }

        sharedState.toastMessage.value = "已导出${tag}数据，并复制到剪贴板"
        sharedState.toastMessage.value = "除当天以外的其他数据已全部清除"
    }

//    fun deleteEventsExceptToday() {
//        viewModelScope.launch {
//            repository.deleteEventsExceptToday()
//        }
//
//        sharedState.toastMessage.value = "除今天以外的所有数据已全部清除！"
//    }

}