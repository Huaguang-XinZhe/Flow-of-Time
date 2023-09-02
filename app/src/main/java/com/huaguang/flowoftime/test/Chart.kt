package com.huaguang.flowoftime.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LineChart(points: List<Offset>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = Color.Blue
                style = PaintingStyle.Stroke
            }
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                canvas.drawLine(start, end, paint)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LineChartPreview() {
    // 模拟的 Offset 点列表
    val points = listOf(
        Offset(50f, 50f),
        Offset(100f, 100f),
        Offset(150f, 50f),
        Offset(200f, 100f),
        Offset(250f, 50f)
    )
    LineChart(points)
}