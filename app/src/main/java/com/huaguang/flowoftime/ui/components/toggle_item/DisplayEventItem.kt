package com.huaguang.flowoftime.ui.components.toggle_item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.huaguang.flowoftime.EventType
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.CombinedEvent
import com.huaguang.flowoftime.data.repositories.IconMappingRepository
import com.huaguang.flowoftime.ui.components.category_dialog.CategoryViewModel
import com.huaguang.flowoftime.ui.components.event_input.EventInputViewModel
import com.huaguang.flowoftime.ui.state.ItemState
import com.huaguang.flowoftime.ui.widget.LabelBlock
import com.huaguang.flowoftime.utils.formatDurationInText
import java.time.Duration

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayEventItem(
    modifier: Modifier = Modifier,
    combinedEvent: CombinedEvent?,
    itemState: ItemState,
    viewModel: EventInputViewModel = viewModel(),
) {
    val event = combinedEvent?.event ?: return
    if (event.duration == null) return // 不显示没有间隔的事件

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .shadow(3.dp, CardDefaults.shape) // 必须放在 clip 之前，放在之后没有效果
            .clip(CardDefaults.shape) // 加上这一句虽然解决了波纹范围非圆角的问题，但也把阴影弄没了
            .combinedClickable(
                onClick = {},
                onDoubleClick = { viewModel.onDisplayItemDoubleClick(itemState) },
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true)
            ),
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
            ) {// 首个一定是主题事件
                DurationText(duration = event.duration!!, type = it)
            }

        }

        Column(
            modifier = Modifier.padding(start = 45.dp, end = 10.dp)
        ) {
            ContentRowList(
                combinedEvent = combinedEvent,
                itemState = itemState,
            )

            LabelRow(id = event.id, category = event.category, tags = event.tags)
        }
    }
}

@Composable
fun LabelRow(
    id: Long,
    category: String?,
    tags: List<String>?,
    viewModel: CategoryViewModel = viewModel()
) {

    LabelBlock(
        category = category,
        tags = tags,
        onLabelClick = {
            viewModel.onClassNameClick(id, category!!) // 能点击得到，就一定不为空
        },
        modifier = Modifier
            .padding(bottom = 5.dp)
            .fillMaxWidth()
    ) {
        viewModel.onDashButtonClick(id, category, tags)
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
            modifier = Modifier
                .size(24.dp)
                .padding(2.dp),
//            colorFilter = ColorFilter.tint(Color.DarkGray) // 更改所加载图片的颜色，不太好用，有些图标会出现覆盖的情况
        )
    }
}

/**
 * 要求在 Column 作用域内
 */
@Composable
fun ContentRowList(
    combinedEvent: CombinedEvent,
    itemState: ItemState,
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
            ) {
                DurationText(duration = son.duration!!, type = it)
            }
        }

        // 递归调用 ContentRowList
        ContentRowList(
            combinedEvent = childCombinedEvent,
            itemState = itemState,
            indent = 24.dp,
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
        modifier = Modifier.padding(horizontal = 5.dp),
    )
}



