package com.huaguang.flowoftime

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.ui.theme.DeepGreen
import com.huaguang.flowoftime.ui.theme.Primary

sealed class LabelType(
    val shape: Shape,
    val borderColor: Color,
    val bgColor: Color,
    val textColor: Color,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val displayText: String,
    open val onClick: () -> Unit,
    val isDashBorder: Boolean = false,
) {
    // 匿名内部类不能有参数，这里必须用 class 或 data class
    class Default(
        override val onClick: () -> Unit,
    ) : LabelType(
        shape = CircleShape,
        borderColor = Primary, // TODO: 这里写死了，该如何应用主题呢？
        bgColor = Primary.copy(alpha = 0.1f),
        textColor = Primary,
        horizontalPadding = 15.dp,
        verticalPadding = 1.dp,
        displayText = "+",
        onClick = onClick,
        isDashBorder = true,
    )

    data class Category(
        val name: String,
        override val onClick: () -> Unit,
    ) : LabelType(
        shape = CircleShape,
        borderColor = Color.Transparent,
        bgColor = DeepGreen,
        textColor = Color.White,
        horizontalPadding = 6.dp,
        verticalPadding = 3.dp,
        displayText = "@$name",
        onClick = onClick
    )

    data class Tag(
        val name: String,
        override val onClick: () -> Unit,
    ) : LabelType(
        shape = RoundedCornerShape(4.dp),
        borderColor = Color.DarkGray,
        bgColor = Color.DarkGray.copy(alpha = 0.05f),
        textColor = Color.DarkGray,
        horizontalPadding = 3.dp,
        verticalPadding = 0.dp,
        displayText = "#$name",
        onClick = onClick
    )
}

