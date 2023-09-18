package com.huaguang.flowoftime.ui.components.toggle_item

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huaguang.flowoftime.data.models.CustomTime
import com.huaguang.flowoftime.ui.pages.time_record.LocalSelectedTime
import com.huaguang.flowoftime.utils.formatLocalDateTime

@Composable
fun TimeLabel(
    customTime: CustomTime,
    customTimeState: MutableState<CustomTime?>,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(4.dp)

    val selectedTime = LocalSelectedTime.current
    val dbTime = customTime.initialTime!! // 这个值来自数据库，其实也是变化的。

    if (customTime.timeState.value == null) {
        customTime.timeState.value = dbTime
    }

    val isSelected = selectedTime?.value == dbTime  // 管理 TimeLabel 的选中态
    // 选中的 Label 才动态变化，不是 Label 有点击（任何一个）就可以
    val timeDisplay = if (isSelected) customTime.timeState.value!!
    else if (selectedTime?.value == null) dbTime else dbTime

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
                    if (isSelected) return@clickable // 选中后就不允许再次选中
                    selectedTime?.value = dbTime // 使用 dynamicTime 赋值的结果有点魔幻
                    customTimeState.value = customTime.also {
                        it.timeState.value = dbTime
                    } // 设置状态，这个状态会传给 TimeRegulator
                }
            )
            .border(0.5.dp, borderColor, shape = shape)
            .background(borderColor.copy(alpha = 0.1f), shape = shape)
            .padding(horizontal = 3.dp) // 可以根据需要调整这个值
    ) {
        Text(
            text = formatLocalDateTime(timeDisplay),
            color = textColor,
            fontSize = 12.sp
        )
    }

}

