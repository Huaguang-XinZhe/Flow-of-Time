package com.huaguang.flowoftime.ui.pages.statistic_page

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foreverrafs.datepicker.DatePickerTimeline
import com.foreverrafs.datepicker.state.DatePickerState
import com.foreverrafs.datepicker.state.rememberDatePickerState
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.widget.HorizontalBarChart
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatisticPage(viewModel: StatisticViewModel = viewModel()) {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val combinedEvents = remember { mutableStateOf<List<CombinedEvent>>(listOf()) }
//    var combinedEvents = listOf<CombinedEvent>() // 必须用状态，否则变量值的变化不会引起重组
    var category = remember { "" }
    // 这里之所以可以不用状态，是因为整个页面并没有重组，所以这里没有恢复到初始值
    // 所以，要想尽可能少用状态，最好用 remember 块包裹，以避免重组时重置。
    var date = remember { getAdjustedEventDate().minusDays(1) }

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
        BarStatistics(
            onDateSelected = { selectedDate ->
                date = selectedDate
            }
        ) {
            coroutineScope.launch {
                combinedEvents.value = viewModel.getCombinedEventsByDateCategory(
                    date = date,
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
    onDateSelected: (LocalDate) -> Unit,
    onBarClick: (category: String) -> Unit
) {
//    val yesterdaysDailyStatistics by viewModel.yesterdaysDailyStatisticsFlow.collectAsState()
    // 确实，上面的状态在观察到新的值时页面会重组，但重组不代表这个赋值操作会执行啊！它只会在受影响的地方执行！所以，这里也必须用状态！
    val yesterday = remember { getAdjustedEventDate().minusDays(1) }
    val date = remember { mutableStateOf(yesterday) }
    val sumDuration = remember { mutableStateOf(Duration.ZERO) } // 奇怪，把它们放到 UI 里后只有一个变量是状态又不会触发重组了。
    var data = remember { listOf<Pair<String, Float>>() }
    var referenceValue = remember { 0f }
    val datePickerState = rememberDatePickerState(initialDate = yesterday)

    LaunchedEffect(Unit) {
        viewModel.deleteEntryByEmptyDuration()
    }

    LaunchedEffect(date.value) {
        val dailyStatistics = viewModel.getDailyStatisticsByDate(date.value)
        if (dailyStatistics.isEmpty()) {
            viewModel.sharedState.toastMessage.value = "当日无数据"
            sumDuration.value = Duration.ZERO // 这个也必须重置
            data = listOf() // 重置为初始值，要不然条形图还是会保留原先的状态
            return@LaunchedEffect
        }

        sumDuration.value = dailyStatistics // 计算昨天的时长总计
            .map { it.totalDuration }
            .fold(Duration.ZERO) { acc, duration -> acc + duration }
        data = dailyStatistics.map { // 获取横向条形图的数据
            it.category to it.totalDuration.toMinutes().toFloat()
        }
        referenceValue = data.first().second

    }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            DatePicker(datePickerState) { selectedDate: LocalDate ->
                if (selectedDate.isAfter(yesterday.plusDays(1))) {
                    viewModel.sharedState.toastMessage.value = "当日无数据"
                    sumDuration.value = Duration.ZERO // 这个也必须重置
                    data = listOf() // 重置为初始值，要不然条形图还是会保留原先的状态
                    return@DatePicker
                }
                onDateSelected(selectedDate)
                date.value = selectedDate
            }
        }

        item {
            Text(
                text = "日期：${date.value}",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            HorizontalBarChart( // 这是完整的一块，已经包含所有的条目了，不能放在 items 里边，否则会有很多很多个！！！
                data = data,
                referenceValue = referenceValue,
                maxValue = sumDuration.value.toMinutes().toFloat(),
            ) { category ->
                onBarClick(category)
            }
        }

        item {
            Text(
                text = "总计：${formatDurationInText(sumDuration.value)}",
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
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
                text = category,
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DatePicker(datePickerState: DatePickerState, onDateSelected: (LocalDate) -> Unit) {
    DatePickerTimeline(
        modifier = Modifier.wrapContentSize(),
        backgroundColor = MaterialTheme.colorScheme.background,
        state = datePickerState,
        selectedBackgroundColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = Color.White,
        dateTextColor = Color.Black,
        pastDaysCount = 10,  // The number of previous dates to display, relative to the initial date. Defaults to 120
        eventIndicatorColor = Color.DarkGray,
        onDateSelected = onDateSelected,
    )
}
