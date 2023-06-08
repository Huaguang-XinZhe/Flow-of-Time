package com.huaguang.flowoftime.ui.components.event_name

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.huaguang.flowoftime.R
import com.huaguang.flowoftime.data.models.Event

@Composable
fun EventName(
    event: Event,
    viewModel: EventNameViewModel,
    showTime: Boolean,
    modifier: Modifier = Modifier
) {
    val stateHolder = remember { EventNameState() }
    val isNameClicked  by remember { // TODO:
        viewModel.isNameClicked
    }

    val textOptions = getTextOptions(stateHolder, event.name, showTime)

    Text(
        text = textOptions.text,
        style = MaterialTheme.typography.titleMedium,
        maxLines = textOptions.maxLines,
        overflow = textOptions.overflow,
        onTextLayout = { textLayoutResult ->  // 文本布局完成时的回调函数
            if (textLayoutResult.hasVisualOverflow) {  // 如果文本溢出
                stateHolder.showIcon()
            }
        },
        modifier = modifier.getModifier(event, viewModel, isNameClicked)
    )

    ExpansionIcon(stateHolder = stateHolder)
}

@Composable
fun ExpansionIcon(stateHolder: EventNameState) {
    val painter = if (!stateHolder.isExpand.value) {
        painterResource(id = R.drawable.expand)
    } else {
        painterResource(id = R.drawable.collapse)
    }
    
    if (stateHolder.isShowIcon.value) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 5.dp)
                .clickable {
                    stateHolder.toggleExpansion()
                }
        )
    }
}

data class TextOptions(
    val text: String,
    val maxLines: Int,
    val overflow: TextOverflow
)

fun getTextOptions(
    stateHolder: EventNameState,
    eventName: String,
    showTime: Boolean
): TextOptions {
    val isExpanded = stateHolder.isExpand.value

    return TextOptions(
        text = if (showTime) eventName else "……$eventName",
        maxLines = if (!isExpanded) 1 else 3,
        overflow = if (!isExpanded) TextOverflow.Ellipsis else TextOverflow.Visible
    )
}

fun Modifier.getModifier(
    event: Event,
    viewModel: EventNameViewModel,
    isNameClicked: Boolean = false
): Modifier {
    Log.i("打标签喽", "getModifier 中：isNameClicked = $isNameClicked")

    return if (event.name.length > 10 && event.parentId == null) this else Modifier
        .padding(end = 5.dp)
        .clickable {
            viewModel.onNameTextClicked(event)
        }
        .then(
            if (isNameClicked) {
                Modifier
                    .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
                    .padding(3.dp)
            } else Modifier
        )
}

class EventNameState {
    var isExpand = mutableStateOf(false)
    var isShowIcon = mutableStateOf(false)

    fun toggleExpansion() {
        isExpand.value = !isExpand.value
    }

    fun showIcon() {
        isShowIcon.value = true
    }
}