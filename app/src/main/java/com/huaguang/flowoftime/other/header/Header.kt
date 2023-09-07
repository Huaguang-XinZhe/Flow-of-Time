package com.huaguang.flowoftime.other.header

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.other.widget.TagGroup

@Composable
fun HeaderRow(viewModel: HeaderViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    val isOneDayButtonClicked by viewModel.isOneDayButtonClicked.collectAsState()

    val toggleButtonText = if (isOneDayButtonClicked) "RecentTwoDays" else "OneDay"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Button(
//            onClick = { viewModel.deleteEventsExceptToday() }
//        ) {
//            Text("Delete")
//        }

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("导出并删除")
        }

        OutlinedButton(
            onClick = { viewModel.toggleListDisplayState() },
            modifier = Modifier.padding(start = 5.dp)
        ) {
            Text(toggleButtonText)
        }

    }

    if (showDialog) {
        SelectAndConfirmDialog(
            onDismiss = { showDialog = false },
            onConfirm = { tag ->
                viewModel.exportAndDeleteEvents(tag)
            }
        )

//        ImportEventsDialog(
//            onDismiss = { showDialog = false },
//            onImport = { text -> viewModel.importEvents(text) }
//        )
    }
}


@Composable
fun SelectAndConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // 这个 String 是 Tag
) {
    var tag by remember { mutableStateOf("昨日") }

    AlertDialog( // 由外至内
        onDismissRequest = onDismiss,
        title = {
            TitleOfDialog()
        },
        text = {
            TagGroup(
                tags = listOf("昨日", "全部"),
                onSelected = { chip ->
                    tag = chip
                }
            )
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("取消")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(tag)
                onDismiss()
            }) {
                Text("确认")
            }
        }
    )
}

@Composable
fun TitleOfDialog() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.export),
            contentDescription = null,
            modifier = Modifier.size(36.dp).padding(5.dp)
        )

        Text("选择导出范围并确认")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ImportEventsDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss, // 在对话框外部被点击或者用户按下返回键时被调用
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
        // 设置了这个属性可让弹窗的宽高随内容而变化
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}