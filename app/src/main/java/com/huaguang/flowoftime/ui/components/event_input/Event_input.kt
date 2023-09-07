package com.huaguang.flowoftime.ui.components.event_input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R

@Composable
fun EventInputField(
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier,
) {
    if (!viewModel.isInputShow.value) return // 为 false 不显示

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
    ) {
        UndoIconButton(viewModel)

        InputRow(viewModel = viewModel, focusRequester = focusRequester)

    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputRow(
    viewModel: EventInputViewModel,
    focusRequester: FocusRequester
) {
    val newEventName by viewModel.newEventName
    var textFieldState by remember {
        mutableStateOf(TextFieldValue(text = newEventName))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textFieldState,
            onValueChange = {
                textFieldState = it
                viewModel.newEventName.value = it.text
            },
            label = { Text("事件名称") },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        // 这将全选文本
                        textFieldState = textFieldState.copy(
                            selection = TextRange(0, textFieldState.text.length)
                        )
                    }
                }
        )

        Button(onClick = { viewModel.onConfirmButtonClick() }) {
            Text("确认")
        }
    }
}

@Composable
fun UndoIconButton(viewModel: EventInputViewModel) {
    IconButton(
        onClick = {
            viewModel.undoButtonClick()
        },
        modifier = Modifier
            .size(36.dp)
            .padding(start = 10.dp, bottom = 5.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.undo),
            contentDescription = null
        )
    }
}