package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.DashType
import com.huaguang.flowoftime.ui.theme.DeepGreen
import com.huaguang.flowoftime.utils.dashBorder

@Composable
fun Category(
    modifier: Modifier = Modifier,
    name: String = "",
    type: DashType = DashType.MIXED_ADD, // 默认就是混合模式
    onClick: (String, DashType) -> Unit,
) {
    val bgColor: Color
    val borderColor: Color
    val textColor: Color
    val text: String
    val horizontalPadding: Dp
    val verticalPadding: Dp

    if (name.isNotEmpty()) {
        borderColor = Color.Transparent
        bgColor = DeepGreen
        textColor = Color.White
        text = "@$name"
        horizontalPadding = 6.dp
        verticalPadding = 3.dp
    } else {
        borderColor = if (type == DashType.MIXED_ADD) MaterialTheme.colorScheme.primary else DeepGreen
        bgColor = borderColor.copy(alpha = 0.1f)
        textColor = borderColor
        horizontalPadding = 15.dp
        verticalPadding = 1.dp
        text = if (type.isAdd()) "+" else "*"
    }

    Box(
        modifier = modifier
            .dashBorder(borderColor) // dashBorder 放在 clip 后边会被 clip 遮挡，放在前边不会？
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = {
                    onClick(name, type)
                }
            )
//            .dashBorder() // 放这里会被 clip 剪切（遮挡不全）
            .background(bgColor, shape = CircleShape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
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
    onClick: (String) -> Unit,
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
        horizontalPadding = 15.dp
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .clip(shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = {
                    onClick(name)
                }
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
    tags: MutableList<String>?,
    mainAxisSpacing: Dp = 5.dp, // 水平间距
    crossAxisSpacing: Dp = 5.dp, // 垂直间距
    onClick: (String) -> Unit // 点击标签的事件处理){}
) {
    val topPadding = if (tags == null) 5.dp else 0.dp // 不加这个的话，只有一个标签时会和上面的类属标签重叠在一起

    Layout(
        content = {
            val newTags = tags?.apply { add("") } ?: listOf("") // 不管标签列表是否为空，都要加一个虚框按钮
            newTags.forEach { tagName ->
                Tag(name = tagName,onClick = onClick)
            }
        },
        modifier = modifier.padding(top = topPadding)
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

@Composable
fun CategoryRow(
    category: String, // 来自 event
    onClick: (String, DashType) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {

        Category(name = category, onClick = onClick)

        Spacer(modifier = Modifier.width(5.dp))

        Category(type = DashType.CATEGORY_CHANGE, onClick = onClick)
    }

}


//@Preview(showBackground = true)
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



    }

}
