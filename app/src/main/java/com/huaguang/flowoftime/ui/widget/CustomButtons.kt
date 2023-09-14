package com.huaguang.flowoftime.ui.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressButton(
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 创建一个自定义的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    // 创建一个波纹效果的 Indication
    val rippleIndication = rememberRipple(bounded = false, radius = 30.dp)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp)) // 必须先调用，要不然会被背景覆盖
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                // 使用自定义的 InteractionSource 和 Indication
                interactionSource = interactionSource,
                indication = rippleIndication
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressTextButton(
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 创建一个自定义的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    // 创建一个波纹效果的 Indication
    val rippleIndication = rememberRipple(bounded = false, radius = 80.dp)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(80.dp)) // 必须先调用，要不然会被背景覆盖
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                // 使用自定义的 InteractionSource 和 Indication
                interactionSource = interactionSource,
                indication = rippleIndication
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LongPressOutlinedIconButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = rememberRipple(bounded = true)
    val borderColor = if (enabled) Color.Black else Color.LightGray

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape) // 必须加这个才能把按钮的点击水波纹限制在这个范围内。
            .border(1.dp, borderColor, CircleShape)
            .background(Color.Transparent, shape = CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = indication,
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}


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

@Preview(showBackground = true)
@Composable
fun LongPressButtonPreview() {
    LongPressOutlinedIconButton(
        onClick = { /*TODO*/ },
        onLongClick = { /*TODO*/ }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.step),
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(12.dp)
        )
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