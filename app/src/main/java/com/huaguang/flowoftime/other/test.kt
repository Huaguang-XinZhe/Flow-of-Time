package com.huaguang.flowoftime.other

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.ui.theme.DarkGreen39
import com.huaguang.flowoftime.utils.LocalDateTimeSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime
import kotlin.math.min


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

/**
 * 将 Item 定位到屏幕中等偏上的位置
 */

@Composable
fun MyScreen() {
    val list = listOf(
        "Item 1", "Item 2", "Item 3",
        "Item 4", "Item 5", "Item 6",
        "Item 7", "Item 8", "Item 9",
        "Item 10", "Item 11", "Item 12",
        "Item 13", "Item 14", "Item 15",
        "Item 1", "Item 2", "Item 3",
        "Item 4", "Item 5", "Item 6",
        "Item 7", "Item 8", "Item 9",
        "Item 10", "Item 11", "Item 12",
        "Item 13", "Item 14", "Item 15",
    )
    val listState = rememberLazyListState()
    var targetIndex by remember { mutableStateOf(-1) }
    val context = LocalContext.current

    val focusManager = LocalFocusManager.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                RDALogger.info("delta = $delta")
                // 这里可以移除 TextField 的焦点
                focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

    Column(

    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // 分配所有剩余空间给 LazyColumn
                .nestedScroll(nestedScrollConnection)
        ) {
            itemsIndexed(list) {index, item ->
//                val interactionSource = remember { MutableInteractionSource() }

//                Text(
//                    text = item,
//                    modifier = Modifier
//                        .clickable(
//                            interactionSource = interactionSource,
//                            indication = rememberRipple(bounded = true) // 添加水波纹效果
//                        ) {
//                            // 更新目标索引的状态
////                            targetIndex = list.indexOf(item) // 如果 Item 是一样的，那这种获取索引的方式就会出错，只会获取列表中第一个 Item 的索引！
//                            targetIndex = index - 2 // 通过将点击 Item 的索引减 2，将 Item 定位到屏幕中等偏上的位置
//                        }
//                        .padding(30.dp)
//                        .fillMaxWidth()
//                )

                TextField(
                    value = item,
                    onValueChange = {},
                    modifier = Modifier
                        .onFocusChanged {
                            targetIndex = index - 4 // 通过将点击 Item 的索引减 4，将 Item 定位到屏幕中等偏上的位置
                        }
                )
            }
        }

        Button(onClick = { targetIndex = 2 }) {
            Text(text = "Scroll to item 3")
        }

        // 检查目标索引的状态，并滚动到指定的 item
        LaunchedEffect(targetIndex) {
            if (targetIndex >= 0) {
                listState.animateScrollToItem(index = targetIndex)
                Toast.makeText(context, "targetIndex = $targetIndex", Toast.LENGTH_SHORT).show()
            }
        }

//        // 检查滚动状态，并取消焦点
//        LaunchedEffect(listState.isScrollInProgress) {
//            if (listState.isScrollInProgress) {
//                focusManager.clearFocus()
//            }
//        }

    }


}


/**
 * Swipeable 滑动 Demo（开关）
 */

// 开关的状态，枚举类
enum class State { OPEN, CLOSE }

//@Preview(showBackground = true)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwitchDemo() {
    val blockSize = 48.dp
    val blockSizePx = with(LocalDensity.current) { blockSize.toPx() }
    val swipeableState = rememberSwipeableState(initialValue = State.CLOSE)

    val anchors = mapOf(
        0f to State.CLOSE,
        blockSizePx to State.OPEN // 这里为什么要使用 Px 呢？
    )

    Box(
        modifier = Modifier.size(width = blockSize * 2, height = blockSize)
    ) {
        Box(
            modifier = Modifier
                .size(blockSize)
                .offset(x = swipeableState.offset.value.dp, y = 0.dp)
//                .offset {
//                    IntOffset(swipeableState.offset.value.toInt(), 0) // 这两种写法有什么区别？
//                }
                .background(Color.Red)
                .swipeable(
                    state = swipeableState,
                    orientation = Orientation.Horizontal,
                    anchors = anchors,
                    thresholds = { from, _ ->
                        if (from == State.CLOSE) {
                            FractionalThreshold(0.3f)
                        } else {
                            FractionalThreshold(0.5f)
                        }
                    }
                )
        )
    }
}


/**
 * Canvas Demo（进度环）
 */
//@Preview
@Composable
fun ProgressLoop() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(400.dp)
            .padding(50.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextCenter(text = "Loading")
            TextCenter(text = "45%")
        }

        LoopCanvas(strokeWidth = 20.dp)

    }
}

@Composable
fun LoopCanvas(strokeWidth: Dp) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        val strokeWidthPx = strokeWidth.toPx() // toPx 必须在 Density 的作用域中使用（DrawScope 为什么也可以？）
        val width = drawContext.size.width
        val height = drawContext.size.height
        val center = Offset(x = width / 2f, y = height / 2f)
        val radius = min(width, height) / 2f - strokeWidthPx / 2f

        drawCircle(
            color = Color.LightGray,
            center = center,
            radius = radius,
            style = Stroke(strokeWidthPx)
        )

        drawArc(
            color = Color.DarkGray,
            startAngle = -90f,
            sweepAngle = 162f,
            useCenter = false, // 这是干嘛的？
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
        )

    }
}

@Composable
fun TextCenter(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontSize = 30.sp
    )
}

/**
 * 红心动画 Demo
 */

@Preview(showBackground = true)
@Composable
fun FavoriteAnimate() {
    var change by remember { mutableStateOf(false) }
    var flag by remember { mutableStateOf(false) }

    // 变化的大小
    val size = animateDpAsState(
        targetValue = if (change) 100.dp else 24.dp,

    )

    // 变化的颜色
    val color = animateColorAsState(
        targetValue = if (flag) Color.Red else Color.LightGray
    )

    if (size.value == 100.dp) {
        // 复原
        change = false
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        IconButton(
            onClick = {
                change = true
                flag = !flag
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = color.value,
                modifier = Modifier.size(size.value) // 对 size 的设置最好放在图标处，不要放在 IconButton 处，有时候不起效果
            )
        }
    }

}