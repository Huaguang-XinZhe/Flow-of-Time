package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.CategoryType
import com.huaguang.flowoftime.ui.theme.DeepGreen
import com.huaguang.flowoftime.utils.dashBorder

@Composable
fun Category(
    modifier: Modifier = Modifier,
    name: String = "",
    type: CategoryType = CategoryType.ADD,
    onClick: () -> Unit,
) {
    val bgColor: Color
    val borderColor: Color
    val textColor: Color
    val text: String
    val horizontalPadding: Dp

    if (name.isNotEmpty()) {
        borderColor = Color.Transparent
        bgColor = DeepGreen
        textColor = Color.White
        text = "@$name"
        horizontalPadding = 6.dp
    } else {
        borderColor = DeepGreen
        bgColor = borderColor.copy(alpha = 0.1f)
        textColor = borderColor
        horizontalPadding = 15.dp
        text = if (type == CategoryType.ADD) "+" else "*"
    }

    Box(
        modifier = modifier
            .dashBorder(borderColor) // dashBorder 放在 clip 后边会被 clip 遮挡，放在前边不会？
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = onClick
            )
//            .dashBorder() // 放这里会被 clip 剪切（遮挡不全）
            .background(bgColor, shape = CircleShape)
            .padding(horizontal = horizontalPadding, vertical = 3.dp)
//            .dashBorder() // 放在这里会被 padding 挤到组件里边来
    ) {
        Text(
            text = text,
            color = textColor,
//            modifier = Modifier.align(Alignment.Center) // 加和不加都一样，是居中的
        )
    }
}

@Composable
fun Tag(
    modifier: Modifier = Modifier,
    name: String = "",
    onClick: () -> Unit,
) {

    val shape = RoundedCornerShape(4.dp)
    val bgColor: Color
    val borderColor: Color
    val borderModifier: Modifier
    val textColor: Color
    val text: String
    val horizontalPadding: Dp

    if (name.isNotEmpty()) {
        borderColor = Color.DarkGray
        borderModifier = Modifier.border(0.5.dp, borderColor, shape = shape)
        bgColor = borderColor.copy(alpha = 0.05f)
        textColor = borderColor
        text = "#$name"
        horizontalPadding = 3.dp
    } else {
        borderColor = Color.DarkGray
        borderModifier = Modifier.dashBorder(borderColor, 15f)
        bgColor = borderColor.copy(alpha = 0.1f)
        textColor = borderColor
        text = "+"
        horizontalPadding = 10.dp
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .clip(shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onClick
            )
            .background(bgColor, shape = shape)
            .padding(horizontal = horizontalPadding)
    ) {
        Text(
            text = text,
            color = textColor
        )
    }
}

@Composable
fun TagsRow(
    modifier: Modifier = Modifier,
    tags: List<String>,
    mainAxisSpacing: Dp = 5.dp, // 水平间距
    crossAxisSpacing: Dp = 5.dp, // 垂直间距
    onClick: () -> Unit // 点击标签的事件处理){}
) {
    Layout(
        content = {
            tags.forEach { tag ->
                Tag(name = tag, onClick = onClick)
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


@Preview(showBackground = true)
@Composable
fun test3() {
    val context = LocalContext.current

    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Category(type = CategoryType.CHANGE) {
//            Toast.makeText(context, "点击了改变按钮", Toast.LENGTH_SHORT).show()
//        }
//
//        Spacer(modifier = Modifier.width(10.dp))
//
//        Category(type = CategoryType.ADD) {
//            Toast.makeText(context, "点击了改变按钮", Toast.LENGTH_SHORT).show()
//        }
//
//        Spacer(modifier = Modifier.width(10.dp))
//
//        Category(name = "@Core") {
//            Toast.makeText(context, "点击了改变按钮", Toast.LENGTH_SHORT).show()
//        }

        TagsRow(tags = listOf("中国", "美国", "")) {
            
        }

    }

}
