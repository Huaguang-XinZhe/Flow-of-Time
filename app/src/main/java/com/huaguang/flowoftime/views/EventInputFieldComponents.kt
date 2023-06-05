package com.huaguang.flowoftime.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.huaguang.flowoftime.viewmodels.EventsViewModel

@Composable
fun EventInputField(viewModel: EventsViewModel) {
    val focusRequester = remember { FocusRequester() }

    Column {
        UndoIconButton(viewModel = viewModel)

        InputRow(viewModel = viewModel, focusRequester = focusRequester)
        
        Spacer(modifier = Modifier.fillMaxWidth().height(280.dp))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputRow(viewModel: EventsViewModel, focusRequester: FocusRequester) {
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

        Button(onClick = { viewModel.onConfirm() }) {
            Text("确认")
        }
    }
}

@Composable
fun UndoIconButton(viewModel: EventsViewModel) {
    val isEventNameNotClicked by viewModel.isEventNameNotClicked

    if (isEventNameNotClicked) {
        IconButton(
            onClick = {
                viewModel.resetState()
            },
            modifier = Modifier
                .size(36.dp)
                .padding(start = 10.dp, bottom = 5.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.revocation),
                contentDescription = null
            )
        }
    }
}