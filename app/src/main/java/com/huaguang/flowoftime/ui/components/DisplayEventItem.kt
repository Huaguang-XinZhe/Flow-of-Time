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
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.widget.CategoryLabel
import com.huaguang.flowoftime.ui.widget.LabelType
import com.huaguang.flowoftime.ui.widget.TagsRow
import com.huaguang.flowoftime.utils.formatDurationInText
import java.time.Duration

@Composable
fun DisplayEventItem(
    combinedEvent: CombinedEvent?,
    viewModel: EventInputViewModel,
    modifier: Modifier = Modifier
) {
    val event = combinedEvent?.event ?: return
    if (event.duration == null) return // 不显示没有间隔的事件

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
            CategoryIconButton(
                category = event.category,
                withContent = event.withContent,
                iconRepository = viewModel.iconRepository
            )

            TailLayout(
                event = event,
                viewModel = viewModel,
            ) {// 首个一定是主题事件
                DurationText(duration = event.duration!!, type = it)
            }

        }

        Column(
            modifier = Modifier.padding(start = 45.dp, end = 10.dp)
        ) {
            ContentRowList(
                combinedEvent = combinedEvent,
                inputViewModel = viewModel,
            )

            event.category?.let {
                CategoryLabel(
                    text = "@$it",
                    labelType = LabelType.CATEGORY,
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    // TODO:
                }
            }

            event.tags?.let {
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
fun CategoryIconButton(
    category: String?,
    withContent: Boolean,
    iconRepository: IconMappingRepository
) {
    val errorRes = if (withContent) R.drawable.expand else R.drawable.collapse

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
            error = painterResource(id = errorRes),
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
    combinedEvent: CombinedEvent,
    inputViewModel: EventInputViewModel,
    indent: Dp = 0.dp,
) {

    combinedEvent.contentEvents.forEach { childCombinedEvent ->
        val son = childCombinedEvent.event

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = indent)
        ) {
            PrefixText(type = son.type)

            TailLayout(
                event = son,
                viewModel = inputViewModel,
            ) {
                DurationText(duration = son.duration!!, type = it)
            }
        }

        // 递归调用 ContentRowList
        ContentRowList(
            combinedEvent = childCombinedEvent,
            inputViewModel = inputViewModel,
            indent = 24.dp
        )
    }

}

@Composable
fun PrefixText(type: EventType) {
    val prefix = when (type) {
        EventType.STEP -> "•"
        EventType.SUBJECT_INSERT, EventType.STEP_INSERT -> ">"
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
        modifier = Modifier.padding(start = 5.dp, end = 10.dp),
    )
}




