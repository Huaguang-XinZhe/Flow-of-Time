package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputAlertDialog(
    viewModel: EventInputViewModel
) {
    if (!viewModel.sharedState.dialogShow.value) return

    val initialValue = TextFieldValue(
        text = viewModel.coreName,
        selection = TextRange(0, viewModel.coreName.length)
    )
    val textFieldValue = remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    val eventControl = LocalEventControl.current
    val buttonsStateControl = LocalButtonsStateControl.current

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = { viewModel.onDialogDismiss() },
        icon = {
            Icon(
                painterResource(id = R.drawable.current_core),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text(text = "「当前核心」事项")
        },
        text = {
            OutlinedTextField(
                value = textFieldValue.value,
                onValueChange = { textFieldValue.value = it },
                singleLine = true,
                modifier = Modifier
                    .focusRequester(focusRequester)
            )
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onDialogDismiss() }) {
                Text("取消")
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.onDialogConfirm(
                    newText = textFieldValue.value.text,
                    eventControl = eventControl,
                    buttonsStateControl = buttonsStateControl,
                )
            }) {
                Text("确认")
            }
        },
    )
}