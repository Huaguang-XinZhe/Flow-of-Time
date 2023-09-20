package com.huaguang.flowoftime.ui.components.category_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.DashType
import com.huaguang.flowoftime.data.repositories.EventRepository
import com.huaguang.flowoftime.ui.state.LabelState
import com.huaguang.flowoftime.ui.state.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
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
        name: String,
        type: DashType,
        names: List<String>? = null
    ) {
        if (name.isEmpty()) { // 没有指定 name（数据库的类属为 null 才不指定 name），即为 + 或 *
            labelState.apply {
                eventId.value = id
                show.value = true
                this.name.value = name
                this.type.value = type
                this.names = names
            }
        } else {
            // 打开搜索页，进行搜索
            sharedState.toastMessage.value = "打开搜索页，进行搜索"
        }
    }

    fun onClassNameDialogDismiss() {
        labelState.show.value = false
    }

    fun onClassNameDialogConfirm(eventId: Long, type: DashType, newText: String) {
        if (newText.trim().isEmpty()) {
            sharedState.toastMessage.value = "类属不能为空哦😊"
            return
        }

        var hasLongString = false
        val labels = newText // 如果 labels 只有一个元素，没有逗号分隔，那么将会返回只有这个元素的集合，不会出错
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

        viewModelScope.launch {
            when(type) {
                DashType.TAG -> {
                    // 全是标签，存入数据库
                    repository.updateTags(eventId, labels)
                }
                DashType.CATEGORY_ADD, DashType.CATEGORY_CHANGE -> {
                    // 只取第一个作为类属，其余无视
                    repository.updateCategory(eventId, labels.first())
                }
                DashType.MIXED_ADD -> {
                    // 第一个作为类属，其余作为标签
                    val category = labels.first()
                    val remain = labels.apply { removeFirst() }
                    val tags = if (remain.isEmpty()) null else remain
                    RDALogger.info("tags = $tags")
                    repository.updateClassName(
                        id = eventId,
                        category = category,
                        tags = tags
                    )
                }
            }
        }

        onClassNameDialogDismiss()
    }

}