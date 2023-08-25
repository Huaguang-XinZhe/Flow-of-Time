package com.huaguang.flowoftime.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R


@Composable
fun CategoryLabel(
    text: String,
    labelType: LabelType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape: RoundedCornerShape
    val bgColor: Color
    val textColor: Color
    val horizontalPadding: Dp
    val verticalPadding: Dp

    if (labelType == LabelType.CATEGORY) {
        shape = CircleShape
        bgColor = colorResource(id = R.color.deep_green)
        textColor = Color.White
        horizontalPadding  = 6.dp
        verticalPadding = 3.dp
    } else {
        shape = RoundedCornerShape(4.dp)
        bgColor = Color.DarkGray.copy(alpha = 0.05f)
        textColor = Color.DarkGray
        horizontalPadding = 3.dp
        verticalPadding = 0.dp
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            )
            .border(0.5.dp, textColor, shape = shape)
            .background(bgColor, shape = shape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Text(
            text = text,
            color = textColor
        )
    }

}

enum class LabelType {
    CATEGORY,
    TAG
}

@Composable
fun TagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val labels = tags.map { tag ->
        "#$tag" to LabelType.TAG
    }

    FlowRow(
        modifier = modifier,
        labels = labels,
        mainAxisSpacing = 5.dp,
        crossAxisSpacing = 5.dp
    ) {
        onClick()
    }
}


@Preview(showBackground = true)
@Composable
fun test3() {
//    Row(
//        modifier = Modifier.padding(10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        CategoryLabel(text = "@core", type = LabelType.CATEGORY) {
//
//        }
//
//        CategoryLabel(text = "#html", type = LabelType.TAG) {
//
//        }
//    }
    
//    val labels = listOf(
//        "@core" to LabelType.CATEGORY, "@html" to LabelType.CATEGORY,
//        "#reddit" to LabelType.TAG, "#image" to LabelType.TAG, "@plugin" to LabelType.TAG,
//        "#rendering" to LabelType.TAG, "#image" to LabelType.TAG, "@plugin" to LabelType.TAG,
//    )
//
//    Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
//        CategoryLabels(labels = labels) {
//
//        }
//    }
}