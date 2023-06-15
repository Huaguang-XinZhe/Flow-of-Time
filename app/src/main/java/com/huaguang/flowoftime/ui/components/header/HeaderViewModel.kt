package com.huaguang.flowoftime.ui.components.header

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.data.sources.DataStoreHelper
import com.huaguang.flowoftime.ui.components.SharedState
import com.huaguang.flowoftime.utils.copyToClipboard
import com.huaguang.flowoftime.utils.extensions.isGetUpTime
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
    private val dataStoreHelper: DataStoreHelper,
) : ViewModel() {

    // 专有
    val isOneDayButtonClicked = MutableStateFlow(false)
    // 默认允许重置列表显示的模式，也就是默认显示最近两天的情况
    var resetListDisplayFlag = true

    init {
        viewModelScope.launch {
            resetListDisplayFlag = dataStoreHelper.resetListDisplayFlagFlow.first()
        }
    }

    suspend fun getCoreKeyWordsInput(): String {
        val keyWords = dataStoreHelper.coreEventKeyWordsFlow.first()
        return keyWords.joinToString(separator = "\n")
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
        dataStoreHelper.saveResetListAgainFlag(value)
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

    fun updateKeyWords(text: String) {
        // 按行提取文本，加入到列表，并保存
        viewModelScope.launch {
            val keyWords = text.split("\n")
            dataStoreHelper.saveCoreEventKeyWords(keyWords)
            sharedState.toastMessage.value = "应用成功"
        }
    }

}