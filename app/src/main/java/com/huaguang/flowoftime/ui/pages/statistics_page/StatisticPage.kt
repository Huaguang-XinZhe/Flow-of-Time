package com.huaguang.flowoftime.ui.pages.statistics_page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ardakaplan.rdalogger.RDALogger
import com.foreverrafs.datepicker.DatePickerTimeline
import com.foreverrafs.datepicker.state.DatePickerState
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.DashShowToggleButton
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.widget.HorizontalBarChart
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.utils.getAdjustedDate
import com.huaguang.flowoftime.utils.space
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatisticsPage(
    datePickerState: DatePickerState,
    viewModel: StatisticViewModel = viewModel(),
) {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(viewModel.size.intValue) {
        RDALogger.info("size = ${viewModel.size.intValue}")
        if (viewModel.size.intValue == 0) { // 为 0 才隐藏！
            sheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        sheetContent = {
            SpecificItemsUnderCategory()
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DatePicker(datePickerState) {
                viewModel.onDateSelected(it)
            }

            StatisticsColumn { barCategory ->
                coroutineScope.launch {
                    viewModel.category.value = barCategory
                    sheetState.show() // 显示
                }
            }
        }
    }
}


@Composable
fun StatisticsColumn(
    viewModel: StatisticViewModel = viewModel(),
    onBarClick: (category: String?) -> Unit
) {
    val sumDuration by viewModel.sumDuration.collectAsState()
    val map = viewModel.map
    val displayXxxText by viewModel.displayXxxText
    val context = LocalContext.current

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            DaySummaryText()
        }

        item {
            map.keys.forEach { key ->
                val data = map[key] ?: return@forEach
                val maxValue = data.map { pair -> pair.second }.sum()
                val totalDurationStr = formatDurationInText(Duration.ofMinutes(maxValue.toLong()))

                Text(
                    text = "${key ?: "❓"}：$totalDurationStr",
                    fontWeight = FontWeight.SemiBold
                )

                HorizontalBarChart(
                    // 这是完整的一块，已经包含所有的条目了，不能放在 items 里边，否则会有很多很多个！！！
                    data = data,
                    referenceValue = data.first().second, // TODO: 既然是固定的逻辑，那为什么不隐入组件内部？
                    maxValue = maxValue,
                ) { category ->
                    onBarClick(category)
                }
            }
        }

        item {
            if (sumDuration == Duration.ZERO) return@item

            Text(
                text = "总计：${formatDurationInText(sumDuration)}",
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        item {
            DateDurationColumn()
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { viewModel.getXXXData() }) {
                        Text(text = "获取 xxx 数据")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(onClick = { viewModel.copyXXXData(context) }) {
                        Text(text = "复制抗性数据")
                    }
                }

                Text(text = displayXxxText)
            }
        }

//        item {
//            Button(
//                onClick = { viewModel.deleteAllOfTheDay() },
//                modifier = Modifier.padding(bottom = 20.dp),
//            ) {
//                Text(text = "删除当日全部数据")
//            }
//        }

    }

}



@Composable
fun SpecificItemsUnderCategory(
    viewModel: StatisticViewModel = viewModel()
) {
    val toggleMap = remember { mutableMapOf<Long, ItemState>() }
    val dashButtonShow = remember { mutableStateOf(false) }
    val date by viewModel.date.collectAsState()
    val category by viewModel.category
    val map = viewModel.categoryDurationMap

    val combinedEvents by produceState(initialValue = emptyList<CombinedEvent>(), category) {
        if (category == "-1") return@produceState // 初始化的时候不要执行 lambda
        viewModel.getCombinedEventsFlow(date, category).collect { combinedEventList ->
            value = combinedEventList
            viewModel.size.intValue = combinedEventList.size
        }
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
//                RDALogger.info("map[category] = ${map[category]}")
                Text(
                    text = "${category ?: "❓"} ${formatDurationInText(map[category] ?: Duration.ZERO)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 15.dp),
                )

                DashShowToggleButton(
                    dashButtonShow = dashButtonShow,
                    modifier = Modifier.padding(start = 5.dp)
                )

            }
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

@Composable
fun DaySummaryText(viewModel: StatisticViewModel = viewModel()) {
    val wakeUpTime by viewModel.wakeUpTime.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val nextWakeUpTime by viewModel.nextWakeUpTime.collectAsState()
    val date by viewModel.date.collectAsState()
    val sumDuration by viewModel.sumDuration.collectAsState()

    if (wakeUpTime == null || sleepTime == null) return

    val firstWakeUpText = "（起床）${formatLocalDateTime(wakeUpTime!!)}"

    val text = if (date.isEqual(getAdjustedDate())) { // 如果是当天
        val duration = Duration.between(wakeUpTime, LocalDateTime.now())
        "$firstWakeUpText -> ${formatLocalDateTime(LocalDateTime.now())}（当前）\n\n" +
                "${space(21)}${formatDurationInText(duration)}"
    } else {
        val firstDuration = Duration.between(wakeUpTime, sleepTime)
        val secondDuration = Duration.between(sleepTime, nextWakeUpTime)
        val diffDurationText = formatDurationInText(firstDuration + secondDuration - sumDuration)
        "$firstWakeUpText -> ${formatLocalDateTime(sleepTime!!)}（入睡）-> ${formatLocalDateTime(nextWakeUpTime!!)}（次日起床）\n\n" +
                "${space(21)}${formatDurationInText(firstDuration)}${space(17)}${formatDurationInText(secondDuration)}\n\n" +
                "${space(38)}间隙：$diffDurationText"
    }

    Text(
        text = text,
        modifier = Modifier.padding(vertical = 10.dp)
    )
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
//        pastDaysCount = 10,  // The number of previous dates to display, relative to the initial date. Defaults to 120
        eventIndicatorColor = Color.DarkGray,
        onDateSelected = onDateSelected,
        eventDates = listOf(getAdjustedDate())
    )
}
