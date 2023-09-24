package com.huaguang.flowoftime.test


import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter


@Preview(showBackground = true)
@Composable
fun BarChart() {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {

                setDrawBarShadow(false)
//                setDrawValueAboveBar(true)

                description.isEnabled = false

                setMaxVisibleValueCount(60)

                setPinchZoom(false)

                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setLabelCount(8, false)
                    setDrawGridLines(false)
                    granularity = 1f
                    labelCount = 7
//                    valueFormatter =
                }

                axisLeft.apply {
                    setLabelCount(8, false)
                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
//                    spaceTop = 100f
                    axisMinimum = 0f // this replaces setStartAtZero(true)
                }

                axisRight.apply {
                    setDrawGridLines(false)
                    setLabelCount(8, false)
//                    spaceTop = 100f
                    axisMinimum = 0f
                }

                // 设置图表属性和数据
                val entries = mutableListOf<BarEntry>().apply {
                    add(BarEntry(1f, 10f))
                    add(BarEntry(2f, 20f))
                    add(BarEntry(3f, 30f))
                }

                val dataSet = BarDataSet(entries, "数据标签")
                val data = BarData(dataSet).apply {
                    setValueTextSize(10f)
//                    barWidth = 0.9f
                }
                setData(data)

                invalidate()

            }
        },
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun HorizontalBarChartComposable() {
    AndroidView(
        factory = { context ->
            HorizontalBarChart(context).apply {
//                // 禁止水平、纵向缩放
//                isScaleXEnabled = false
//                isScaleYEnabled = false
//                // 不绘制网格背景
//                setDrawGridBackground(false)
//                // 不绘制描述
//                description.isEnabled = false
//                // 不绘制图例
//                legend.isEnabled = false
//
//                // 禁用X轴和Y轴的网格线和轴线
//                xAxis.apply {
//                    setDrawGridLines(false)
//                    setDrawAxisLine(false)
////                    setDrawLabels(false)  // 如果您也想隐藏X轴的标签
//                }
//                axisLeft.apply {
//                    setDrawGridLines(false)
//                    setDrawAxisLine(false)
//                    setDrawLabels(false)  // 如果您也想隐藏Y轴的标签
//                }
//                axisRight.apply {
//                    setDrawGridLines(false)
//                    setDrawAxisLine(false)
//                    setDrawLabels(false)  // 如果您也想隐藏Y轴的标签
//                }

//                setDrawBarShadow(false)
//                setDrawValueAboveBar(true)

                // 禁止水平、纵向缩放
                isScaleXEnabled = false
                isScaleYEnabled = false

                // 不绘制图例
                legend.isEnabled = false

                description.isEnabled = false

                setMaxVisibleValueCount(60)

                setPinchZoom(false)

                setDrawGridBackground(false)

                xAxis.apply {
//                    position = XAxis.XAxisPosition.BOTTOM
//                    setLabelCount(8, false)
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
//                    granularity = 1f
//                    labelCount = 7
//                    valueFormatter =
                }

                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
//                    setLabelCount(8, false)
//                    setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
//                    spaceTop = 100f
                    axisMinimum = 0f // 终于找到了，关键就在这里，纵轴必须要有这一句，如果没有，就不会显示值！！！
                }

                axisRight.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)
//                    setLabelCount(8, false)
////                    spaceTop = 100f
//                    axisMinimum = 0f
                }

                // 设置图表属性和数据
                val entries = mutableListOf<BarEntry>().apply {
                    add(BarEntry(1f, 1f))
                    add(BarEntry(3f, 2f))
                    add(BarEntry(5f, 3f))
                }

                val dataSet = BarDataSet(entries, "数据标签")
                val data = BarData(dataSet).apply {
                    setValueTextSize(10f)
                }

                setData(data)

                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )

}

@Composable
fun HorizontalBarChartComposable2(
    entries: List<BarEntry>,
//    categories: List<String>,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            HorizontalBarChart(context).apply {
                val dataSet = BarDataSet(entries, "数据标签").apply {
//                    highLightColor = Color.RED  // 设置高亮颜色
                    isHighlightPerTapEnabled = false // 禁用点击高亮
                }
                val data = BarData(dataSet).apply {
                    setValueTextSize(10f)
                }

                setData(data)

                // 禁止水平、纵向缩放
                isScaleXEnabled = false
                isScaleYEnabled = false
                // 不绘制网格背景
                setDrawGridBackground(false)
                // 不绘制描述
                description.isEnabled = false
                // 不绘制图例
                legend.isEnabled = false

                // 禁用X轴和Y轴的网格线和轴线
                xAxis.apply {
                    setDrawGridLines(false)
                    position = XAxis.XAxisPosition.BOTTOM

//                    setDrawAxisLine(false)
//                    setDrawLabels(false)  // 如果您也想隐藏X轴的标签
                }
                axisLeft.apply {
                    setDrawGridLines(false)
//                    setDrawAxisLine(false)
//                    setDrawLabels(false)  // 如果您也想隐藏Y轴的标签
                    axisMinimum = 0f // 必须写，要不然值不会显示
                }
                axisRight.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    setDrawLabels(false)  // 如果您也想隐藏Y轴的标签
                }

//                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                    override fun onValueSelected(e: Entry?, h: Highlight?) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onNothingSelected() {
//                        TODO("Not yet implemented")
//                    }
//
//                })

                invalidate()

            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}


@Composable
fun PieChartComposable() {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                // 设置图表属性和数据
                val entries = mutableListOf<PieEntry>().apply {
                    add(PieEntry(10f, "分类1"))
                    add(PieEntry(20f, "分类2"))
                    add(PieEntry(30f, "分类3"))
                }

                val dataSet = PieDataSet(entries, "数据标签")
                val data = PieData(dataSet)
                this.data = data
                invalidate()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun DetailedPieChartComposable() {
    AndroidView(factory = { context ->
        PieChart(context).apply {
            // 1. 设置饼图的描述
            description.apply {
                text = "销售数据"
                textSize = 12f
                textColor = Color.BLACK
                setPosition(500f, 500f)
            }

            // 2. 设置饼图的数据
            val entries = mutableListOf<PieEntry>().apply {
                add(PieEntry(10f, "产品A"))
                add(PieEntry(20f, "产品B"))
                add(PieEntry(30f, "产品C"))
            }

            val dataSet = PieDataSet(entries, "产品类别").apply {
                // 3. 设置数据集的颜色
                colors = listOf(Color.RED, Color.GREEN, Color.BLUE)

                // 4. 设置数据集的值的文字大小和颜色
                valueTextSize = 12f
                valueTextColor = Color.BLACK

                // 5. 设置数据集的选中效果
                setDrawValues(true)
                setAutomaticallyDisableSliceSpacing(true) // 自动切片间距（饼图切片太小，就不显示）
            }

            val data = PieData(dataSet).apply {
                // 6. 设置值的格式，例如保留两位小数
                setValueFormatter(object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return String.format("%.2f%%", value)
                    }
                })
            }

            this.data = data

            // 7. 设置饼图的旋转角度
            rotationAngle = 120f

            // 8. 设置饼图的交互
            isRotationEnabled = true

            // 9. 设置饼图的动画
            animateY(1500)

            // 设置饼图中间的文字
            centerText = "总销售额"
            setCenterTextSize(12f)
            setCenterTextColor(Color.BLACK)

            // 设置图例
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }

            // 10. 刷新图表
            invalidate()
        }
    }, modifier = Modifier.fillMaxSize())
}

@Composable
fun DetailedHorizontalBarChartComposable() {
    AndroidView(factory = { context ->
        HorizontalBarChart(context).apply {
            // 1. 设置图表的描述
            description.apply {
                text = "销售数据"
                textSize = 12f
                textColor = Color.BLACK
            }

            // 2. 设置图表的数据
            val entries = mutableListOf<BarEntry>().apply {
                add(BarEntry(0f, 10f))
                add(BarEntry(1f, 20f))
                add(BarEntry(2f, 30f))
            }

            val dataSet = BarDataSet(entries, "产品类别").apply {
                // 3. 设置数据集的颜色
                colors = listOf(Color.RED, Color.GREEN, Color.BLUE)

                // 4. 设置数据集的值的文字大小和颜色
                valueTextSize = 12f
                valueTextColor = Color.BLACK
            }

            val data = BarData(dataSet)
            this.data = data

            /*--------------------------- 下面的代码怪怪的 -------------------------*/
            // 5. 设置X轴的标签
            xAxis.apply {
                position = XAxis.XAxisPosition.TOP
                valueFormatter = IndexAxisValueFormatter(listOf("产品A", "产品B", "产品C"))
                granularity = 1f
            }

            // 6. 设置Y轴的属性
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            /*-----------------------------------------------------------------*/

            // 7. 设置图例的位置
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
            }

            // 8. 设置条形图的动画
            animateY(1500)

            // 9. 刷新图表
            invalidate()
        }
    }, modifier = Modifier.fillMaxSize())
}




@Preview(showBackground = true)
@Composable
fun ChartTestPreview() {
//    HorizontalBarChartComposable()
//    PieChartComposable()
//    DetailedPieChartComposable()
//    DetailedHorizontalBarChartComposable()
}


