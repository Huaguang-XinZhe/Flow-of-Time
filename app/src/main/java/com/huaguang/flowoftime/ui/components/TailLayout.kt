package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.Mode
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.pages.time_record.LocalSelectedTime

/**
 * 使用 Layout 来自定义文本和时间标签的布局
 */
@Composable
fun TailLayout(
    event: Event,
    viewModel: EventInputViewModel,
    mode: Mode = Mode.DISPLAY,
    content: @Composable (RowScope.(type: EventType) -> Unit)
) {
    val selectedTime = LocalSelectedTime.current

    val fontSize = if (event.type == EventType.SUBJECT) 20.sp else {
        if (mode == Mode.DISPLAY) 14.sp else 16.sp
    }
    val fontWeight = if (event.type == EventType.SUBJECT) FontWeight.Bold else {
        if (mode == Mode.DISPLAY) FontWeight.Light else FontWeight.Normal
    }

    Layout(
        content = {
            // 事项名称
            Text(
                text = "${event.id}, ${event.name}",
                maxLines = 2, // 最多两行
                overflow = TextOverflow.Ellipsis, // 如果文本太长，则使用省略号
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = fontWeight,
                modifier = Modifier.clickable {
                    if (mode == Mode.RECORD) selectedTime?.value = null // 如果是记录 Item ，点击名称后便取消时间标签的选中状态
                    viewModel.onNameClick(event, mode)
                }
            )

            Row(
                modifier = Modifier.padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content(event.type)
            }
        }
    ) { measurables, constraints ->
        // 首先测量时间标签，以便知道剩余可用空间
        val timeLabel = measurables.last().measure(constraints)

        // 然后测量事项名称文本，给定减去时间标签宽度的约束
        val textWidth = constraints.maxWidth - timeLabel.width
        val textConstraints = constraints.copy(maxWidth = textWidth)
        val text = measurables.first().measure(textConstraints)

        // 计算总宽度和高度
        val width = text.width + timeLabel.width
        val height = maxOf(text.height, timeLabel.height)

        layout(width, height) {
            // 布局元素
            text.placeRelative(0, 0)
            timeLabel.placeRelative(x = text.width, y = (text.height - timeLabel.height) / 2) // y 这么写是为了居中
        }
    }
}
