package com.huaguang.flowoftime.ui.pages.statistic_page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.ui.widget.HorizontalBarChart
import com.huaguang.flowoftime.utils.formatDurationInText


@Composable
fun StatisticalList(
    viewModel: StatisticalListViewModel = viewModel()
) {
//    val yesterdaysDailyStatistics by viewModel.yesterdaysDailyStatisticsFlow.collectAsState()
    // 确实，上面的状态在观察到新的值时页面会重组，但重组不代表这个赋值操作会执行啊！它只会在受影响的地方执行！所以，这里也必须用状态！
    val sumDuration by viewModel.sumDuration

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "日期："
            )
        }

//        items(yesterdaysDailyStatistics) { item: DailyStatistics ->
////            if (item.totalDuration == Duration.ZERO) return@items // 不能这样写，会报错！
//
//            if (item.totalDuration != Duration.ZERO) {
//                Text(
//                    text = "${item.category} -> ${formatDurationInText(item.totalDuration)}",
//                    modifier = Modifier.padding(10.dp)
//                )
//            }
//        }

        item {
            HorizontalBarChart( // 这是完整的一块，已经包含所有的条目了，不能放在 items 里边，否则会有很多很多个！！！
                data = viewModel.data,
                referenceValue = viewModel.referenceValue,
                maxValue = sumDuration.toMinutes().toFloat(),
            )
        }

        item {
            Text(
                text = "总计：${formatDurationInText(sumDuration)}"
            )
        }



    }
}




