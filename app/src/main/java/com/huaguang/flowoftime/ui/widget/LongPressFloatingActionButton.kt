package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    color: Color = Color.White,
    icon: @Composable () -> Unit
) {

    // 创建一个自定义的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    // 创建一个波纹效果的 Indication
    val rippleIndication = rememberRipple(bounded = false, radius = 30.dp)

    Box(
        modifier = modifier
            .size(56.dp)
            // shadow 必须放在 background 上面，否则会有内部锯齿感，也必须要又 shape 设定，要不然就是方形
            .shadow(2.dp, CircleShape)
            .background(color, CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                // 使用自定义的 InteractionSource 和 Indication
                interactionSource = interactionSource,
                indication = rippleIndication
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
@Preview
fun PreviewLongPressFloatingActionButton() {
    LongPressFloatingActionButton(
        onClick = { /*TODO*/ },
        onLongClick = { /*TODO*/ },
        color = Color.White
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.Black
        )
    }
}
