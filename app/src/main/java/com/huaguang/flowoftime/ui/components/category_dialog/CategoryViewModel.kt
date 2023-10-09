package com.huaguang.flowoftime.ui.components.category_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.EventCategoryUpdate
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.LabelState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    val labelState: LabelState,
    val sharedState: SharedState,
    val repository: EventRepository,
) : ViewModel() {

    fun onClassNameClick(
        id: Long,
        name: String
    ) {
        // 打开搜索页，进行搜索
        sharedState.toastMessage.value = "打开搜索页，进行搜索"
    }

    fun onDashButtonClick(
        id: Long,
        category: String?,
        tags: List<String>?
    ) {
        labelState.apply {
            eventId = id
            dialogShow.value = true
            this.category.value = category
            this.tags = tags
        }
    }

    fun onClassNameDialogDismiss() {
        labelState.dialogShow.value = false
    }

    fun onClassNameDialogConfirm(eventId: Long, newText: String) {
        val labels = processInputText(newText) ?: return

        viewModelScope.launch {
            val category = labels.removeAt(0)  // Remove and get the first element
            val tags = if (labels.isEmpty()) null else labels

            // 触发类属统计更新（必须放在前边，否则以前的类属获取不到）
            sharedState.categoryUpdate.value = EventCategoryUpdate(eventId, category)

            delay(50) // 延迟一下，防止以前的类属还没获取到就更新了

            repository.updateClassName(
                id = eventId,
                category = category,
                tags = tags
            )
        }

        onClassNameDialogDismiss()
    }


    private fun processInputText(text: String): MutableList<String>? {
        if (text.trim().isEmpty()) {
            sharedState.toastMessage.value = "类属不能为空哦😊"
            return null
        }

        var hasLongString = false
        val labels = text // 如果 labels 只有一个元素，没有逗号分隔，那么将会返回只有这个元素的集合，不会出错
            .split("，", ",")
            .map { it.trim() } // 使用 map 函数来应用 trim 函数到每一个元素
            .filterNot {
                if (it.length > 15) {
                    hasLongString = true
                }
                it.isEmpty() || it.length > 15 // 使用 filterNot 函数来排除所有空字符串和长串
            }
            .toMutableList() // 转换结果为可变列表

        if (hasLongString) {
            sharedState.toastMessage.value = "太长的话，就删了哦🙃"
        }

        return labels
    }

}