package com.huaguang.flowoftime.ui.widget

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputAlertDialog(
    show: Boolean,
    title: String,
    initialValue: TextFieldValue,
    onDismiss: () -> Unit,
    onConfirm: (newText: String) -> Unit,
    iconRes: Int? = null,
    titlePrefix: String? = null,
    labelText: String? = null
) {
    if (!show) return

    val textFieldValue = remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
           if (iconRes != null) {
               Icon(
                   painterResource(id = iconRes),
                   contentDescription = null,
                   tint = MaterialTheme.colorScheme.primary,
                   modifier = Modifier.size(24.dp)
               )
           }
        },
        title = {
            val annotatedString = buildAnnotatedString {
                if (titlePrefix != null) {
                    withStyle(
                        style = SpanStyle(
                            color = Color.DarkGray.copy(alpha = 0.8f),
                        )
                    ) {
                        append("$titlePrefix ")
                    }
                }
                append(title) // 细节 Bug，title 不应该放在 titlePrefix 非空块下！
            }
            Text(text = annotatedString)
        },
        text = {
            OutlinedTextField(
                value = textFieldValue.value,
                onValueChange = { textFieldValue.value = it },
                maxLines = 2,
                modifier = Modifier.focusRequester(focusRequester),
                label = {
                    if (labelText != null) {
                        Text(labelText)
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(textFieldValue.value.text) }) {
                Text("确认")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

