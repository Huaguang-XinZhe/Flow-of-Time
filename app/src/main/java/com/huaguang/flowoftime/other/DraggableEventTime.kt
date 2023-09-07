package com.huaguang.flowoftime.other

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.utils.formatLocalDateTime
import com.huaguang.flowoftime.utils.isCoreEvent
import java.time.Duration
import java.time.LocalDateTime


@Composable
fun DraggableEventTime(
    isEndTime: Boolean = false,
    event: Event,
    mediator: EventTrackerMediator,
    startTimeState: MutableState<LocalDateTime>,
    endTimeState: MutableState<LocalDateTime?>,
    durationState: MutableState<Duration?>
) {
    val text = if (isEndTime) {
        endTimeState.value?.let { formatLocalDateTime(it) } ?: "..."
    } else {
        formatLocalDateTime(startTimeState.value)
    }

    DraggableText(
        modifier = Modifier.padding(end = if (isEndTime) 0.dp else 5.dp),
        text = text,
        isEndTime = isEndTime,
        mediator = mediator,
        event = event,
        onDragDelta = { dragValue ->
            if (isEndTime) {
                endTimeState.value = endTimeState.value?.plusMinutes(dragValue.toLong())
                durationState.value = durationState.value?.plusMinutes(dragValue.toLong())
            } else {
                startTimeState.value = startTimeState.value.plusMinutes(dragValue.toLong())
                durationState.value = durationState.value?.minusMinutes(dragValue.toLong())
            }
        }
    ) {
        val updatedEvent = event.copy(
            startTime = startTimeState.value,
            endTime = endTimeState.value,
            duration = durationState.value
        )

        mediator.updateOnDragStopped(updatedEvent, event.duration)
    }
}

@Composable
fun DraggableText(
    modifier: Modifier = Modifier,
    text: String,
    isEndTime: Boolean = false,
    mediator: EventTrackerMediator,
    event: Event,
    onDragDelta: (Float) -> Unit,
    onDragStopped: () -> Unit
) {
    val speedList = remember { mutableStateListOf<Float>() }
    val lastDragTime = remember { mutableStateOf<Long?>(null) }
    val lastDelta = remember { mutableStateOf(0f) }
    val dragTracker = remember { mediator.dragTracker }
    val isSelected by remember {
        derivedStateOf { dragTracker.isSelected(event.id) }
    }

    val allow = isEndTime && event.endTime != null || !isEndTime

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)) // 设置Box的边缘为圆角
            .clickable(
                enabled = allow
            ) {  // 添加点击事件，点击后设置isClicked为true
                dragTracker.toggleSelection(event.id)
            }
            .then(
                if (isSelected && allow) { // 如果Text被点击，添加阴影
                    if (isCoreEvent(event.name)) {
                        Modifier.border(1.dp, Color.White)
                    } else Modifier.shadow(1.dp)
                } else Modifier
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(5.dp).draggable(
                orientation = Orientation.Horizontal,
                enabled = allow && isSelected, // 只有当Text被点击时，才能拖动
                state = rememberDraggableState { delta ->
                    calculateDragSpeed(delta, speedList, lastDragTime, lastDelta)
                },
                onDragStarted = { speedList.clear() },
                onDragStopped = {
                    handleDragStopped(speedList, lastDelta.value, onDragDelta, onDragStopped)
                }
            )
        )
    }
}

private fun calculateDragSpeed(
    delta: Float,
    speedList: MutableList<Float>,
    lastDragTime: MutableState<Long?>,
    lastDelta: MutableState<Float>
) {
    val currentTime = System.currentTimeMillis()
    val timeDiff = if (lastDragTime.value != null) currentTime - lastDragTime.value!! else 0
    lastDragTime.value = currentTime

    val speed = if (timeDiff != 0L) delta / timeDiff else 0f
    speedList.add(speed)

    lastDelta.value = delta
}

private fun handleDragStopped(
    speedList: List<Float>,
    lastDelta: Float,
    onDragDelta: (Float) -> Unit,
    onDragStopped: () -> Unit
) {
    val maxSpeed = speedList.maxOrNull() ?: 0f
    val dragCoefficient = if (maxSpeed > 6) 10f else 2f
    val direction = if (lastDelta > 0) 1 else -1
    val dragValue = dragCoefficient * direction
    onDragDelta(dragValue)

    onDragStopped()
}
