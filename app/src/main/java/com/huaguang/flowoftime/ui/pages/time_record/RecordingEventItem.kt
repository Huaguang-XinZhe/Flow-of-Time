package com.huaguang.flowoftime.ui.pages.time_record

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.ItemType
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.TimeType
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.data.models.EventInfo
import com.huaguang.flowoftime.data.models.tables.Event
import com.huaguang.flowoftime.ui.components.TailLayout
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.widget.LongPressOutlinedIconButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingEventItem(
    combinedEvent: CombinedEvent?,
    customTimeState: MutableState<CustomTime?>,
    viewModel: EventInputViewModel,
    itemState: MutableState<ItemType>,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 10.dp)
            .clip(CardDefaults.outlinedShape)
            .combinedClickable(
                onClick = {},
                onDoubleClick = { viewModel.onRecordingItemDoubleClick(itemState) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true)
            ),
    ) {
        RecordingEventTree(
            combinedEvent = combinedEvent,
            customTimeState = customTimeState,
            viewModel = viewModel,
            itemState = itemState,
            modifier = Modifier.padding(5.dp),
        )
    }

}

@Composable
fun RecordingEventTree(
    combinedEvent: CombinedEvent?,
    customTimeState: MutableState<CustomTime?>,
    viewModel: EventInputViewModel,
    itemState: MutableState<ItemType>,
    modifier: Modifier = Modifier
) {
    val event = combinedEvent?.event ?: return
    val expandState = remember { mutableStateOf(false) }

    LaunchedEffect(event.withContent) { // 副作用初始化的时候都会执行一次！！！
        expandState.value = event.withContent
    }

    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {

            if (event.type.isExpandable()) {
                ExpandIcon(expandState) // 直接传递MutableState<Boolean>给ExpandIcon
            } else {
                Spacer(modifier = Modifier.size(24.dp)) // 占位用的
            }

            TimeLabelStart(event = event, customTimeState = customTimeState)

            FlagText(type = event.type)

            RecordingTailLayout(
                event = event,
                viewModel = viewModel,
                customTimeState = customTimeState,
            )

        }

        if (expandState.value) {
            val indentModifier = Modifier.padding(start = 30.dp) // 添加缩进

            Column(modifier = indentModifier) {
                combinedEvent.contentEvents.forEach { childCombinedEvent ->
                    RecordingEventTree( // 递归调用以显示子事件
                        combinedEvent = childCombinedEvent,
                        customTimeState = customTimeState,
                        viewModel = viewModel,
                        itemState = itemState,
                    )
                }
            }

        }
    }
}


@Composable
fun TimeLabelStart(
    event: Event,
    customTimeState: MutableState<CustomTime?>,
) {
    val startCustomTime =
        CustomTime(
            eventInfo = EventInfo(
                id = event.id,
                isTiming = event.endTime == null,
                parentId = event.parentEventId,
                eventType = event.type,
            ),
            type = TimeType.START,
            initialTime = event.startTime, // 把 remember 调整到这里是非常重要的一处改变，这使得 initialTime 的值是动态的！
            timeState = remember { mutableStateOf(null) }
        )

    TimeLabel(
        customTime = startCustomTime,
        customTimeState = customTimeState,
        modifier = Modifier.padding(end = 5.dp)
    )
}

@Composable
fun RecordingTailLayout(
    event: Event,
    viewModel: EventInputViewModel,
    customTimeState: MutableState<CustomTime?>,
) {
    val endCustomTime =
        CustomTime(
            eventInfo = EventInfo(
                id = event.id,
                isTiming = false,
                parentId = event.parentEventId,
                eventType = event.type,
            ),
            type = TimeType.END,
            initialTime = event.endTime, // 重新组合的时候这个状态会被记住，但值会改变
            timeState = remember { mutableStateOf(null) } // 开始时 endTime 为 null，但在显示尾部 TimeLabel 时就已经排除这种情况
        )
    // 正在进行的主题事项，其名称非空（上边的判断是对单条事项的，下边的是对整个 item 的，需要与其他条目交互。一个判断都不能少！）
    val allowShow = event.type == EventType.SUBJECT && event.endTime == null && event.name.isNotEmpty() &&
            viewModel.sharedState.cursorType.value == EventType.SUBJECT // 关键判断

    val showState = remember { mutableStateOf(false) }

    LaunchedEffect(allowShow) {
        if (allowShow) delay(50) // 延迟一会儿再显示（放频闪），变为不显示的时候就不要延迟了
        showState.value = allowShow
    }

    TailLayout(
        event = event,
        viewModel = viewModel,
        itemType = ItemType.RECORD,
    ) {
        // 二选其一，一定有一项
        if (event.endTime == null) {
            Text(text = "……")
        } else {
            TimeLabel(
                customTime = endCustomTime,
                customTimeState = customTimeState
            )
        }

        if (showState.value) { // 后来添加
            val eventControl = LocalEventControl.current
            val buttonsStateControl = LocalButtonsStateControl.current

            LongPressOutlinedIconButton(
                onClick = { viewModel.onStepButtonClick(eventControl, buttonsStateControl) },
                onLongClick = { viewModel.onStepButtonLongClick(eventControl, buttonsStateControl) },
                modifier = Modifier.padding(start = 5.dp),
                enabled = !viewModel.inputState.show.value
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.step),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

    }
}

@Composable
fun FlagText(type: EventType) {
    var flag = ""

    if (type == EventType.FOLLOW) {
        flag = "f: "
    } else if (type.isInsert()) {
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



