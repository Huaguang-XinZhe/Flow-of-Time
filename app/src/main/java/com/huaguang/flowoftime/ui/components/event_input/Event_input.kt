package com.huaguang.flowoftime.ui.components.event_input

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInputField(
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier,
) {
    if (!viewModel.inputState.show.value) return // 为 false 不显示

    val focusRequester = remember { FocusRequester() }
    val newEventName by viewModel.inputState.newName
    var textFieldState by remember {
        mutableStateOf(TextFieldValue(text = newEventName))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        TextField(
            value = textFieldState,
            onValueChange = { textFieldState = it },
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

        Button(onClick = { viewModel.onConfirmButtonClick(textFieldState.text) }) {
            Text("确认")
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

}


