package com.huaguang.flowoftime.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LongPressButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    text: String
) {
    // 创建一个自定义的 InteractionSource
    val interactionSource = remember { MutableInteractionSource() }
    // 创建一个波纹效果的 Indication
    val rippleIndication = rememberRipple(bounded = false, radius = 30.dp)

    Box(
        modifier = Modifier
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    text: String,
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




@Preview(showBackground = true)
@Composable
fun LongPressButtonPreview() {
    LongPressTextButton(onClick = { /*TODO*/ }, onLongClick = { /*TODO*/ }, text = "开始")
}