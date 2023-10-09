package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.LabelType
import com.huaguang.flowoftime.utils.dashBorder

@Composable
fun LabelBlock(
    modifier: Modifier = Modifier,
    category: String? = null,
    tags: List<String>? = null,
    onLabelClick: (() -> Unit)? = null,
    onDashClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
    ) {
        CategoryRow(
            name = category ?: "",
            onLabelClick = onLabelClick,
            onDashClick = onDashClick
        )

        TagsRow(
            tags = tags,
            onClick = { onLabelClick!!.invoke() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Test() {
    Box(
        modifier = Modifier.padding(10.dp)
    ) {
        LabelBlock(
            category = "核心",
            tags = listOf("时光流", "Spring", "现金流", "灵光", "灵感", "框架", "财务", "沉默"),
            onLabelClick = { RDALogger.info("类属或 tag 标签点击") }
        ) {
            RDALogger.info("onDashClick！")
        }
    }
}

@Composable
fun CategoryRow(
    modifier: Modifier = Modifier,
    name: String = "",
    onLabelClick: (() -> Unit)? = null,
    onDashClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (name.isNotEmpty()) {
            Label(
                type = LabelType.Category(
                    name = name,
                    onClick = { onLabelClick!!.invoke() }
                )
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Label(
            type = LabelType.Default(onClick = onDashClick)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsRow(
    modifier: Modifier = Modifier,
    tags: List<String>? = null,
    onClick: () -> Unit
) {
    if (tags == null) return

    FlowRow(
        modifier = modifier
    ) {
        tags.forEach { tagName ->
            Label(
                type = LabelType.Tag(
                    name = tagName,
                    onClick = onClick
                ),
                modifier = Modifier.padding(top = 5.dp, end = 5.dp)
            )
        }
    }
}

@Composable
fun Label(
    modifier: Modifier = Modifier,
    type: LabelType
) {
    type.apply {

        val borderModifier = if (isDashBorder) {
            Modifier.dashBorder(borderColor)
        } else {
            Modifier.border(0.5.dp, borderColor, shape)
        }

        Box(
            modifier = modifier
                .then(borderModifier) // dashBorder 放在 clip 后边会被 clip 遮挡，放在前边不会？
                .clip(shape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = true),
                    onClick = onClick
                )
//            .dashBorder() // 放这里会被 clip 剪切（遮挡不全）
                .background(bgColor, shape = shape)
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
//            .dashBorder() // 放在这里会被 padding 挤到组件里边来
        ) {
            Text(
                text = displayText,
                color = textColor,
//            modifier = Modifier.align(Alignment.Center) // 加和不加都一样，是居中的
            )
        }
    }
}

