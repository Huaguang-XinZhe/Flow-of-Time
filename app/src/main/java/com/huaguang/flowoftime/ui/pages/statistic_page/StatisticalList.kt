package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.data.models.tables.DailyStatistics
import com.huaguang.flowoftime.utils.formatDurationInText

@Composable
fun StatisticalList(
    viewModel: StatisticalListViewModel = viewModel()
) {
    val yesterdaysDailyStatistics by viewModel.yesterdaysDailyStatisticsFlow.collectAsState()
    val sumDuration by viewModel.sumDuration // 这是一个状态，当它的值发生改变的时候，组件会重组

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "日期：2023-09-21"
            )
        }

        items(yesterdaysDailyStatistics) { item: DailyStatistics ->
            Text(
                text = "${item.category} -> ${formatDurationInText(item.totalDuration)}",
                modifier = Modifier.padding(10.dp)
            )
        }

        item {
            Text(
                text = "总计：${formatDurationInText(sumDuration)}"
            )
        }
    }
}