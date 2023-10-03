package com.huaguang.flowoftime.ui.pages.inspiration_page

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import com.huaguang.flowoftime.data.models.tables.Inspiration
import com.huaguang.flowoftime.utils.toAnnotatedString

@Composable
fun InspirationPage(viewModel: InspirationPageViewModel = viewModel()) {
    val allInspirations by viewModel.allInspirations.collectAsState()
    val itemCount by viewModel.itemCount
    val webViewShow = remember { mutableStateOf(false) }
    val dialogShow = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = "现有 $itemCount 条",
                fontSize = 18.sp,
                modifier = Modifier.padding(10.dp)
            )
        }

        items(allInspirations) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormattedText(item)

                    TextButton(
                        onClick = { viewModel.onDeleteButtonClick(item.id) },
                    ) {
                        Text("del")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val context = LocalContext.current

                Button(onClick = { dialogShow.value = true }) {
                    Text(text = "导入")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = { viewModel.export(context) }) {
                    Text(text = "导出")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = { webViewShow.value = true }) {
                    Text(text = "显示 WebView")
                }
            }
        }
    }

    if (webViewShow.value) {
        WebViewCompose()
    }

    if (dialogShow.value) {
        ImportDialog(
            onDismiss = { dialogShow.value = false },
            onImport = { inputText ->
                viewModel.import(inputText)
            }
        )
    }

}

@Composable
fun RowScope.FormattedText(inspiration: Inspiration) {
    val spannedText = HtmlCompat.fromHtml(inspiration.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    val annotatedStr = spannedText.toAnnotatedString()
    val context = LocalContext.current

    ClickableText(
        text = annotatedStr,
        style = TextStyle(fontSize = 15.sp),
        modifier = Modifier
            .padding(15.dp, 10.dp, 0.dp, 10.dp)
            .weight(1f),
        onClick = { offset ->
            val annotations = annotatedStr.getStringAnnotations("URL", offset, offset)
            annotations.firstOrNull()?.let {
                val uri = Uri.parse(it.item)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        }

    )
}

@Composable
fun WebViewCompose() {
    val state = rememberWebViewState(url = "https://www.taobao.com")

    WebView(
        state = state,
        captureBackPresses = true,
        modifier = Modifier.fillMaxSize(),
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入灵感记录") },
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
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}