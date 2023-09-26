package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.utils.formatDurationInText
import java.time.Duration

@Composable
fun HorizontalBarChart(
    data: List<Pair<String?, Float>>,
    referenceValue: Float,
    maxValue: Float,
    barHeight: Dp = 30.dp,
    spacing: Dp = 10.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (String?) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.SpaceAround
    ) {

        data.forEach { (label, value) ->
            val labelNotNull = label ?: "❓"
            val percent = (value/maxValue * 100).toInt()
            val ratio = if (referenceValue != 0f) value / referenceValue else 0f
            val formattedDuration = formatDurationInText(Duration.ofMinutes(value.toLong()))

            if (ratio <= 0.175) {
                TextButton(onClick = { onClick(label) }) {
                    Text(
                        text = "$labelNotNull -> $formattedDuration  $percent%",
                        modifier = Modifier.padding(vertical = spacing)
                    )
                }
                return@forEach
            }

            // 类属
            Text(
                text = labelNotNull,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = spacing, bottom = 5.dp)
            )
            // 条形
            Bar(
                percent = "$percent%",
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
        "核心" to 181f,
        "框架" to 176f,
        "常务" to 171f,
        "休息" to 70f,
        "xxx" to 41f,
        "家人" to 31f,
        "违破" to 24f,
    )
//    HorizontalBarChart(data = sampleData, referenceValue = 181f, maxValue = 696f) {
//
//    }
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
    Layout(
        content = {
            Box(
                modifier = Modifier
                    .background(barColor, shape = RoundedCornerShape(8.dp))
                    .height(barHeight)
                    .clickable { onClick() },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = durationText,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = percent,
                color = barColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp)
            )
        },
        measurePolicy = { measurables, constraints ->
            val barMeasurable = measurables[0]
            val textMeasurable = measurables[1]

            val textPlaceable = textMeasurable.measure(constraints)
            val barWidth = constraints.maxWidth - textPlaceable.width
            val barPlaceable = barMeasurable.measure(
                Constraints.fixedWidth((barWidth * ratio).toInt())
            )

            val width = constraints.maxWidth
            val height = maxOf(barPlaceable.height, textPlaceable.height)

            layout(width, height) {
                barPlaceable.place(0, 0)
                textPlaceable.place(barPlaceable.width, (height - textPlaceable.height) / 2)
            }
        }
    )
}
