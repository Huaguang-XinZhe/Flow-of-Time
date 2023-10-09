package com.huaguang.flowoftime.ui.components.category_dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.ui.widget.InputAlertDialog

@Composable
fun ClassNameInputAlertDialog(
    viewModel: CategoryViewModel = viewModel()
) {
    var dialogDisplay: DialogDisplay? = null

    val labelState = viewModel.labelState.apply {
        if (category.value == null && tags == null) {
            dialogDisplay = DialogDisplay.Default
            return@apply
        }

        if (category.value != null && tags == null) {
            dialogDisplay = DialogDisplay.CategoryOnly(
                category = category.value!!
            )
            return@apply
        }

        if (tags != null) { // tags 不为 null，category 一定不为 null
            dialogDisplay = DialogDisplay.CategoryAndTags(
                category = category.value!!,
                tags = tags!!
            )
            return@apply
        }
    }
    RDALogger.info("labelState = $labelState")
    RDALogger.info("dialogDisplay = ${dialogDisplay?.toString()}")

    // 这里必须使用安全调用（?.），传非空值进来，否则 title、inputTip 等，会使用默认值，就不能用上继承的优势了
    dialogDisplay?.apply {
        InputAlertDialog(
            show = labelState.dialogShow.value,
            title = title,
            initialValue = getInitialValue(),
            onDismiss = { viewModel.onClassNameDialogDismiss() },
            onConfirm = { newText ->
                viewModel.onClassNameDialogConfirm(labelState.eventId, newText)
            },
            inputTip = inputTip,
        )
    }
}

sealed class DialogDisplay(
    val title: String = "新增类属和标签",
    val initialName: String = "",
    val startIndex: Int = 0,
    val endIndex: Int = 0,
    val inputTip: String? = "首个作类属，其余作标签，逗号分隔值"
) {
    /**
     * 默认，没有类属，也没有标签的情况，UI 上只有一个虚框添加按钮
     */
    object Default : DialogDisplay()

    /**
     * 只有类属，没有标签，默认选中标签，后边加个逗号，以便修改或新增标签；
     */
    data class CategoryOnly(
        val category: String
    ) : DialogDisplay(
        title = "更改类属或新增标签", 
        initialName = "$category，",
        endIndex = category.length + 1,
    )

    /**
     * 既有类属又有标签，默认添加标签，在后边加个逗号，以便快速新增；
     * 当然，也可以修改类属，只是多点一步罢了
     */
    data class CategoryAndTags(
        val category: String, 
        val tags: List<String>,
        // 下边的参数虽然在构造函数里边，但并非构造所需，只是为了存储中间值，避免重复计算
        val separatedTags: String = tags.joinToString("，"),
        val _initialName: String = "$category，$separatedTags，",
        val length: Int = _initialName.length,
    ) : DialogDisplay(
        title = "新增标签或更改类属",
        initialName = _initialName,
        startIndex = length,
        endIndex = length,
    )

    fun getInitialValue(): TextFieldValue {
        val selection = TextRange(start = startIndex, end = endIndex)

        return TextFieldValue(
            text = initialName,
            selection = selection
        )
    }

}