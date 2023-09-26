package com.huaguang.flowoftime.ui.pages.statistics_page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huaguang.flowoftime.utils.formatDateToCustomPattern
import com.huaguang.flowoftime.utils.formatDurationInText
import java.time.Duration

@Composable
fun OverviewPage() {
    DateDurationColumn()
}

@Composable
fun DateDurationColumn(viewModel: OverviewPageViewModel = viewModel()) {
    val loadButtonShow by viewModel.loadButtonShow
    val statisticsByCategory = viewModel.statisticsByCategory
    val averageDurationByCategory = viewModel.averageDurationByCategory

    if (loadButtonShow) {
        Button(
            onClick = { viewModel.loadDateDurationData() },
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Text(text = "加载不同类属下的日期时长数据")
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            statisticsByCategory.forEach { (category, dailyStatistics) ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                        )) {
                            append(category ?: "❓")
                        }
                        val averageDuration = averageDurationByCategory[category] ?: Duration.ZERO
                        append("（均）${formatDurationInText(averageDuration)}")
                    },
                    modifier = Modifier.padding(bottom = 5.dp)
                )

                dailyStatistics.forEach { dailyStatistic ->
                    val date = dailyStatistic.date
                    val duration = dailyStatistic.totalDuration

                    Text(
                        text = "${formatDateToCustomPattern(date)} -> ${formatDurationInText(duration)}",
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}