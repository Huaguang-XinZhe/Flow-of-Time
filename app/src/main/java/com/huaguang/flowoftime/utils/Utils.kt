package com.huaguang.flowoftime.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.huaguang.flowoftime.coreEventKeyWords

fun space(num: Int): String {
    var count = 0
    val builder = StringBuilder()
    while (count <= num) {
        builder.append(" ")
        count++
    }
    return builder.toString()
}

fun isCoreEvent(name: String): Boolean {
    for (keyWord in coreEventKeyWords) {
        val contains = name.contains(keyWord, true)

        if (contains) return true
    }

    return false
}


fun Modifier.dashBorder(
    borderColor: Color,
    radius: Float = 35f,
): Modifier {
    return this.drawWithContent { // 它允许我们在现有的绘制内容之上添加额外的绘制逻辑。
        drawContent() // 这个函数调用确保原有的内容被绘制，然后我们在其上添加我们的虚线边框。
        drawRoundRect(
            color = borderColor,
            style = Stroke(
                width = 3f,
                // phase 定义了虚线模式的相位，或者说是虚线模式的起始点。我们设置它为 0f 来从数组的开始处开始虚线模式。
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                cap = StrokeCap.Round
            ),
            cornerRadius = CornerRadius(radius, radius) // 30~35 就比较贴合 CircleShape 了
        )
    }
}

