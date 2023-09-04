package com.huaguang.flowoftime.pages.time_record.recording_event_item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.ui.components.EventDisplay
import com.huaguang.flowoftime.widget.TimeLabel

@Composable
fun RecordingEventItem(
    eventDisplay: EventDisplay,
    viewModel: RecordingEventItemViewModel,
    modifier: Modifier = Modifier
) {
    val type = eventDisplay.type
    val expandState = viewModel.getExpandStateFor(eventDisplay)

    Column(
        modifier = modifier
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            if (type.isExpandable() && eventDisplay.contentEvents != null) {
                ExpandIcon(expandState) // 直接传递MutableState<Boolean>给ExpandIcon
            } else {
                Spacer(modifier = Modifier.size(24.dp)) // 占位用的
            }

            TimeLabel(
                time = eventDisplay.startTime,
                modifier = Modifier.padding(end = 5.dp)
            )

            FlagText(type = type)

            TailLayout(eventDisplay.name, type, false) {
                if (eventDisplay.endTime == null) {
                    Text(text = "……")
                } else {
                    TimeLabel(time = eventDisplay.endTime!!)
                }
            }

        }

        if (expandState.value && eventDisplay.contentEvents != null) {
            val indentModifier = Modifier.padding(start = 30.dp) // 添加缩进

            Column(modifier = indentModifier) {
                eventDisplay.contentEvents.forEach { childEvent ->
                    RecordingEventItem(childEvent, viewModel) // 递归调用以显示子事件
                }
            }
        }
    }
}

/**
 * 使用 Layout 来自定义文本和时间标签的布局
 */
@Composable
fun TailLayout(
    name: String,
    type: EventType,
    isDisplay: Boolean = true,
    content: @Composable (type: EventType) -> Unit
) {
    val fontSize = if (type == EventType.SUBJECT) 20.sp else {
        if (isDisplay) 14.sp else 16.sp
    }

    val fontWeight = if (type == EventType.SUBJECT) FontWeight.Bold else {
        if (isDisplay) FontWeight.Light else FontWeight.Normal
    }

    Layout(
        content = {
            // 事项名称
            Text(
                text = name,
                maxLines = 2, // 最多两行
                overflow = TextOverflow.Ellipsis, // 如果文本太长，则使用省略号
                color = Color.Black,
                fontSize = fontSize,
                fontWeight = fontWeight,
                modifier = Modifier.clickable {
                    // TODO:
                }
            )

            Box(modifier = Modifier.padding(horizontal = 5.dp)) {
                content(type)
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

@Composable
fun FlagText(type: EventType) {
    var flag = ""

    if (type == EventType.FOLLOW) {
        flag = "f: "
    } else if (type == EventType.INSERT) {
        flag = "i: "
    }

    Text(
        text = flag,
        color = Color.LightGray,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun ExpandIcon(expandState: MutableState<Boolean>) {

    val resId = if (expandState.value) {
        R.drawable.expand
    } else R.drawable.collapse

    IconToggleButton(
        checked = expandState.value,
        onCheckedChange = { expandState.value = it },
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}


enum class EventType {
    SUBJECT,
    STEP,
    FOLLOW,
    INSERT;

    fun isExpandable() = this == SUBJECT || this == STEP
}
