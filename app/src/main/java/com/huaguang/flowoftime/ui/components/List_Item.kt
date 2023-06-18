package com.huaguang.flowoftime.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.huaguang.flowoftime.ListItem
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.Event
import com.huaguang.flowoftime.ui.theme.LightGray93
import com.huaguang.flowoftime.utils.extensions.formatLocalDateTime
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime


@Composable
fun DateItem(item: ListItem.DateItem) {
    Surface(
        shape = CircleShape,
        color = LightGray93,
        modifier = Modifier
            .padding(10.dp, 10.dp, 10.dp, 0.dp)
            .fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.value.toString(),
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }
    }
}

@Composable
fun MainItem(item: ListItem.MainItem) {
    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {
        val (startTimeTextRef, dotRef, topDividerRef, bottomDividerRef, categoryIconRef,
            eventNameRef, durationTextRef, categoryTagRowRef) = remember { createRefs() }

        StartTimeText(
            startTime = item.event.startTime,
            modifier = Modifier.constrainAs(startTimeTextRef) {
                start.linkTo(parent.start)
                top.linkTo(parent.top, 20.dp)
            }
        )

        Dot(modifier = Modifier.constrainAs(dotRef) {
            start.linkTo(startTimeTextRef.end, 10.dp)
            // 与 startTimeTextRef 对齐
            top.linkTo(startTimeTextRef.top)
            bottom.linkTo(startTimeTextRef.bottom)
        })

        Divider(
            height = 27.dp,
            modifier = Modifier.constrainAs(topDividerRef) {
                // 居于 dotRef 的中间
                start.linkTo(dotRef.start)
                end.linkTo(dotRef.end)
                bottom.linkTo(dotRef.top)
            }
        )

        Divider(
            height = 54.dp,
            modifier = Modifier.constrainAs(bottomDividerRef) {
                // 居于 dotRef 的中间
                start.linkTo(dotRef.start)
                end.linkTo(dotRef.end)
                top.linkTo(dotRef.bottom)
            }
        )

        CategoryIcon(
            icon = painterResource(id = R.drawable.dev), // TODO: 靠类属去推理（需要相关的映射函数）
            modifier = Modifier.constrainAs(categoryIconRef) {
                start.linkTo(dotRef.end, 10.dp)
                // 与 startTimeTextRef 对齐
                top.linkTo(startTimeTextRef.top)
                bottom.linkTo(startTimeTextRef.bottom)
            }
        )

        EventName(
            name = item.event.name,
            modifier = Modifier.constrainAs(eventNameRef) {
                start.linkTo(categoryIconRef.end, 10.dp)
                // 与 startTimeTextRef 对齐
                top.linkTo(startTimeTextRef.top)
                bottom.linkTo(startTimeTextRef.bottom)
            }
        )

        DurationText(
            duration = item.event.duration ?: Duration.ZERO,
            modifier = Modifier.constrainAs(durationTextRef) {
                start.linkTo(eventNameRef.end, 10.dp)
                // 与 startTimeTextRef 对齐
                top.linkTo(startTimeTextRef.top)
                bottom.linkTo(startTimeTextRef.bottom)
            }
        )

        CategoryTagRow(
            category = item.event.category,
            subCategory = item.event.subCategory,
            modifier = Modifier.constrainAs(categoryTagRowRef) {
                start.linkTo(eventNameRef.start)
                top.linkTo(eventNameRef.bottom, 6.dp)
                bottom.linkTo(parent.bottom, 10.dp)
            }
        )

    }
}

@Composable
fun SubItem(item: ListItem.SubItem) {
    ConstraintLayout(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {
        val (dividerRef, categoryIconRef, eventNameRef,
            durationTextRef, categoryTagRowRef) = remember { createRefs() }

        Divider(
            height = 58.dp,
            modifier = Modifier.constrainAs(dividerRef) {
                start.linkTo(parent.start, 46.dp)
            }
        )

        CategoryIcon(
            size = 16.dp,
            icon = painterResource(id = R.drawable.family), // TODO: 靠类属去推理（需要相关的映射函数）
            modifier = Modifier.constrainAs(categoryIconRef) {
                start.linkTo(dividerRef.end, 46.dp)
                top.linkTo(parent.top, 5.dp)
            }
        )

        EventName(
            size = 15.sp,
            name = item.event.name,
            modifier = Modifier.constrainAs(eventNameRef) {
                start.linkTo(categoryIconRef.end, 6.dp)
                // 对齐 categoryIconRef
                top.linkTo(categoryIconRef.top)
                bottom.linkTo(categoryIconRef.bottom)
            }
        )

        DurationText(
            hourSize = 18.sp,
            minutesSize = 14.sp,
            unitSize = 8.sp,
            duration = item.event.duration ?: Duration.ZERO,
            modifier = Modifier.constrainAs(durationTextRef) {
                start.linkTo(eventNameRef.end, 6.dp)
                // 对齐 categoryIconRef
                top.linkTo(categoryIconRef.top)
                bottom.linkTo(categoryIconRef.bottom)
            }
        )

        CategoryTagRow(
            size = 8.sp,
            paddingHorizontal = 6.dp,
            category = item.event.category,
            subCategory = item.event.subCategory,
            modifier = Modifier.constrainAs(categoryTagRowRef) {
                start.linkTo(eventNameRef.start)
                top.linkTo(eventNameRef.bottom, 4.dp)
                bottom.linkTo(parent.bottom, 10.dp)
            }
        )

    }
}

@Composable
fun Interval(item: ListItem.Interval) {
    NumberCircle(
        number = item.value,
        modifier = Modifier.padding(start = 45.dp)
    )
}


@Composable
fun StartTimeText(
    modifier: Modifier = Modifier,
    startTime: LocalDateTime,
) {
    Text(
        text = formatLocalDateTime(startTime),
        modifier = modifier
    )
}

@Composable
fun CategoryIcon(
    modifier: Modifier = Modifier,
    icon: Painter,
    size: Dp = 24.dp,
) {
    Icon(
        painter = icon,
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

@Composable
fun EventName(
    modifier: Modifier = Modifier,
    name: String,
    size: TextUnit = 20.sp
) {
    Text(
        text = name,
        fontSize = size,
        modifier = modifier
    )
}


@Composable
fun CategoryTagRow(
    modifier: Modifier = Modifier,
    category: String, 
    subCategory: String? = null,
    size: TextUnit = 11.sp,
    paddingHorizontal: Dp = 8.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CategoryRounded(text = category, size = size, paddingHorizontal = paddingHorizontal)

        if (subCategory != null) {
            Spacer(modifier = Modifier.width(10.dp))
            
            CategoryOutlined(text = subCategory, size = size, paddingHorizontal = paddingHorizontal)
        }
    }
}

@Composable
fun CategoryRounded(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    size: TextUnit = 11.sp,
    paddingHorizontal: Dp = 8.dp,
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = size,
            modifier = Modifier.padding(
                horizontal = paddingHorizontal,
                vertical = paddingHorizontal / 2,
            )
        )
    }
}

@Composable
fun CategoryOutlined(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    size: TextUnit = 11.sp,
    paddingHorizontal: Dp = 8.dp,
) {
    Surface(
        color = Color.White,
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = size,
            modifier = Modifier.padding(
                horizontal = paddingHorizontal,
                vertical = paddingHorizontal / 2,
            )
        )
    }
}

@Composable
fun Dot(
    modifier: Modifier = Modifier,
    size: Dp = 5.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, shape = CircleShape)
    )
}


@Composable
fun Divider(
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(height)
            .background(Color.LightGray)
    )
}

@Composable
fun NumberCircle(
    modifier: Modifier = Modifier,
    number: Int,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
fun DurationText(
    modifier: Modifier = Modifier,
    duration: Duration,
    hourSize: TextUnit = 25.sp,
    minutesSize: TextUnit = 18.sp,
    unitSize: TextUnit = 10.sp,
) {
    val hours = duration.toHours()
    val remainingMinutes = duration.minusHours(hours).toMinutes()

    val annotatedString = buildAnnotatedString {
        when {
            hours == 0L && remainingMinutes == 0L -> {
                // do nothing
            }
            hours == 0L -> {
                withStyle(style = SpanStyle(fontSize = minutesSize)) {
                    append("$remainingMinutes")
                }
                withStyle(style = SpanStyle(fontSize = unitSize)) {
                    append("分钟")
                }
            }
            remainingMinutes == 0L -> {
                withStyle(style = SpanStyle(fontSize = hourSize)) {
                    append("$hours")
                }
                withStyle(style = SpanStyle(fontSize = unitSize)) {
                    append("小时")
                }
            }
            else -> {
                withStyle(style = SpanStyle(fontSize = hourSize)) {
                    append("$hours")
                }
                withStyle(style = SpanStyle(fontSize = unitSize)) {
                    append("小时")
                }
                withStyle(style = SpanStyle(fontSize = minutesSize)) {
                    append("$remainingMinutes")
                }
                withStyle(style = SpanStyle(fontSize = unitSize)) {
                    append("分钟")
                }
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}




@Preview(showBackground = true)
@Composable
fun Test() {
//    val dateItem = ListItem.DateItem(LocalDate.now())
//
//    DateItem(item = dateItem)

    val event = Event(
        name = "时光流开发",
        startTime = LocalDateTime.now(),
        duration = Duration.ofMinutes(205),
        category = "当下核心",
        subCategory = "实战开发"
    )

    val subEvent = Event(
        name = "老妈来电",
        startTime = LocalDateTime.now(),
        duration = Duration.ofMinutes(19),
        category = "家人",
    )

    Column {

        DateItem(item = ListItem.DateItem(LocalDate.now()))

        MainItem(item = ListItem.MainItem(event))

        SubItem(item = ListItem.SubItem(subEvent))

        Interval(item = ListItem.Interval(12))

        MainItem(item = ListItem.MainItem(event))

        Interval(item = ListItem.Interval(12))

        MainItem(item = ListItem.MainItem(event))
    }
    
//    CategoryTagRow(
//        category = "当下核心",
//        subCategory = "实战开发"
//    )



}

