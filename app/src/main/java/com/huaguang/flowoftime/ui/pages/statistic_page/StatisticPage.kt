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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foreverrafs.datepicker.DatePickerTimeline
import com.foreverrafs.datepicker.state.DatePickerState
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.ui.components.toggle_item.DRToggleItem
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.widget.HorizontalBarChart
import com.huaguang.flowoftime.utils.formatDurationInText
import com.huaguang.flowoftime.utils.getAdjustedEventDate
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatisticPage(
    datePickerState: DatePickerState,
    viewModel: StatisticViewModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val combinedEvents by viewModel.combinedEvents.collectAsState()
    val category by viewModel.category
    val date by viewModel.date.collectAsState()

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
        sheetContent = {
            SpecificItemsUnderCategory(combinedEvents, category) // 两个参数，只要有一个是状态，引发重组即可
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

            BarStatistics { barCategory ->
                coroutineScope.launch {
                    viewModel.fetchCombinedEventsByDateCategory(date, barCategory)
                    sheetState.show() // 显示
                }
            }
        }
    }
}


@Composable
fun BarStatistics(
    viewModel: StatisticViewModel = viewModel(),
    onBarClick: (category: String) -> Unit
) {
    val sumDuration by viewModel.sumDuration.collectAsState()
    val data by viewModel.data.collectAsState()
    val referenceValue by viewModel.referenceValue.collectAsState()

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            HorizontalBarChart( // 这是完整的一块，已经包含所有的条目了，不能放在 items 里边，否则会有很多很多个！！！
                data = data,
                referenceValue = referenceValue,
                maxValue = sumDuration.toMinutes().toFloat(),
            ) { category ->
                onBarClick(category)
            }
        }

        item {
            Text(
                text = "总计：${formatDurationInText(sumDuration)}",
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
        eventDates = listOf(getAdjustedEventDate())
    )
}
