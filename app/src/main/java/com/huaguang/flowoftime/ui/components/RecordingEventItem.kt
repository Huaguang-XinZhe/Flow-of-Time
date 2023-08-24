package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.widget.TimeLabel
import java.time.LocalDateTime

@Composable
fun RecordingEventItem(
    name: String,
    type: EventType,
    isTiming: Boolean,
    level: Int = 0,
    expandStates: List<MutableState<Boolean>>, // 使用列表跟踪每个层级的展开状态
    content: @Composable (() -> Unit)? = null
) {

    val expandState = expandStates[level] // 获取当前层级的展开状态

    Column {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            if (type.isExpandable() && content != null && level < 3) { // 限制嵌套层级到3
                ExpandIcon(expandState)
            } else {
                Spacer(modifier = Modifier.size(24.dp)) // 占位用的
            }

            TimeLabel(
                time = LocalDateTime.now(),
                modifier = Modifier.padding(end = 5.dp)
            )

            FlagText(type = type)

            TailLayout(name, type, isTiming)

        }

        if (content != null && expandState.value) {
            val indentModifier = Modifier.padding(start = 30.dp) // 添加缩进
            Column(modifier = indentModifier) {
                content()
            }
        }
    }
}

/**
 * 使用 Layout 来自定义文本和时间标签的布局
 */
@Composable
fun RowScope.TailLayout(name: String, type: EventType, isTiming: Boolean) {
    Layout(
        content = {
            // 事项名称
            Text(
                text = name,
                maxLines = 2, // 最多两行
                overflow = TextOverflow.Ellipsis, // 如果文本太长，则使用省略号
                fontSize = if (type == EventType.SUBJECT) 18.sp else 16.sp,
                fontWeight = if (type == EventType.SUBJECT) FontWeight.ExtraBold else FontWeight.Normal,
                modifier = Modifier.clickable {
                    // TODO:
                }
            )

            Spacer(Modifier.weight(1f)) // 添加灵活的空间

            if (isTiming) {
                Text(
                    text = "……",
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            } else {
                TimeLabel(
                    time = LocalDateTime.now(),
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
        }
    ) { measurables, constraints ->
        // 首先测量时间标签，以便知道剩余可用空间
        val timeLabel = measurables.last().measure(constraints)

        // 然后测量事项名称文本，给定减去时间标签宽度的约束
        val textWidth = constraints.maxWidth - timeLabel.width
        val textConstraints = constraints.copy(maxWidth = textWidth)
        val text = measurables.first().measure(textConstraints)

        // 计算 Spacer 的大小
        val spacer = measurables[1].measure(constraints.copy(maxWidth = textWidth - text.width))

        // 计算总宽度和高度
        val width = text.width + spacer.width + timeLabel.width
        val height = maxOf(text.height, timeLabel.height)

        layout(width, height) {
            // 布局元素
            text.placeRelative(0, 0)
            spacer.placeRelative(x = text.width, y = 0)
            timeLabel.placeRelative(x = text.width + spacer.width, y = (text.height - timeLabel.height) / 2) // y 这么写是为了居中
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

@Preview(showBackground = true)
@Composable
fun test1() {

    val expandStates = List(3) { remember { mutableStateOf(false) } } // 创建一个列表来跟踪每个层级的展开状态

    OutlinedCard(
        modifier = Modifier.padding(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            RecordingEventItem(
                name = "时间统计法",
                type = EventType.SUBJECT,
                isTiming = false,
                level = 0, // 设置当前层级
                expandStates = expandStates, // 传递展开状态列表
            ) {
                RecordingEventItem(
                    name = "昨日作息统析，今日计划",
                    type = EventType.STEP,
                    isTiming = false,
                    level = 1, // 设置当前层级
                    expandStates = expandStates, // 传递展开状态列表
                ) {
                    RecordingEventItem(
                        name = "老妈来电",
                        type = EventType.INSERT,
                        isTiming = false,
                        level = 2,
                        expandStates = expandStates
                    )

                    RecordingEventItem(
                        name = "尚硅谷 HTML + CSS 学习bikjkfkjfdsjs ，陕西农业大学",
                        type = EventType.FOLLOW,
                        isTiming = true,
                        level = 2,
                        expandStates = expandStates
                    )

                }
            }
        }
    }

}
