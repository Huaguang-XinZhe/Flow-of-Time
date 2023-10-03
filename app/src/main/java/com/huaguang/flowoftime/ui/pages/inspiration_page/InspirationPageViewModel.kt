package com.huaguang.flowoftime.ui.pages.inspiration_page

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huaguang.flowoftime.data.models.tables.Inspiration
import com.huaguang.flowoftime.data.repositories.InspirationRepository
import com.huaguang.flowoftime.ui.state.SharedState
import com.huaguang.flowoftime.utils.copyToClipboard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.ParseException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InspirationPageViewModel @Inject constructor(
    val repository: InspirationRepository,
    val sharedState: SharedState,
): ViewModel() {
    private val _allInspirations = MutableStateFlow(listOf<Inspiration>())
    val allInspirations: StateFlow<List<Inspiration>> = _allInspirations
    val itemCount = mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            repository.getAllInspirationsFlow().collect { inspirations ->
                itemCount.intValue = inspirations.size
                _allInspirations.value = inspirations
            }
        }
    }

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

    fun export(context: Context) {
        val exportText = _allInspirations.value.joinToString(
            separator = "$=$"
        ) { inspiration ->
            "${inspiration.date}===${inspiration.text}"
        } // 把列表中的每个元素都按 lambda 中的转换方法进行转换，转成字符串，然后用 separator 连接起来（没指定的话就默认用英文逗号加空格）

        copyToClipboard(context, exportText)
        sharedState.toastMessage.value = "数据已复制到剪贴板"
    }

}