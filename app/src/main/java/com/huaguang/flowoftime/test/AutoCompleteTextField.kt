package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 能基本实现，但体验很不好，还没输完软键盘就收起来了，还没清完也是。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AutoCompleteTextField(
    suggestions: List<String>,
    label: String
) {
    var text by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onItemSelected: (String) -> Unit = {
        text = it
        isDropdownExpanded = false
        keyboardController?.hide()
    }

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                isDropdownExpanded = true
            },
            label = { Text(label) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    isDropdownExpanded = false
                    keyboardController?.hide()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            suggestions.filter { it.contains(text, true) }.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = { onItemSelected(suggestion) },
                    text = {
                        Text(text = suggestion)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AutoCompleteExample() {
    val suggestions = listOf("Apple", "Banana", "Cherry", "Date", "Elderberry")

    AutoCompleteTextField(
        suggestions = suggestions,
        label = "Fruit"
    )
}
