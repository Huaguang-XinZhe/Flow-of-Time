package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.utils.formatDurationInText
import java.time.Duration

@Composable
fun HorizontalBarChart(
    data: List<Pair<String, Float>>,
    referenceValue: Float,
    maxValue: Float,
    barHeight: Dp = 30.dp,
    spacing: Dp = 10.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (label: String) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {

        data.forEach { (label, value) ->
            val percent = "${(value/maxValue * 100).toInt()}%"
            val ratio = value /referenceValue
            val formattedDuration = formatDurationInText(Duration.ofMinutes(value.toLong()))

            if (ratio <= 0.16) {
                TextButton(onClick = { onClick(label) }) {
                    Text(
                        text = "$label -> $formattedDuration  $percent",
                        modifier = Modifier.padding(vertical = spacing)
                    )
                }
                return@forEach
            }

            // 类属
            Text(
                text = label,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = spacing, bottom = 5.dp)
            )
            // 条形
            Bar(
                percent = percent,
                ratio = ratio,
                durationText = formattedDuration,
                barHeight = barHeight,
                barColor = barColor,
                onClick = { onClick(label) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HorizontalBarChartPreview() {
    val sampleData = listOf(
        "Category AAAAAAAA" to 50f,
        "Category B" to 30f,
        "Category C" to 70f
    )
    HorizontalBarChart(data = sampleData, referenceValue = 70f, maxValue = 150f) {

    }
}

@Composable
fun Bar(
    percent: String,
    ratio: Float,
    durationText: String,
    barHeight: Dp,
    barColor: Color,
    onClick: () -> Unit
) {

//    RDALogger.info("formattedDuration = $formattedDuration, ratio = $ratio")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bar
        Box(
            modifier = Modifier
                .background(barColor, shape = RoundedCornerShape(8.dp))
//                .width(maxBarWidth * (value / maxValue)) // 这里必须用 dp，用 float 的条形是一样的，全部充满，不会有区分
                .weight(ratio)
                .height(barHeight)
                .clickable { onClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
//                text = value.toInt().toString(),
                text = durationText,
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Spacer
        Spacer(modifier = Modifier.width(8.dp))

        // Percentage
        Text(
            text = percent,
            color = barColor,
            fontWeight = FontWeight.SemiBold,
            modifier = if (ratio == 1f) Modifier else Modifier.weight((1 - ratio)) // invalid weight 0.0; must be greater than zero
        )
    }
}