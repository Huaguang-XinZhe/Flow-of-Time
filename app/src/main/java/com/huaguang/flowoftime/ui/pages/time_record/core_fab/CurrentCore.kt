package com.huaguang.flowoftime.ui.pages.time_record.core_fab

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.pages.time_record.LocalButtonsStateControl
import com.huaguang.flowoftime.ui.pages.time_record.LocalDisplayItemState
import com.huaguang.flowoftime.ui.pages.time_record.LocalEventControl
import com.huaguang.flowoftime.ui.pages.time_record.LocalRecordingItemState
import com.huaguang.flowoftime.ui.widget.InputAlertDialog
import com.huaguang.flowoftime.ui.widget.LongPressFloatingActionButton

@Composable
fun CoreFloatingButton(
    modifier: Modifier = Modifier,
    viewModel: CurrentCoreViewModel = viewModel()
) {
    if (viewModel.coreButtonNotShow()) return

    val eventControl = LocalEventControl.current
    val buttonsStateControl = LocalButtonsStateControl.current
    val displayItemState = LocalDisplayItemState.current
    val recordingItemState = LocalRecordingItemState.current

    LongPressFloatingActionButton(
        onClick = {
            viewModel.onCoreFloatingButtonClick(
                eventControl,
                buttonsStateControl,
                displayItemState,
                recordingItemState,
            )
        },
        onLongClick = { viewModel.onCoreFloatingButtonLongClick() },
        modifier = modifier.size(48.dp) // 新的按钮大小会覆盖先前默认的设置
    ) {
        Icon(
            painter = painterResource(id = R.drawable.current_core),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)

        )
    }
}

@Composable
fun CoreNameInputAlertDialog(viewModel: CurrentCoreViewModel = viewModel()) {

    val initialValue = TextFieldValue(
        text = viewModel.coreName,
        selection = TextRange(0, viewModel.coreName.length)
    )
    val eventControl = LocalEventControl.current
    val buttonsStateControl = LocalButtonsStateControl.current
    val displayItemState = LocalDisplayItemState.current
    val recordingItemState = LocalRecordingItemState.current

    InputAlertDialog(
        show = viewModel.coreInputShow.value,
        title = "「当前核心」事项",
        initialValue = initialValue,
        onDismiss = { viewModel.onCoreNameDialogDismiss() },
        onConfirm = { newText ->
            viewModel.onCoreNameDialogConfirm(
                newText = newText,
                eventControl = eventControl,
                buttonsStateControl = buttonsStateControl,
                displayItemState = displayItemState,
                recordingItemState = recordingItemState,
            )
        },
        iconRes = R.drawable.current_core,
        labelText = "设置/更新名称",
    )

}
