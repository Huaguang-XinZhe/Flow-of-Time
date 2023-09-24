package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.widget.HorizontalBarChart
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatisticPage(viewModel: StatisticViewModel = viewModel()) {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val combinedEvents = remember { mutableStateOf<List<CombinedEvent>>(listOf()) }
//    var combinedEvents = listOf<CombinedEvent>() // 必须用状态，否则变量值的变化不会引起重组
    var category = ""

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        sheetContent = {
            SpecificItemsUnderCategory(combinedEvents.value, category) // 两个参数，只要有一个是状态，引发重组即可
//                       Text(text = "中国")
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        BarStatistics {
            coroutineScope.launch {
                combinedEvents.value = viewModel.getCombinedEventsByDateCategory(
                    date = getAdjustedEventDate().minusDays(1),
                    category = it
                )
                category = it
                sheetState.show() // 显示
            }
        }
    }
}


@Composable
fun BarStatistics(
    viewModel: StatisticViewModel = viewModel(),
    onBarClick: (category: String) -> Unit
) {
//    val yesterdaysDailyStatistics by viewModel.yesterdaysDailyStatisticsFlow.collectAsState()
    // 确实，上面的状态在观察到新的值时页面会重组，但重组不代表这个赋值操作会执行啊！它只会在受影响的地方执行！所以，这里也必须用状态！
    val sumDuration by viewModel.sumDuration


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "日期："
        )

        HorizontalBarChart( // 这是完整的一块，已经包含所有的条目了，不能放在 items 里边，否则会有很多很多个！！！
            data = viewModel.data,
            referenceValue = viewModel.referenceValue,
            maxValue = sumDuration.toMinutes().toFloat(),
        ) { category ->
            onBarClick(category)
        }

        Text(
            text = "总计：${formatDurationInText(sumDuration)}"
        )
    }

}

@Composable
fun SpecificItemsUnderCategory(combinedEvents: List<CombinedEvent>, category: String) {
    val toggleMap = remember { mutableMapOf<Long, ItemState>() }

    LaunchedEffect(Unit) {
        combinedEvents.forEach { event ->
            toggleMap[event.event.id] = ItemState.initialDisplay()
        }
    }

    LazyColumn(
        // 这里如果设为 fillMaxSize() 的话，就先是半屏呈现，上拉的话，会全屏
        // 必须加这个（height），要不然会崩溃，报错：The initial value must have an associated anchor.
        modifier = Modifier
            .wrapContentSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "${category}下具体条目",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp)
            )
        }

        items(combinedEvents) { item ->
            DRToggleItem(
                modifier = Modifier.padding(bottom = 5.dp),
                itemState = toggleMap[item.event.id] ?: ItemState.initialDisplay(),
                combinedEvent = item,
            )
        }
    }
}
