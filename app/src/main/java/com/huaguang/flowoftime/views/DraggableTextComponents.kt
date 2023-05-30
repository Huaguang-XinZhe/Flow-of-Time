package com.huaguang.flowoftime.views

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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.viewmodels.EventsViewModel

@Composable
fun DraggableText(
    text: String,
    onDragDelta: (Float) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    viewModel: EventsViewModel,
    isShadow: Boolean = true
) {
    val speedList = remember { mutableStateListOf<Float>() }
    val lastDragTime = remember { mutableStateOf<Long?>(null) }
    val lastDelta = remember { mutableStateOf(0f) }
    val isClicked = viewModel.isStartOrEndTimeClicked // 这个会对所有事件生效，而不仅仅局限于当前条目

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)) // 设置Box的边缘为圆角
            .clickable { isClicked.value = !isClicked.value } // 添加点击事件，点击后设置isClicked为true
            .then(
                if (isClicked.value && isShadow) { // 如果Text被点击，添加阴影
                    Modifier.shadow(1.dp)
                } else Modifier
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(5.dp).draggable(
                orientation = Orientation.Horizontal,
                enabled = enabled && isClicked.value, // 只有当Text被点击时，才能拖动
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