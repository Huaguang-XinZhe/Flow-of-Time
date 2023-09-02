package com.huaguang.flowoftime.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.components.DisplayEventItem
import com.huaguang.flowoftime.ui.components.EventDisplay
import com.huaguang.flowoftime.ui.components.EventType
import com.huaguang.flowoftime.ui.theme.FlowOfTimeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.Duration
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity2 : ComponentActivity() {
    
    @Inject
    lateinit var iconRepository: IconMappingRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        iconRepository.preloadData()  // 从数据库预加载映射数据到内存中
        
        setContent {
            FlowOfTimeTheme {
                test(iconRepository = iconRepository)
            }
        }

    }
}

@Composable
fun test(iconRepository: IconMappingRepository) {
    val event1 = EventDisplay(
        name = "昨日作息统析，今日计划",
        duration = Duration.ofHours(1) + Duration.ofMinutes(15),
        type = EventType.STEP,
    )

    val event2 = EventDisplay(
        name = "老妈来电",
        duration = Duration.ofMinutes(15),
        type = EventType.INSERT
    )

    val event3 = EventDisplay(
        name = "项目更新",
        duration = Duration.ofMinutes(42),
        type = EventType.STEP,
        contentEvents = listOf(event2)
    )


    val event4 = EventDisplay(
        name = "尚硅谷 HTML + CSS 学习",
        duration = Duration.ofMinutes(35),
        type = EventType.FOLLOW,
    )


    val eventDisplay = EventDisplay(
        name = "时间统计法",
        duration = Duration.ofHours(3) + Duration.ofMinutes(35),
        type = EventType.SUBJECT,
        category = "个人框架",
        tags = listOf("时间统计法", "自我应用", "项目", "非当前核心", "时间", "个人管理", "SB"),
        contentEvents = listOf(
            event1, event3, event4
        )
    )

    DisplayEventItem(eventDisplay = eventDisplay, iconRepository)

}