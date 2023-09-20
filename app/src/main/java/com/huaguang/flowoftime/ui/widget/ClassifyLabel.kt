package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.DashType
import com.huaguang.flowoftime.ui.theme.DeepGreen
import com.huaguang.flowoftime.utils.dashBorder

@Composable
fun Category(
    modifier: Modifier = Modifier,
    name: String = "",
    type: DashType = DashType.MIXED_ADD, // 默认就是混合模式
    onClick: (String, DashType) -> Unit,
) {
    val bgColor: Color
    val borderColor: Color
    val textColor: Color
    val text: String
    val horizontalPadding: Dp
    val verticalPadding: Dp

    if (name.isNotEmpty()) {
        borderColor = Color.Transparent
        bgColor = DeepGreen
        textColor = Color.White
        text = "@$name"
        horizontalPadding = 6.dp
        verticalPadding = 3.dp
    } else {
        borderColor = if (type == DashType.MIXED_ADD) MaterialTheme.colorScheme.primary else DeepGreen
        bgColor = borderColor.copy(alpha = 0.1f)
        textColor = borderColor
        horizontalPadding = 15.dp
        verticalPadding = 1.dp
        text = if (type.isAdd()) "+" else "*"
    }

    Box(
        modifier = modifier
            .dashBorder(borderColor) // dashBorder 放在 clip 后边会被 clip 遮挡，放在前边不会？
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true),
                onClick = {
                    onClick(name, type)
                }
            )
//            .dashBorder() // 放这里会被 clip 剪切（遮挡不全）
            .background(bgColor, shape = CircleShape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
//            .dashBorder() // 放在这里会被 padding 挤到组件里边来
    ) {
        Text(
            text = text,
            color = textColor,
//            modifier = Modifier.align(Alignment.Center) // 加和不加都一样，是居中的
        )
    }
}

@Composable
fun Tag(
    modifier: Modifier = Modifier,
    name: String = "",
    onClick: (String) -> Unit,
) {

    val shape = RoundedCornerShape(4.dp)
    val bgColor: Color
    val borderColor: Color
    val borderModifier: Modifier
    val textColor: Color
    val text: String
    val horizontalPadding: Dp

    if (name.isNotEmpty()) {
        borderColor = Color.DarkGray
        borderModifier = Modifier.border(0.5.dp, borderColor, shape = shape)
        bgColor = borderColor.copy(alpha = 0.05f)
        textColor = borderColor
        text = "#$name"
        horizontalPadding = 3.dp
    } else {
        borderColor = Color.DarkGray
        borderModifier = Modifier.dashBorder(borderColor, 15f)
        bgColor = borderColor.copy(alpha = 0.1f)
        textColor = borderColor
        text = "+"
        horizontalPadding = 15.dp
    }

    Box(
        modifier = modifier
            .then(borderModifier)
            .clip(shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = {
                    onClick(name)
                }
            )
            .background(bgColor, shape = shape)
            .padding(horizontal = horizontalPadding)
    ) {
        Text(
            text = text,
            color = textColor
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsRow(
    modifier: Modifier = Modifier,
    tags: MutableList<String>?,
    addDashButton: Boolean,
    onClick: (String) -> Unit
) {
    FlowRow(
        modifier = modifier
    ) {
        tags?.forEach { tagName -> // 如果为空，就不绘制了，也不需要绘制（复合新增按钮会解决这个问题）
            Tag(
                name = tagName,
                onClick = onClick,
                modifier = Modifier.padding(bottom = 5.dp, end = 5.dp)
            )
        }

        if (addDashButton) {
            Tag(
                onClick = onClick,
                modifier = Modifier.padding(bottom = 5.dp, end = 5.dp)
            )
        }
    }
}


@Composable
fun CategoryRow(
    category: String, // 来自 event
    addDashButton: Boolean,
    onClick: (String, DashType) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
    ) {

        Category(name = category, onClick = onClick)

        if (addDashButton) {
            Spacer(modifier = Modifier.width(5.dp))

            Category(type = DashType.CATEGORY_CHANGE, onClick = onClick)
        }
    }

}

