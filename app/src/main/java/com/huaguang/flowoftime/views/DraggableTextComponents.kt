package com.huaguang.flowoftime.views

import android.util.Log
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun DraggableText(
    text: String,
    onDragDelta: (Float) -> Unit,
    onDragStopped: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val speedList = remember { mutableStateListOf<Float>() }
    val lastDragTime = remember { mutableStateOf<Long?>(null) }
    val lastDelta = remember { mutableStateOf(0f) }

    Text(
        text = text,
        modifier = modifier.draggable(
            orientation = Orientation.Horizontal,
            enabled = enabled,
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
    Log.i("打标签喽", "maxSpeed = $maxSpeed")
    val dragCoefficient = if (maxSpeed > 6) 10f else 2f
    val direction = if (lastDelta > 0) 1 else -1
    val dragValue = dragCoefficient * direction
    onDragDelta(dragValue)

    onDragStopped()
}