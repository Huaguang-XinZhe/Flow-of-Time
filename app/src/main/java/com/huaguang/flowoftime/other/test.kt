package com.huaguang.flowoftime.other

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.ui.theme.DarkGreen39
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime


@ExperimentalMaterialApi
@Composable
fun <T> CustomSwipeToDismiss2(
    list: List<T>,
    key: ((T) -> Any)? = null,
    dismissed: (T) -> Unit,
    dismissContent: @Composable (RowScope.() -> Unit)
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(list, key) { item ->
            val dismissState = rememberDismissState()
            if (dismissState.isDismissed(DismissDirection.EndToStart)) {
                dismissed(item)
            }
            SwipeToDismiss(
                state = dismissState,
                modifier = Modifier.padding(vertical = 1.dp, horizontal = 10.dp),
                directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                dismissThresholds = { direction ->
                    FractionalThreshold(if (direction == DismissDirection.StartToEnd) 0.25f else 0.5f)
                },
                background = {
                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
                    val isDefault = dismissState.targetValue == DismissValue.Default
                    val color by animateColorAsState(
                        when (dismissState.targetValue) {
                            DismissValue.Default -> Color.LightGray
                            DismissValue.DismissedToEnd -> DarkGreen39
                            DismissValue.DismissedToStart -> Color.Red
                        }
                    )
                    val alignment = when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    }
                    val icon = when (direction) {
                        DismissDirection.StartToEnd -> Icons.Filled.Done
                        DismissDirection.EndToStart -> Icons.Filled.Delete
                    }
                    val scale by animateFloatAsState(
                        // DismissValue.Default 是滑块达到阈值之前的状态
                        if (isDefault) 0.75f else 1f
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = alignment
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "Localized description",
                            modifier = Modifier.scale(scale),
                            tint = if (isDefault) Color.Black else Color.White
                        )
                    }
                },
                dismissContent = dismissContent
            )
        }
    }
}

//                    Card(
//                        elevation = animateDpAsState(
//                            if (dismissState.dismissDirection != null) 4.dp else 0.dp
//                        ).value,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text(
//                            text = item.text,
//                            modifier = Modifier.padding(10.dp),
//                            textAlign = TextAlign.Center
//                        )
//                    }


fun main() {

    val startTime = LocalDateTime.of(2023, 3, 12, 11, 5)

    val json = Json {
        serializersModule = SerializersModule {
            contextual(LocalDateTime::class, LocalDateTimeSerializer)
        }
    }

    val startTimeJson = json.encodeToString(startTime)
    val decodeStartTime = json.decodeFromString<LocalDateTime>(startTimeJson)

    println(startTime)
    println(startTimeJson)
    println(decodeStartTime)


//    val event = Event(
//        id = 2,
//        startTime = startTime,
//        name = "这是我自己造的一个 event",
//        endTime = LocalDateTime.now(),
//        parentId = 5
//    )
//
//    val eventJson = Json.encodeToString(Event.serializer(), event)
//    val decodeEvent = Json.decodeFromString<Event>(eventJson)
//    println(eventJson)
//    println(decodeEvent)

}



