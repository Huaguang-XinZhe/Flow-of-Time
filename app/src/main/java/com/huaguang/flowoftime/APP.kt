package com.huaguang.flowoftime

import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.display_list.DisplayListPage
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordPage
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordPageViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APP() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
    ) { paddingValues ->

        RDALogger.info("bottomPadding = ${paddingValues.calculateBottomPadding()}")

        NavHost(
            navController = navController,
            startDestination = Page.Record.route,
//            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(Page.Record.route) {
                val buttonsViewModel = hiltViewModel<EventButtonsViewModel>()
                val inputViewModel = hiltViewModel<EventInputViewModel>()
                val regulatorViewModel = hiltViewModel<TimeRegulatorViewModel>()

                val timeRecordPageViewModel = TimeRecordPageViewModel(
                    buttonsViewModel, regulatorViewModel, inputViewModel
                )

                TimeRecordPage(viewModel = timeRecordPageViewModel)
            }
            composable(Page.List.route) {
                val inputViewModel = hiltViewModel<EventInputViewModel>()
                DisplayListPage(viewModel = inputViewModel)
            }
            composable(Page.Statistic.route) {
                StatisticPage()
            }
            composable(Page.Category.route) {
                CategoryPage()
            }

        }
    }
}

@Composable
fun BottomBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.background,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { page ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = page.iconRes),
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 3.dp) // 往 label 上加 topPadding 居然没有效果，往 Icon 上加还有哩
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = page.labelRes),
                        fontSize = 12.sp,
                    )
                },
                selected = currentDestination?.hierarchy?.any { it.route == page.route } == true, // 这里的 hierarchy 还是不太明白
                onClick = {
                    navController.navigate(page.route) {
                        // 弹出（清除）跳转前页面在回退栈之前的所有栈条目，不包括页面本身
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true // 避免多次点击产生多个栈条目
                        restoreState = true // 再次点击之前的 item，恢复状态
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary, // 这个改变的是选中 Tab 水波纹的颜色
            )
        }
    }
}

@Composable
fun StatisticPage() {

    Text(text = "敬请期待")

}

@Composable
fun CategoryPage() {
    Text(text = "有待开发")
}