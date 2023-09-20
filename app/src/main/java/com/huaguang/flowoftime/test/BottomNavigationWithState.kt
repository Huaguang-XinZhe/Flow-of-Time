package com.huaguang.flowoftime.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.Page
import com.huaguang.flowoftime.tabs

@Preview(showBackground = true)
@Composable
fun BottomNavigationWithState() {
    val selectedTab = remember { mutableStateOf(tabs[0]) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 你的内容区域
        when (selectedTab.value) { // 只有选中项的 Composable 会重新组合
            Page.Record -> FirstScreen()
            Page.List -> SecondScreen()
            Page.Statistic -> ThirdScreen()
            Page.Category -> FourthScreen()
        }

        // 你的底部导航栏
        BottomNavigation {
            tabs.forEach { page ->
                BottomNavigationItem(
                    icon = { Icon(painterResource(id = page.iconRes), contentDescription = null) },
                    label = { Text(stringResource(id = page.labelRes)) },
                    selected = selectedTab.value == page,
                    onClick = { selectedTab.value = page } // 重复点击也没有关系，Composable 只会重组一次
                )
            }
        }
    }
}

@Composable
fun FirstScreen() {
    RDALogger.info("FirstScreen 重新组合")
    // 你的第一个屏幕的内容
    Text(text = "FirstScreen")
}

@Composable
fun SecondScreen() {
    RDALogger.info("SecondScreen 重新组合")
    // 你的第二个屏幕的内容
    Text(text = "SecondScreen")
}

@Composable
fun ThirdScreen() {
    RDALogger.info("ThirdScreen 重新组合")
    // 你的第三个屏幕的内容
    Text(text = "ThirdScreen")
}

@Composable
fun FourthScreen() {
    RDALogger.info("FourthScreen 重新组合")
    // 你的第四个屏幕的内容
    Text(text = "FourthScreen")
}
