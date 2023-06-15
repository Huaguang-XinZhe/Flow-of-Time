package com.huaguang.flowoftime.ui.components.header


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun HeaderRow(viewModel: HeaderViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val isOneDayButtonClicked by viewModel.isOneDayButtonClicked.collectAsState()

    val toggleButtonText = if (isOneDayButtonClicked) "RecentTwoDays" else "OneDay"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { showDialog = true }
        ) {
            Text("关键词")
        }

        Button(
            onClick = { viewModel.exportEvents() },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("导出")
        }

        OutlinedButton(
            onClick = { viewModel.toggleListDisplayState() },
            modifier = Modifier.padding(start = 5.dp)
        ) {
            Text(toggleButtonText)
        }

    }

    if (showDialog) {
        KeyWordsSettingDialog(
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        ) { text -> viewModel.updateKeyWords(text) }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun KeyWordsSettingDialog(
    viewModel: HeaderViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var keyWordsInputValue by remember {
        mutableStateOf(TextFieldValue(text = ""))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        val keyWordsInput = viewModel.getCoreKeyWordsInput()

        keyWordsInputValue = TextFieldValue(
            text = keyWordsInput,
            selection = TextRange(keyWordsInput.length)
        )
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设定核心事务关键词") },
        text = {
            OutlinedTextField(
                value = keyWordsInputValue,
                onValueChange = { keyWordsInputValue = it },
                label = { Text("可设定多个，换行分隔") },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(keyWordsInputValue.text)
                onDismiss()
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("取消")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}