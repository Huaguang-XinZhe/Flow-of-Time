package com.huaguang.flowoftime.pages.time_record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.data.models.EventWithSubEvents
import com.huaguang.flowoftime.ui.components.TailLayout
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel

@Composable
fun RecordingEventItem(
    event: Event,
    customTimeState: MutableState<CustomTime?>,
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier
) {
    // TODO: 隐隐的感觉这里一定会出问题。
    val expandStates = remember { mutableStateMapOf<Long, MutableState<Boolean>>() }
    val eventWithSubEventsState = remember { mutableStateOf<EventWithSubEvents?>(null) }
    val expandState = remember { mutableStateOf(false) }
    val startCustomTime =
        CustomTime(
            type = TimeType.START,
            timeState = remember { mutableStateOf(null) }, // 把 remember 调整到这里是非常重要的一处改变，这使得 initialTime 的值是动态的！
            initialTime = event.startTime
        )

    val endCustomTime =
        CustomTime(
            type = TimeType.END,
            timeState = remember { mutableStateOf(null) }, // 重新组合的时候这个状态会被记住，但值会改变
            initialTime = event.endTime // 开始时 endTime 为 null，但在显示尾部 TimeLabel 时就已经排除这种情况
        )
    val type = event.type

    LaunchedEffect(event.id) {
        // TODO: 这里的逻辑或许也可以移到 ViewModel 中
        val eventWithSubEvents = viewModel.repository.getEventWithSubEvents(event.id)
        eventWithSubEventsState.value = eventWithSubEvents

        val isExpanded = expandStates.getOrPut(event.id) { mutableStateOf(false) }.value
        expandState.value = isExpanded
    }

    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            if (type.isExpandable() && event.parentEventId != null) {
                ExpandIcon(expandState) // 直接传递MutableState<Boolean>给ExpandIcon
            } else {
                Spacer(modifier = Modifier.size(24.dp)) // 占位用的
            }

            TimeLabel(
                customTime = startCustomTime,
                customTimeState = customTimeState,
                modifier = Modifier.padding(end = 5.dp)
            )

            FlagText(type = type)

            TailLayout(
                event = event,
                isDisplay = false,
                viewModel = viewModel,
            ) {
                if (event.endTime == null) {
                    Text(text = "……")
                } else {
                    TimeLabel(
                        customTime = endCustomTime,
                        customTimeState = customTimeState
                    )
                }
            }

        }

        if (expandState.value && event.parentEventId != null) {
            val indentModifier = Modifier.padding(start = 30.dp) // 添加缩进

            Column(modifier = indentModifier) {
                eventWithSubEventsState.value?.let { eventWithSubEvents ->
                    eventWithSubEvents.subEvents.forEach { childEvent ->
                        RecordingEventItem( // 递归调用以显示子事件
                            event = childEvent,
                            customTimeState = customTimeState,
                            viewModel = viewModel,
                        )
                    }
                }
            }

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

