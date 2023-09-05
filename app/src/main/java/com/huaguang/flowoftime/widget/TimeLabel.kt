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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.pages.time_record.LocalSelectedTime
import com.huaguang.flowoftime.utils.formatLocalDateTime
import java.time.LocalDateTime

@Composable
fun TimeLabel(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
) {
    val selectedTime = LocalSelectedTime.current
    val isSelected = selectedTime?.value == time
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(4.dp)

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.LightGray
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.LightGray
    }

    Box(
        modifier = modifier
            // 必须放在 clickable 前边，要不然涟漪效果的形状依然是方形。其次，只 clip 没用，border 和 background 也必须指定 shape，否则会显示异常
            .clip(shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true), // 必须设为 true，为 false 的话水波纹的最大范围是一个以组件宽度为直径的圆形
                onClick = {
                    selectedTime?.value = time
                }
            )
            .border(0.5.dp, borderColor, shape = shape)
            .background(borderColor.copy(alpha = 0.1f), shape = shape)
            .padding(horizontal = 3.dp) // 可以根据需要调整这个值
    ) {
        Text(
            text = formatLocalDateTime(time),
            color = textColor,
            fontSize = 12.sp
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

        TimeLabel(LocalDateTime.now())

    }
}