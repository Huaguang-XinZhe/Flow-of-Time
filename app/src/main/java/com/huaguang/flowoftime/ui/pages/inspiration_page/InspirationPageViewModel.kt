package com.huaguang.flowoftime.ui.pages.inspiration_page

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.tables.Inspiration
import com.huaguang.flowoftime.data.repositories.InspirationRepository
import com.huaguang.flowoftime.ui.state.CategoryLabelState
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.copyToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.ParseException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InspirationPageViewModel @Inject constructor(
    val repository: InspirationRepository,
    val sharedState: SharedState,
    val categoryLabelState: CategoryLabelState,
): ViewModel() {
//    private val _allInspirations = MutableStateFlow(listOf<Inspiration>())
//    val allInspirations: StateFlow<List<Inspiration>> = _allInspirations
    val tabMap = mapOf(
        0 to "开发",
        1 to "抗性",
        2 to null,
        3 to "探索",
        4 to "资源",
    )
    val dateDisplayTabs = listOf("抗性", "开发")

//    init {
//        viewModelScope.launch {
//            repository.getAllInspirationsFlow().collect { inspirations ->
//                itemCount.intValue = inspirations.size
//                _allInspirations.value = inspirations
//            }
//        }
//    }

    fun onDeleteButtonClick(id: Long) {
        viewModelScope.launch {
            repository.deleteInspirationById(id)
        }
    }

    fun import(inputText: String) {
        try {
            val inspirations = inputText.split("$=$").map { elementStr ->
                val (dateStr, htmlText) = elementStr.split("===")

                Inspiration(
                    date = LocalDate.parse(dateStr),
                    text = htmlText,
                )
            }

            viewModelScope.launch {
                if (inspirations.isNotEmpty()) {
                    repository.insertAll(inspirations)
                    sharedState.toastMessage.value = "导入成功"
                } else {
                    sharedState.toastMessage.value = "解析为空，导入失败"
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    fun export(context: Context, inspirations: List<Inspiration>) {
        val exportText = inspirations.joinToString(
            separator = "$=$"
        ) { inspiration ->
            "${inspiration.date}===${inspiration.text}"
        } // 把列表中的每个元素都按 lambda 中的转换方法进行转换，转成字符串，然后用 separator 连接起来（没指定的话就默认用英文逗号加空格）

        copyToClipboard(context, exportText)
        sharedState.toastMessage.value = "数据已复制到剪贴板"
    }

    fun getInspirations(category: String?) = repository.getInspirationByCategoryFlow(category)

    fun onCategoryDialogConfirmButtonClick(id: Long, text: String) {
        val newCategory = text.trim()

        if (newCategory.isEmpty()) {
            sharedState.toastMessage.value = "类属不能为空哦😊"
            return
        }

        viewModelScope.launch {
            repository.updateCategoryById(id, newCategory)
        }
    }

}