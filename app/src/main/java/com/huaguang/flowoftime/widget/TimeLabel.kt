package com.huaguang.flowoftime.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ardakaplan.rdalogger.RDALogger
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.utils.extensions.formatLocalDateTime
import java.time.LocalDateTime


@Composable
fun TimeLabel(
    time: LocalDateTime,
    onClick: (time: LocalDateTime, checkState: MutableState<Boolean>) -> Unit
) {

    val checkState = remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val borderColor = if (!checkState.value) {
        colorResource(id = R.color.border_time_label)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val textColor = if(!checkState.value) {
        colorResource(id = R.color.content_time_label)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .clip(CircleShape) // 必须放在 clickable 前边，要不然涟漪效果的形状依然是方形。其次，只 clip 没用，border 和 background 也必须指定 shape，否则会显示异常
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true), // 必须设为 true，为 false 的话水波纹的最大范围是一个以组件宽度为直径的圆形
                onClick = {
                    checkState.value = true // 一点就选中，每次都选中
                    onClick(time, checkState) // 将状态传出去，处理完后再将状态设为 false，取消选中
                }
            )
            .border(0.3.dp, borderColor, shape = CircleShape)
            .background(borderColor.copy(alpha = 0.2f), shape = CircleShape)
            .padding(horizontal = 6.dp, vertical = 3.dp) // 可以根据需要调整这个值
    ) {
        Text(
            text = formatLocalDateTime(time),
            color = textColor
        )
    }

}

@Preview(showBackground = true)
@Composable
fun test() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        TimeLabel(LocalDateTime.now()) { time: LocalDateTime, checkState: MutableState<Boolean> ->
            RDALogger.info("点击了时间标签")
//            checkState.value = false
        }

    }
}