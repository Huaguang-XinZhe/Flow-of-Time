package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.DashType
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.widget.InputAlertDialog

@Composable
fun ClassNameInputAlertDialog(viewModel: EventInputViewModel) {
    val labelState = viewModel.labelState
    val type = labelState.type.value
    // 逗号分隔值（初始时会加载一次，这是 labelInfo 的 rowNames 为 null，故需指定初始值）
    val initialName = labelState.names?.joinToString() ?: "" // 如果列表只有一个元素，那么就不会加上逗号进行分隔
    val selection = if (type.isTag()) {
        TextRange(initialName.length)
    } else {
        TextRange(start = 0, end = initialName.length)
    }
    val initialValue = TextFieldValue(
        text = initialName,
        selection = selection
    )

    val title: String
    val labelText: String?
    val titlePrefix: String?

    when(type) {
        DashType.TAG -> {
            title = "新增/修改标签"
            labelText = "多个标签之间以 ，或 , 相隔"
            titlePrefix = "#"
        }
        DashType.CATEGORY_ADD -> {
            title = "新增类属"
            labelText = "若有多个，只取第一个"
            titlePrefix = "@"
        }
        DashType.CATEGORY_CHANGE -> {
            title = "修改类属"
            labelText = "若有多个，只取第一个"
            titlePrefix = "@"
        }
        DashType.MIXED_ADD -> {
            title = "新增类属和标签"
            labelText = "首个作类属，其余作标签，逗号分隔"
            titlePrefix = "+"
        }
    }

    InputAlertDialog(
        show = labelState.show.value,
        title = title,
        initialValue = initialValue,
        onDismiss = { viewModel.onClassNameDialogDismiss() },
        onConfirm = { newText ->
            viewModel.onClassNameDialogConfirm(labelState.eventId.value, type, newText)
        },
        labelText = labelText,
        titlePrefix = titlePrefix,
        inputHeight = if (type.isCategory()) 56.dp else 80.dp
    )
}