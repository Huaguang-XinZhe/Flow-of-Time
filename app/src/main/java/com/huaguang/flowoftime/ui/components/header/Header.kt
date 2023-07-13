package com.huaguang.flowoftime.ui.components.header

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun HeaderRow(viewModel: HeaderViewModel) {
//    var showDialog by remember { mutableStateOf(false) }
    val isOneDayButtonClicked by viewModel.isOneDayButtonClicked.collectAsState()

    val toggleButtonText = if (isOneDayButtonClicked) "RecentTwoDays" else "OneDay"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { viewModel.deleteAll() }
        ) {
            Text("Delete All")
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

//    if (showDialog) {
//        ImportEventsDialog(
//            onDismiss = { showDialog = false },
//            onImport = { text -> viewModel.importEvents(text) }
//        )
//    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ImportEventsDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入时间记录") },
        text = {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("请输入要导入的源文本") },
                modifier = Modifier.heightIn(min = 56.dp, max = 560.dp)  // Assuming each line is 56.dp
            )
        },
        confirmButton = {
            Button(onClick = {
                onImport(inputText)
                onDismiss()
            }) {
                Text("导入")
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