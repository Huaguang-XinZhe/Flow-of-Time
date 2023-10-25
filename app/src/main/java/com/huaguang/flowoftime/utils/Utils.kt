package com.huaguang.flowoftime.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
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
import androidx.core.text.getSpans
import com.huaguang.flowoftime.coreEventKeyWords
import com.huaguang.flowoftime.data.models.db_returns.CsvOrder
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

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
    val length = this.length - 2 // 排除 HTML 解析后产生的两个换行符
//    RDALogger.info("length = $length")
    return buildAnnotatedString {
        var lastIndex = 0

        // 暂时不对引用 Span 进行处理，过滤掉！
        getSpans(0, length, Any::class.java).filterNot { it is QuoteSpan }.forEach { span ->
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

fun Editable.getCustomSpans(): List<Any> {
    val interestedSpanTypes = arrayOf(URLSpan::class.java, StyleSpan::class.java)
    val allSpans = this.getSpans<Any>()
    // 创建一个空的集合来存储找到的自定义 spans
    val foundCustomSpans = mutableListOf<Any>()
    // 遍历 allSpans 数组，检查每个 span 是否是你关心的类型之一
    for (span in allSpans) {
        if (interestedSpanTypes.any { it.isInstance(span) }) { // java 类是否是当前 span 的实例
            foundCustomSpans.add(span)
        }
    }
    return foundCustomSpans
}

fun String.extractUrls(): List<String> {
    val urlRegex = "https?://\\S*?(?=[\\u4e00-\\u9fa5\\s,])"
    val pattern = Pattern.compile(urlRegex)
    val matcher = pattern.matcher(this)
    val urls = mutableListOf<String>()
    while (matcher.find()) {
        urls.add(matcher.group())
    }
    return urls
}

fun String.convertToHtml(urls: List<String>): String {
    var htmlText = this
    for (url in urls) {
        htmlText = htmlText.replace(url, """<a href="$url">$url</a>""")
    }

    return htmlText.map {
        if (it.code > 127) "&#${it.code};"
        else if (it == '\n') "<br/>"
        else it.toString()
    }.joinToString(
        separator = "",
        prefix = """<p dir="ltr">""",
        postfix = "</p>"
    )
}


//fun main() {
//    val text = "https://www.baidu.com中国\n\n中过"
//    val urls = text.extractUrls()
//    println(urls)
//}

/**
 * 通过计算第一个日期和最后一个日期之间的间隔（总）来计算平均间隔
 */
fun averageDateInterval(dates: List<String>): Float {
    if (dates.size <= 1) return 0f

    val sortedDates = dates.map { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        .sorted()

    val totalDays = sortedDates.last().toEpochDay() - sortedDates.first().toEpochDay()

    return (totalDays / (dates.size - 1)).toFloat()
}

/**
 * 导出昨天的数据（SimpleEvent 列表）到 CSV 文件
 */
//fun exportToCsv(
//    data: List<SimpleEvent>,
//    filePath: String = "yesterday_simple_events"
//) {
//    val file = File(filePath)
//    file.bufferedWriter().use { out ->
//        // Write header
//        out.write("id,name,duration,category,tags,type,eventDate,parentEventId\n")
//        // Write data
//        data.forEach { item ->
//            out.write("${item.id},${item.name},${item.duration},${item.category}," +
//                    "${item.tags},${item.type},${item.eventDate},${item.parentEventId}\n")
//        }
//    }
//}

/**
 * 导出 CSV 文件的通用函数
 */
fun <T : Any> exportToCsv(
    context: Context,
    data: List<T>,
    filePath: String = "output.csv"
) {
    val file = File(context.filesDir, filePath)
    file.bufferedWriter().use { out ->
        // Get the first item's class, or return early if the list is empty
        val klass = data.firstOrNull()?.javaClass?.kotlin ?: return

        // Sort the properties based on the CsvOrder annotation
        val sortedProperties = klass.memberProperties.sortedBy {
            it.findAnnotation<CsvOrder>()?.order ?: Int.MAX_VALUE
        }

        // Write header
        val header = sortedProperties.joinToString(",") { it.name }
        out.write(header + "\n")

        // Write data
        data.forEach { item ->
            val row = sortedProperties.joinToString(",") { prop ->
                when (val value = prop.call(item)) {
                    is List<*> -> "\"${value.joinToString(",") { it.toString() }}\""  // Quote lists
                    else -> value?.toString() ?: ""
                }
            }
            out.write(row + "\n")
        }
    }
}


fun main() {
    // Example usage
//    val events = listOf(
//        SimpleEvent(0, "Meeting", "1 hour", "Work", listOf("team", "sync"), "Meeting", "2023-10-23", 1),
//        // ... other events
//    )
//    exportToCsv(events, "events.csv")
}