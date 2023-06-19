package com.huaguang.flowoftime.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.huaguang.flowoftime.ui.components.EventTrackerMediator
import com.huaguang.flowoftime.ui.screens.event_tracker.EventTrackerScreen

@Composable
fun MyApp(mediator: EventTrackerMediator) {

    // 获取系统 UI 控制器
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight

    // 在 SideEffect 中更新系统 UI
    SideEffect {

        // 设置状态栏图标的颜色
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    // 其他 UI
    EventTrackerScreen(mediator = mediator)

}