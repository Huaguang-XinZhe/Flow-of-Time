package com.huaguang.flowoftime.ui.pages.inspiration_page

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

/**
 * 通用化基础 TabRow
 */
@Composable
fun MyTabRow(
    modifier: Modifier = Modifier,
    tabTitles: List<String>,
    selectedTabIndex: MutableState<Int>,
) {
    TabRow(
        selectedTabIndex = selectedTabIndex.value,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.value])
            )
        },
        modifier = modifier
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex.value == index,
                onClick = {
                    selectedTabIndex.value = index
                },
                text = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabLayoutExamplePreview() {
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Tab 1", "Tab 2", "Tab 3")

    MyTabRow(tabTitles = tabTitles, selectedTabIndex = selectedTabIndex)
}
