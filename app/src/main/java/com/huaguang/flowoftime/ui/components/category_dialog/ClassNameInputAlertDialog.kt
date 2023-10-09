package com.huaguang.flowoftime.ui.components.category_dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.ui.widget.InputAlertDialog

@Composable
fun ClassNameInputAlertDialog(viewModel: CategoryViewModel = viewModel()) {
    val labelState = viewModel.labelState
    // 逗号分隔值（初始时会加载一次，这是 labelInfo 的 rowNames 为 null，故需指定初始值）
    val initialName = labelState.names?.joinToString() ?: "" // 如果列表只有一个元素，那么就不会加上逗号进行分隔
    val selection = TextRange(start = 0, end = initialName.length)
    val initialValue = TextFieldValue(
        text = initialName,
        selection = selection
    )

    InputAlertDialog(
        show = labelState.show.value,
        title = "新增类属和标签",
        initialValue = initialValue,
        onDismiss = { viewModel.onClassNameDialogDismiss() },
        onConfirm = { newText ->
            viewModel.onClassNameDialogConfirm(labelState.eventId.value, newText)
        },
        labelText = "首个作类属，其余作标签，逗号分隔",
        titlePrefix = "+",
    )
}