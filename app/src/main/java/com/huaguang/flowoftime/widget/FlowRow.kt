package com.huaguang.flowoftime.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    labels: List<Pair<String, LabelType>>, // 水平间距
    mainAxisSpacing: Dp = 0.dp, // 垂直间距
    crossAxisSpacing: Dp = 0.dp,
    onClickLabel: (LabelType) -> Unit // 点击标签的事件处理){}
) {
    Layout(
        content = {
            labels.forEach { (text, type) ->
                CategoryLabel(
                    text = text,
                    labelType = type
                ) { onClickLabel(type) } // 处理点击事件

            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val rowHeights = mutableListOf<Int>()
        val placeables = mutableListOf<Placeable>()
        var rowWidth = 0
        var rowMaxHeight = 0 // 当前行的最大高度（一行只有一个）

        measurables.forEachIndexed { index, measurable ->
            val placeable = measurable.measure(constraints)
            if (rowWidth + placeable.width + mainAxisSpacing.toPx() > constraints.maxWidth) { // 触发越行重置，并收集当前行的最大高度
                rowHeights.add(rowMaxHeight)
                rowWidth = 0
                rowMaxHeight = 0
            }
            rowWidth += (placeable.width + mainAxisSpacing.toPx()).toInt()
            rowMaxHeight = maxOf(rowMaxHeight, placeable.height)
            placeables.add(placeable)
            if (index == measurables.lastIndex) {
                rowHeights.add(rowMaxHeight) // 最后一行的标签可能不会触发越行，但也要收集一个最大高度
            }
        }

        val totalHeight = rowHeights.sum() + (crossAxisSpacing.toPx() * (rowHeights.size - 1)).toInt()
        val totalWidth = constraints.maxWidth

        layout(totalWidth, totalHeight) { // 传入总宽高
            var yPosition = 0
            var xPosition = 0
            placeables.forEach { placeable -> // 布局循环（排列组件的位置）
                if (xPosition + placeable.width > totalWidth) { // 越行重置，计算组件的起绘坐标点
                    xPosition = 0
                    yPosition += rowHeights.removeFirst() + crossAxisSpacing.toPx().toInt() // 删除第一个元素并返回
                }
                // 使每个标签在其行内居中对齐
                val centerOffset = (rowHeights.first() - placeable.height) / 2
                placeable.placeRelative(xPosition, yPosition + centerOffset)
                xPosition += placeable.width + mainAxisSpacing.toPx().toInt()
            }
        }
    }
}
