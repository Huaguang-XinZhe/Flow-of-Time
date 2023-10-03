package com.huaguang.flowoftime.utils

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.QuoteSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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

fun Spanned.toAnnotatedString(): AnnotatedString {
    val length = this.length
//    RDALogger.info("length = $length")
    return buildAnnotatedString {
        var lastIndex = 0

        // 暂时不对引用 Span 进行处理，过滤掉！
        getSpans(0, length, Any::class.java).filterNot { it is QuoteSpan }.forEach {span ->
//            RDALogger.info("span = $span")
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            // Append text that comes before the span
//            RDALogger.info("lastIndex = $lastIndex, start = $start")
            append(substring(lastIndex, start)) // 这里的 span 如果是引用的话，就会出问题
            when (span) {
                is URLSpan -> {
                    pushStyle(
                        style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                    )
                    pushStringAnnotation(tag = "URL", annotation = span.url)
                    append(substring(start, end)) // 这个不能移走（提到 when 下公共区域），必须放在这里才能正常显示
                    pop()
                    pop() // Pop annotation and style from the stack
                }
                is StyleSpan -> {
                    if (span.style == Typeface.BOLD) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        append(substring(start, end))
                    }
                }
                else -> {
                    append(substring(start, end))
                }
            }
            lastIndex = end
        }
        // Append remaining text
        append(substring(lastIndex, length))
    }
}





