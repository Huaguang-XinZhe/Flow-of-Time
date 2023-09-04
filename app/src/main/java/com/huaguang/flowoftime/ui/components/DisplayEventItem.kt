package com.huaguang.flowoftime.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.pages.time_record.recording_event_item.EventType
import com.huaguang.flowoftime.pages.time_record.recording_event_item.TailLayout
import com.huaguang.flowoftime.utils.extensions.formatDurationInText
import com.huaguang.flowoftime.widget.CategoryLabel
import com.huaguang.flowoftime.widget.LabelType
import com.huaguang.flowoftime.widget.TagsRow
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun DisplayEventItem(
    eventDisplay: EventDisplay,
    iconRepository: IconMappingRepository,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIconButton(eventDisplay.category, iconRepository)

            TailLayout(name = eventDisplay.name, type = EventType.SUBJECT) {// 首个一定是主题事件
                DurationText(duration = eventDisplay.duration!!, type = it)
            }

        }

        Column(
            modifier = Modifier.padding(start = 45.dp, end = 10.dp)
        ) {
            ContentRowList(eventDisplay = eventDisplay)

            eventDisplay.category?.let {
                CategoryLabel(
                    text = "@$it",
                    labelType = LabelType.CATEGORY,
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    // TODO:
                }
            }

            eventDisplay.tags?.let {
                TagsRow(
                    tags = it,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) { // 已经加了 # 号
                    // TODO:
                }
            }
        }
    }
}

@Composable
fun CategoryIconButton(category: String?, iconRepository: IconMappingRepository) {

    IconButton(
        onClick = { /*TODO*/ },
        modifier = Modifier
            .padding(end = 10.dp)
            .size(24.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(iconRepository.getIconUrlByCategory(category))
                .crossfade(true)
                .build(),
            contentDescription = null,
            error = painterResource(id = R.drawable.expand),
            onSuccess = {
                RDALogger.info("加载成功了！")
            },
            modifier = Modifier
                .size(24.dp)
                .padding(2.dp)
        )
    }
}

/**
 * 要求在 Column 作用域内
 */
@Composable
fun ContentRowList(
    eventDisplay: EventDisplay,
    indent: Dp = 0.dp,
) {
    eventDisplay.contentEvents?.forEach { son -> // 在 Column 的作用域内
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = indent)
        ) {
            PrefixText(type = son.type)

            TailLayout(name = son.name, type = son.type) {
                DurationText(duration = son.duration!!, type = it)
            }
        }

        ContentRowList(eventDisplay = son, indent = 24.dp)

    }
}

@Composable
fun PrefixText(type: EventType) {
    val prefix = when (type) {
        EventType.STEP -> "•"
        EventType.INSERT -> ">"
        EventType.FOLLOW -> "*"
        else -> ""
    }

    Text(
        text = prefix,
        fontWeight = FontWeight.Black,
        color = Color.DarkGray,
        modifier = Modifier.padding(end = 5.dp)
    )
}

@Composable
fun DurationText(duration: Duration, type: EventType) {
    Text(
        text = formatDurationInText(duration),
        fontSize = if (type == EventType.SUBJECT) 18.sp else 12.sp,
        fontWeight = FontWeight.ExtraBold,
        fontStyle = FontStyle.Italic,
        modifier = Modifier.padding(start = 5.dp),
    )
}

data class EventDisplay(
    var name: String,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    val duration: Duration? = null,
    val type: EventType,
    var category: String? = null, // 除主题事件外无类属
    val tags: List<String>? = null, // 除主题事件外无标签
    val contentEvents: List<EventDisplay>? = null, // 除主题事件外无内容事件
)



