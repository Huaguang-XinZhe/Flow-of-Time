package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale

/**
 * 能实现基本的搜索、高亮功能，但匹配规则还有待完善。
 */
@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchWithHighlight() {
    val query = remember { mutableStateOf("") }
    val items = listOf("Apple", "Banana", "Cherry")
    val filteredItems = items.filter { it.contains(query.value, ignoreCase = true) }

    Column {
        TextField(
            value = query.value,
            onValueChange = { newValue -> query.value = newValue },
            label = { Text("Search") }
        )

        LazyColumn {
            items(filteredItems) { item ->
                Text(text = highlightText(item, query.value))
            }
        }
    }
}

fun highlightText(text: String, query: String): AnnotatedString {
    val lowerCaseText = text.lowercase(Locale.getDefault())
    val lowerCaseQuery = query.lowercase(Locale.getDefault())
    val startIndex = lowerCaseText.indexOf(lowerCaseQuery)
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.Black)) {
            append(text)
        }
        if (startIndex >= 0) {
            addStyle(
                SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold),
                startIndex,
                startIndex + query.length
            )
        }
    }
}
