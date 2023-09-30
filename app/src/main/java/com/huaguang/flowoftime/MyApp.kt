package com.huaguang.flowoftime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.foreverrafs.datepicker.state.rememberDatePickerState
import com.huaguang.flowoftime.ui.components.category_dialog.ClassNameInputAlertDialog
import com.huaguang.flowoftime.ui.components.event_input.EventInputField
import com.huaguang.flowoftime.ui.pages.display_list.DisplayListPage
import com.huaguang.flowoftime.ui.pages.inspiration_page.InspirationPage
import com.huaguang.flowoftime.ui.pages.statistics_page.StatisticsPage
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordPage
import com.huaguang.flowoftime.utils.getAdjustedDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp(appViewModels: AppViewModels) {
//    val selectedTab = remember { mutableStateOf(tabs[0]) }
    val selectedTab = rememberSaveable { mutableStateOf(tabs[0]) } // 配置更改或被系统杀内存时将保存这个状态（里边类型需要 Parcelable）
    val datePickerState = rememberDatePickerState(initialDate = getAdjustedDate().minusDays(1))

    Scaffold(
        bottomBar = { BottomBar(selectedTab) },
    ) { paddingValues ->
        val navController = rememberNavController()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // 通过 when 切换会快一些，但每次切换还是会重新组合，页面并不能保持
            appViewModels.apply {
                when (selectedTab.value) { // 只有选中项的 Composable 会重新组合
                    Page.Record -> {
                        TimeRecordPage(
                            eventControlViewModel = eventControlViewModel,
                            buttonsViewModel = buttonsViewModel,
                            regulatorViewModel = regulatorViewModel,
                        ) { route ->
                            navController.navigate(route)
                        }
                    }
                    Page.List -> DisplayListPage()
                    Page.Statistic -> {
                        StatisticsPage(datePickerState)
                    }
                    Page.Category -> Text(text = "类属页，敬请期待")
                    Page.Inspiration -> InspirationPage()
                }
            }

        }

        val topPadding = when(selectedTab.value) {
            Page.Record -> 450.dp
            Page.List -> 600.dp
            else -> 500.dp
        }
        EventInputField(
            modifier = Modifier.padding(top = topPadding)
        )

        ClassNameInputAlertDialog()
    }
}

@Composable
fun BottomBar(selectedTab: MutableState<Page>) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        tabs.forEach { page ->
            val selected = selectedTab.value == page
            val selectedColor = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = page.iconRes),
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.padding(bottom = 3.dp) // 往 label 上加 topPadding 居然没有效果，往 Icon 上加还有哩
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = page.labelRes),
                        fontSize = 12.sp,
                        color = selectedColor,
                    )
                },
                selected = selected,
                onClick = { selectedTab.value = page }, // 重复点击也没有关系，Composable 只会重组一次
                selectedContentColor = MaterialTheme.colorScheme.primary, // 这个改变的是选中 Tab 水波纹的颜色
            )
        }
    }
}