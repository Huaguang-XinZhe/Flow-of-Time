package com.huaguang.flowoftime.test

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.Page
import com.huaguang.flowoftime.tabs
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.display_list.DisplayListPage
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordPage
import com.huaguang.flowoftime.ui.pages.time_record.TimeRecordPageViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APP2() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) },
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Page.Record.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(Page.Record.route) {
                val buttonsViewModel = hiltViewModel<EventButtonsViewModel>()
                val inputViewModel = hiltViewModel<EventInputViewModel>()
                val regulatorViewModel = hiltViewModel<TimeRegulatorViewModel>()

                val timeRecordPageViewModel = remember {
                    TimeRecordPageViewModel(buttonsViewModel, regulatorViewModel, inputViewModel)
                } // 防止切回记录页时重新创建 ViewModel 实例

                RDALogger.info("记录页 composable")
                TimeRecordPage(
                    viewModel = timeRecordPageViewModel,
                    onNavigation = { route ->
                        navController.navigate(route)
                    }
                )
            }
            composable(Page.List.route) {
                val inputViewModel = hiltViewModel<EventInputViewModel>()
                RDALogger.info("展示页 composable")
                DisplayListPage(viewModel = inputViewModel)
                RDALogger.info("页面显示：${System.currentTimeMillis()}")
            }
            composable(Page.Statistic.route) {
                RDALogger.info("统计页 composable")
                StatisticPage()
            }
            composable(Page.Category.route) {
                RDALogger.info("类属页 composable")
                CategoryPage()
            }

        }
    }

    // 添加监听器来打印当前的回退栈
    DisposableEffect(navController) { // 这一部分放在 Scaffold 前边还是后边不会影响起始 route 为 null 的情况。
        val listener = NavController.OnDestinationChangedListener { controller, _, _ ->
            // 这里的 map 是从一个列表映射到另一个列表，外包装不变，里边的元素变了
            // backStackEntries 就是回退栈内的路由列表
            val backStackEntries = controller.backQueue.map { it.destination.route } // route 最开始为 null
            Log.d("NavBackStack", backStackEntries.joinToString(" -> "))
        }
        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
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
        Log.d("NavBackStack", currentDestination?.route ?: "route 为空")

        tabs.forEach { page ->
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
                selected = currentDestination?.route == page.route,
                onClick = {
                    RDALogger.info("点击：${System.currentTimeMillis()}")
                    Log.d("NavBackStack", "点击的页面，${page.route}")
                    navController.navigate(page.route) {
                        popUpTo(Page.Record.route) {// 无论切换到哪个 Tab，每次返回都进入记录页
                            saveState = true
                        }
                        launchSingleTop = true // 避免多次点击产生多个栈条目
                        restoreState = true
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary, // 这个改变的是选中 Tab 水波纹的颜色
            )
        }
    }
}

@Composable
fun StatisticPage() {

//    RDALogger.info("统计页重新组合")
    Text(text = "敬请期待")

}

@Composable
fun CategoryPage() {
//    RDALogger.info("类属页重新组合")
    Text(text = "有待开发")
}